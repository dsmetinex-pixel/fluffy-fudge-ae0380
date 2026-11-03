package com.example.data.repository

import com.example.data.local.MetinexDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.DashboardSummary
import com.example.data.model.LiveStockItem
import com.example.data.model.Product
import com.example.data.model.StockInTransaction
import com.example.data.model.StockOutTransaction
import com.example.data.remote.supabase.SupabaseClient
import com.example.data.remote.supabase.SupabaseSyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InventoryRepository(
    private val database: MetinexDatabase,
    val supabaseClient: SupabaseClient = SupabaseClient()
) {

    private val productDao = database.productDao()
    private val stockInDao = database.stockInDao()
    private val stockOutDao = database.stockOutDao()
    private val activityLogDao = database.activityLogDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allStockIn: Flow<List<StockInTransaction>> = stockInDao.getAllStockIn()
    val allStockOut: Flow<List<StockOutTransaction>> = stockOutDao.getAllStockOut()
    val allLogs: Flow<List<ActivityLog>> = activityLogDao.getAllLogs()

    val supabaseSyncStatus: StateFlow<SupabaseSyncStatus> = supabaseClient.syncStatus
    val supabaseSyncLogs: StateFlow<List<String>> = supabaseClient.syncLogs

    private fun currentDateString(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }

    // Dashboard Summary derived flow
    val dashboardSummary: Flow<DashboardSummary> = combine(
        allProducts,
        allStockIn,
        allStockOut
    ) { products, stockInList, stockOutList ->
        val totalProducts = products.size
        val totalAvailableStock = products.sumOf { it.currentStock }
        val totalStockIn = stockInList.sumOf { it.quantity }
        val totalStockOut = stockOutList.sumOf { it.quantity }
        val totalInventoryAmount = products.sumOf { it.currentStock * it.ratePerUnit }

        val today = currentDateString()
        val todayIn = stockInList.count { it.date == today }
        val todayOut = stockOutList.count { it.date == today }
        val todayTransactionsCount = todayIn + todayOut

        val lowStockCount = products.count { it.currentStock <= it.minimumStock }

        DashboardSummary(
            totalProducts = totalProducts,
            totalAvailableStock = totalAvailableStock,
            totalStockIn = totalStockIn,
            totalStockOut = totalStockOut,
            totalInventoryAmount = totalInventoryAmount,
            todayTransactionsCount = todayTransactionsCount,
            lowStockCount = lowStockCount
        )
    }

    // Live Stock calculation flow:
    // AVAILABLE STOCK = OPENING STOCK + TOTAL STOCK IN - TOTAL STOCK OUT
    // TOTAL INVENTORY VALUE = AVAILABLE STOCK * RATE PER UNIT
    val liveStockItems: Flow<List<LiveStockItem>> = combine(
        allProducts,
        allStockIn,
        allStockOut
    ) { products, stockInList, stockOutList ->
        val stockInByProduct = stockInList.groupBy { it.productId }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
        val stockOutByProduct = stockOutList.groupBy { it.productId }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }

        products.mapIndexed { index, product ->
            val totalIn = stockInByProduct[product.productId] ?: 0.0
            val totalOut = stockOutByProduct[product.productId] ?: 0.0
            val available = product.openingStock + totalIn - totalOut
            val totalVal = available * product.ratePerUnit

            LiveStockItem(
                slNo = index + 1,
                productId = product.productId,
                productName = product.productName,
                hsmCode = product.hsmCode,
                category = product.category,
                unit = product.unit,
                openingStock = product.openingStock,
                totalStockIn = totalIn,
                totalStockOut = totalOut,
                availableStock = available,
                ratePerUnit = product.ratePerUnit,
                totalInventoryValue = totalVal,
                isLowStock = available > 0 && available <= product.minimumStock,
                isOutOfStock = available <= 0
            )
        }
    }

    suspend fun addStockIn(
        productId: String,
        productName: String,
        quantity: Double,
        unit: String,
        hsmCode: String,
        rate: Double,
        date: String,
        firmName: String,
        user: String = "Admin (dsmetinex@gmail.com)"
    ): Result<StockInTransaction> {
        val product = productDao.getProductById(productId)
            ?: return Result.failure(Exception("Product not found with ID: $productId"))

        val previousStock = product.currentStock
        val newStock = previousStock + quantity
        val totalAmount = quantity * rate
        val txId = "IN-" + UUID.randomUUID().toString().substring(0, 8).uppercase()

        val tx = StockInTransaction(
            transactionId = txId,
            productId = productId,
            productName = productName,
            quantity = quantity,
            unit = unit,
            previousStock = previousStock,
            hsmCode = hsmCode,
            rate = rate,
            totalAmount = totalAmount,
            newStock = newStock,
            date = date,
            firmName = firmName,
            createdBy = user
        )

        stockInDao.insertStockIn(tx)
        productDao.updateStock(productId, newStock)

        val log = ActivityLog(
            logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
            userName = user,
            action = "Stock In",
            description = "Added $quantity $unit to $productName from firm: $firmName",
            productName = productName,
            transactionType = "IN",
            quantity = quantity,
            previousStock = previousStock,
            updatedStock = newStock,
            date = date
        )
        activityLogDao.insertLog(log)

        // Asynchronous cloud sync to Supabase
        scope.launch {
            supabaseClient.insertStockIn(tx)
            productDao.getProductById(productId)?.let { updatedProd ->
                supabaseClient.upsertProduct(updatedProd)
            }
            supabaseClient.insertActivityLog(log)
        }

        return Result.success(tx)
    }

    suspend fun addStockOut(
        partyName: String,
        productId: String,
        productName: String,
        quantity: Double,
        unit: String,
        hsmCode: String,
        rate: Double,
        date: String,
        firmName: String,
        user: String = "Admin (dsmetinex@gmail.com)"
    ): Result<StockOutTransaction> {
        val product = productDao.getProductById(productId)
            ?: return Result.failure(Exception("Product not found with ID: $productId"))

        val previousStock = product.currentStock
        if (quantity > previousStock) {
            return Result.failure(
                IllegalArgumentException("Insufficient stock! Available stock is $previousStock $unit, but $quantity $unit was requested.")
            )
        }

        val remainingStock = previousStock - quantity
        val totalAmount = quantity * rate
        val txId = "OUT-" + UUID.randomUUID().toString().substring(0, 8).uppercase()

        val tx = StockOutTransaction(
            transactionId = txId,
            productId = productId,
            productName = productName,
            partyName = partyName,
            quantity = quantity,
            unit = unit,
            previousStock = previousStock,
            hsmCode = hsmCode,
            rate = rate,
            totalAmount = totalAmount,
            remainingStock = remainingStock,
            date = date,
            firmName = firmName,
            createdBy = user
        )

        stockOutDao.insertStockOut(tx)
        productDao.updateStock(productId, remainingStock)

        val log = ActivityLog(
            logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
            userName = user,
            action = "Stock Out",
            description = "Dispatched $quantity $unit of $productName to party: $partyName (Firm: $firmName)",
            productName = productName,
            transactionType = "OUT",
            quantity = quantity,
            previousStock = previousStock,
            updatedStock = remainingStock,
            date = date
        )
        activityLogDao.insertLog(log)

        // Asynchronous cloud sync to Supabase
        scope.launch {
            supabaseClient.insertStockOut(tx)
            productDao.getProductById(productId)?.let { updatedProd ->
                supabaseClient.upsertProduct(updatedProd)
            }
            supabaseClient.insertActivityLog(log)
        }

        return Result.success(tx)
    }

    suspend fun deleteStockIn(transactionId: String): Result<Unit> {
        val tx = stockInDao.getStockInById(transactionId)
            ?: return Result.failure(Exception("Stock In transaction not found"))
        val product = productDao.getProductById(tx.productId)

        if (product != null) {
            val adjustedStock = (product.currentStock - tx.quantity).coerceAtLeast(0.0)
            productDao.updateStock(tx.productId, adjustedStock)
        }
        stockInDao.deleteStockIn(tx)

        val log = ActivityLog(
            logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
            action = "Transaction Deleted",
            description = "Reversed Stock In transaction $transactionId for ${tx.productName} (${tx.quantity} ${tx.unit})",
            productName = tx.productName,
            transactionType = "IN",
            quantity = tx.quantity,
            previousStock = product?.currentStock ?: 0.0,
            updatedStock = ((product?.currentStock ?: 0.0) - tx.quantity).coerceAtLeast(0.0),
            date = currentDateString()
        )
        activityLogDao.insertLog(log)

        // Cloud sync deletion
        scope.launch {
            supabaseClient.deleteStockIn(transactionId)
            productDao.getProductById(tx.productId)?.let {
                supabaseClient.upsertProduct(it)
            }
            supabaseClient.insertActivityLog(log)
        }

        return Result.success(Unit)
    }

    suspend fun deleteStockOut(transactionId: String): Result<Unit> {
        val tx = stockOutDao.getStockOutById(transactionId)
            ?: return Result.failure(Exception("Stock Out transaction not found"))
        val product = productDao.getProductById(tx.productId)

        if (product != null) {
            val adjustedStock = product.currentStock + tx.quantity
            productDao.updateStock(tx.productId, adjustedStock)
        }
        stockOutDao.deleteStockOut(tx)

        val log = ActivityLog(
            logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
            action = "Transaction Deleted",
            description = "Reversed Stock Out transaction $transactionId for ${tx.productName} (${tx.quantity} ${tx.unit})",
            productName = tx.productName,
            transactionType = "OUT",
            quantity = tx.quantity,
            previousStock = product?.currentStock ?: 0.0,
            updatedStock = (product?.currentStock ?: 0.0) + tx.quantity,
            date = currentDateString()
        )
        activityLogDao.insertLog(log)

        // Cloud sync deletion
        scope.launch {
            supabaseClient.deleteStockOut(transactionId)
            productDao.getProductById(tx.productId)?.let {
                supabaseClient.upsertProduct(it)
            }
            supabaseClient.insertActivityLog(log)
        }

        return Result.success(Unit)
    }

    suspend fun saveProduct(product: Product): Result<Unit> {
        productDao.insertProduct(product)
        val log = ActivityLog(
            logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
            action = "Product Added",
            description = "Created new product ${product.productName} (Code: ${product.hsmCode})",
            productName = product.productName,
            transactionType = "PRODUCT",
            quantity = product.openingStock,
            previousStock = 0.0,
            updatedStock = product.currentStock,
            date = currentDateString()
        )
        activityLogDao.insertLog(log)

        scope.launch {
            supabaseClient.upsertProduct(product)
            supabaseClient.insertActivityLog(log)
        }

        return Result.success(Unit)
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        productDao.updateProduct(product)
        val log = ActivityLog(
            logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
            action = "Product Edited",
            description = "Updated product specs for ${product.productName}",
            productName = product.productName,
            transactionType = "PRODUCT",
            quantity = 0.0,
            previousStock = product.currentStock,
            updatedStock = product.currentStock,
            date = currentDateString()
        )
        activityLogDao.insertLog(log)

        scope.launch {
            supabaseClient.upsertProduct(product)
            supabaseClient.insertActivityLog(log)
        }

        return Result.success(Unit)
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        val product = productDao.getProductById(productId)
        if (product != null) {
            productDao.deleteProduct(product)
            val log = ActivityLog(
                logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                action = "Product Deleted",
                description = "Deleted product ${product.productName}",
                productName = product.productName,
                transactionType = "PRODUCT",
                date = currentDateString()
            )
            activityLogDao.insertLog(log)

            scope.launch {
                supabaseClient.deleteProduct(productId)
                supabaseClient.insertActivityLog(log)
            }
        }
        return Result.success(Unit)
    }

    suspend fun testSupabaseConnection(): Result<Boolean> {
        return supabaseClient.testConnection()
    }

    suspend fun syncAllToSupabase(): Result<Int> {
        val prods = allProducts.first()
        val inList = allStockIn.first()
        val outList = allStockOut.first()
        val logs = allLogs.first()
        return supabaseClient.syncAll(prods, inList, outList, logs)
    }
}
