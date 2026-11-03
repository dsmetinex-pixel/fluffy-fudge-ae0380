package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.ExcelExporter
import com.example.data.model.ActivityLog
import com.example.data.model.DashboardSummary
import com.example.data.model.LiveStockItem
import com.example.data.model.Product
import com.example.data.model.StockInTransaction
import com.example.data.model.StockOutTransaction
import com.example.data.remote.supabase.SupabaseSyncStatus
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppNavDestination(val label: String) {
    HOME("Home"),
    STOCK_IN("Stock In"),
    STOCK_OUT("Stock Out"),
    LIVE_STOCK("Live Stock"),
    PRODUCTS("Products"),
    REPORTS("Reports"),
    LOGS("Log")
}

data class StockInFormState(
    val selectedProductId: String = "",
    val quantity: String = "",
    val unit: String = "KG",
    val previousStock: Double = 0.0,
    val hsmCode: String = "",
    val rate: String = "",
    val date: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
    val firmName: String = "Metinex Aluminium Plant 1",
    val editingTransactionId: String? = null
)

data class StockOutFormState(
    val partyName: String = "",
    val selectedProductId: String = "",
    val quantity: String = "",
    val unit: String = "KG",
    val previousStock: Double = 0.0,
    val hsmCode: String = "",
    val rate: String = "",
    val date: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
    val firmName: String = "Metinex Aluminium Plant 1",
    val editingTransactionId: String? = null
)

data class ProductFormState(
    val productId: String = "",
    val productName: String = "",
    val hsmCode: String = "",
    val category: String = "Extrusions",
    val unit: String = "KG",
    val ratePerUnit: String = "",
    val openingStock: String = "",
    val minimumStock: String = "",
    val description: String = "",
    val isEditing: Boolean = false
)

class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _currentDestination = MutableStateFlow(AppNavDestination.HOME)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockIn: StateFlow<List<StockInTransaction>> = repository.allStockIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockOut: StateFlow<List<StockOutTransaction>> = repository.allStockOut
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ActivityLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardSummary: StateFlow<DashboardSummary> = repository.dashboardSummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    val liveStockItems: StateFlow<List<LiveStockItem>> = repository.liveStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supabaseSyncStatus: StateFlow<SupabaseSyncStatus> = repository.supabaseSyncStatus
    val supabaseSyncLogs: StateFlow<List<String>> = repository.supabaseSyncLogs

    fun testSupabaseConnection() {
        viewModelScope.launch {
            repository.testSupabaseConnection()
        }
    }

    fun syncAllToSupabase() {
        viewModelScope.launch {
            val res = repository.syncAllToSupabase()
            res.onSuccess { count ->
                emitMessage("Synced $count items to Supabase cloud successfully.")
            }.onFailure { err ->
                emitError("Supabase sync failed: ${err.message}")
            }
        }
    }

    // Stock In Form State
    private val _stockInForm = MutableStateFlow(StockInFormState())
    val stockInForm: StateFlow<StockInFormState> = _stockInForm.asStateFlow()

    // Stock Out Form State
    private val _stockOutForm = MutableStateFlow(StockOutFormState())
    val stockOutForm: StateFlow<StockOutFormState> = _stockOutForm.asStateFlow()

    // Product Form Dialog State
    private val _productForm = MutableStateFlow<ProductFormState?>(null)
    val productForm: StateFlow<ProductFormState?> = _productForm.asStateFlow()

    // Filters for Stock In Report
    val stockInSearchQuery = MutableStateFlow("")
    val stockInFirmFilter = MutableStateFlow("")
    val stockInDateFilter = MutableStateFlow("")

    // Filters for Stock Out Report
    val stockOutSearchQuery = MutableStateFlow("")
    val stockOutPartyFilter = MutableStateFlow("")
    val stockOutFirmFilter = MutableStateFlow("")
    val stockOutDateFilter = MutableStateFlow("")

    // Filters for Live Stock
    val liveStockSearchQuery = MutableStateFlow("")
    val liveStockCategoryFilter = MutableStateFlow("ALL")
    val liveStockStatusFilter = MutableStateFlow("ALL") // ALL, LOW_STOCK, OUT_OF_STOCK, IN_STOCK

    // Filtered Stock In List
    val filteredStockIn: StateFlow<List<StockInTransaction>> = combine(
        allStockIn,
        stockInSearchQuery,
        stockInFirmFilter,
        stockInDateFilter
    ) { list, query, firm, date ->
        list.filter { item ->
            (query.isEmpty() || item.productName.contains(query, ignoreCase = true) || item.hsmCode.contains(query, ignoreCase = true)) &&
            (firm.isEmpty() || item.firmName.contains(firm, ignoreCase = true)) &&
            (date.isEmpty() || item.date.contains(date))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Stock Out List
    val filteredStockOut: StateFlow<List<StockOutTransaction>> = combine(
        allStockOut,
        stockOutSearchQuery,
        stockOutPartyFilter,
        stockOutFirmFilter,
        stockOutDateFilter
    ) { list, query, party, firm, date ->
        list.filter { item ->
            (query.isEmpty() || item.productName.contains(query, ignoreCase = true) || item.hsmCode.contains(query, ignoreCase = true)) &&
            (party.isEmpty() || item.partyName.contains(party, ignoreCase = true)) &&
            (firm.isEmpty() || item.firmName.contains(firm, ignoreCase = true)) &&
            (date.isEmpty() || item.date.contains(date))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Live Stock List
    val filteredLiveStock: StateFlow<List<LiveStockItem>> = combine(
        liveStockItems,
        liveStockSearchQuery,
        liveStockCategoryFilter,
        liveStockStatusFilter
    ) { list, query, category, status ->
        list.filter { item ->
            val matchesQuery = query.isEmpty() ||
                item.productName.contains(query, ignoreCase = true) ||
                item.hsmCode.contains(query, ignoreCase = true)
            val matchesCategory = category == "ALL" || item.category.equals(category, ignoreCase = true)
            val matchesStatus = when (status) {
                "LOW_STOCK" -> item.isLowStock
                "OUT_OF_STOCK" -> item.isOutOfStock
                "IN_STOCK" -> !item.isLowStock && !item.isOutOfStock
                else -> true
            }
            matchesQuery && matchesCategory && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(dest: AppNavDestination) {
        _currentDestination.value = dest
    }

    // ---------------- Stock In Actions ----------------
    fun onSelectStockInProduct(product: Product) {
        _stockInForm.value = _stockInForm.value.copy(
            selectedProductId = product.productId,
            unit = product.unit,
            previousStock = product.currentStock,
            hsmCode = product.hsmCode,
            rate = product.ratePerUnit.toString()
        )
    }

    fun updateStockInQuantity(qty: String) {
        _stockInForm.value = _stockInForm.value.copy(quantity = qty)
    }

    fun updateStockInUnit(unit: String) {
        _stockInForm.value = _stockInForm.value.copy(unit = unit)
    }

    fun updateStockInRate(rate: String) {
        _stockInForm.value = _stockInForm.value.copy(rate = rate)
    }

    fun updateStockInDate(date: String) {
        _stockInForm.value = _stockInForm.value.copy(date = date)
    }

    fun updateStockInFirm(firm: String) {
        _stockInForm.value = _stockInForm.value.copy(firmName = firm)
    }

    fun resetStockInForm() {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        _stockInForm.value = StockInFormState(date = today)
    }

    fun submitStockIn() {
        val form = _stockInForm.value
        val prodId = form.selectedProductId
        if (prodId.isBlank()) {
            emitError("Please select a product first.")
            return
        }
        val qty = form.quantity.toDoubleOrNull()
        if (qty == null || qty <= 0.0) {
            emitError("Please enter a valid positive quantity.")
            return
        }
        val rate = form.rate.toDoubleOrNull() ?: 0.0
        val product = products.value.find { it.productId == prodId }
        val productName = product?.productName ?: "Product"

        viewModelScope.launch {
            val result = repository.addStockIn(
                productId = prodId,
                productName = productName,
                quantity = qty,
                unit = form.unit,
                hsmCode = form.hsmCode,
                rate = rate,
                date = form.date,
                firmName = form.firmName.ifBlank { "Metinex Aluminium Plant 1" }
            )
            result.onSuccess {
                emitMessage("Stock In recorded successfully! Stock updated to ${it.newStock} ${it.unit}")
                resetStockInForm()
            }.onFailure { err ->
                emitError(err.message ?: "Failed to record Stock In")
            }
        }
    }

    fun deleteStockIn(transactionId: String) {
        viewModelScope.launch {
            repository.deleteStockIn(transactionId)
                .onSuccess { emitMessage("Stock In transaction removed and stock reversed.") }
                .onFailure { emitError(it.message ?: "Failed to delete transaction.") }
        }
    }

    // ---------------- Stock Out Actions ----------------
    fun onSelectStockOutProduct(product: Product) {
        _stockOutForm.value = _stockOutForm.value.copy(
            selectedProductId = product.productId,
            unit = product.unit,
            previousStock = product.currentStock,
            hsmCode = product.hsmCode,
            rate = product.ratePerUnit.toString()
        )
    }

    fun updateStockOutParty(party: String) {
        _stockOutForm.value = _stockOutForm.value.copy(partyName = party)
    }

    fun updateStockOutQuantity(qty: String) {
        _stockOutForm.value = _stockOutForm.value.copy(quantity = qty)
    }

    fun updateStockOutUnit(unit: String) {
        _stockOutForm.value = _stockOutForm.value.copy(unit = unit)
    }

    fun updateStockOutRate(rate: String) {
        _stockOutForm.value = _stockOutForm.value.copy(rate = rate)
    }

    fun updateStockOutDate(date: String) {
        _stockOutForm.value = _stockOutForm.value.copy(date = date)
    }

    fun updateStockOutFirm(firm: String) {
        _stockOutForm.value = _stockOutForm.value.copy(firmName = firm)
    }

    fun resetStockOutForm() {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        _stockOutForm.value = StockOutFormState(date = today)
    }

    fun submitStockOut() {
        val form = _stockOutForm.value
        if (form.partyName.isBlank()) {
            emitError("Party / Customer Name is required.")
            return
        }
        val prodId = form.selectedProductId
        if (prodId.isBlank()) {
            emitError("Please select a product.")
            return
        }
        val qty = form.quantity.toDoubleOrNull()
        if (qty == null || qty <= 0.0) {
            emitError("Please enter a valid positive quantity.")
            return
        }

        val product = products.value.find { it.productId == prodId }
        val currentStock = product?.currentStock ?: 0.0
        if (qty > currentStock) {
            emitError("Insufficient Stock! Available stock is $currentStock ${product?.unit ?: ""}, but $qty was requested.")
            return
        }

        val rate = form.rate.toDoubleOrNull() ?: 0.0
        val productName = product?.productName ?: "Product"

        viewModelScope.launch {
            val result = repository.addStockOut(
                partyName = form.partyName,
                productId = prodId,
                productName = productName,
                quantity = qty,
                unit = form.unit,
                hsmCode = form.hsmCode,
                rate = rate,
                date = form.date,
                firmName = form.firmName.ifBlank { "Metinex Aluminium Plant 1" }
            )
            result.onSuccess {
                emitMessage("Stock Out recorded! Remaining stock: ${it.remainingStock} ${it.unit}")
                resetStockOutForm()
            }.onFailure { err ->
                emitError(err.message ?: "Failed to record Stock Out")
            }
        }
    }

    fun deleteStockOut(transactionId: String) {
        viewModelScope.launch {
            repository.deleteStockOut(transactionId)
                .onSuccess { emitMessage("Stock Out transaction removed and stock restored.") }
                .onFailure { emitError(it.message ?: "Failed to delete transaction.") }
        }
    }

    // ---------------- Product Catalog Actions ----------------
    fun openAddProductDialog() {
        _productForm.value = ProductFormState(
            productId = "PRD-AL-" + UUID.randomUUID().toString().substring(0, 4).uppercase(),
            unit = "KG",
            isEditing = false
        )
    }

    fun openEditProductDialog(product: Product) {
        _productForm.value = ProductFormState(
            productId = product.productId,
            productName = product.productName,
            hsmCode = product.hsmCode,
            category = product.category,
            unit = product.unit,
            ratePerUnit = product.ratePerUnit.toString(),
            openingStock = product.openingStock.toString(),
            minimumStock = product.minimumStock.toString(),
            description = product.description,
            isEditing = true
        )
    }

    fun closeProductDialog() {
        _productForm.value = null
    }

    fun updateProductFormField(updater: (ProductFormState) -> ProductFormState) {
        _productForm.value?.let { current ->
            _productForm.value = updater(current)
        }
    }

    fun saveProductFromDialog() {
        val form = _productForm.value ?: return
        if (form.productName.isBlank()) {
            emitError("Product Name is required.")
            return
        }
        if (form.hsmCode.isBlank()) {
            emitError("HSM Code is required.")
            return
        }
        val rate = form.ratePerUnit.toDoubleOrNull() ?: 0.0
        val opening = form.openingStock.toDoubleOrNull() ?: 0.0
        val minStock = form.minimumStock.toDoubleOrNull() ?: 10.0

        viewModelScope.launch {
            if (form.isEditing) {
                val existing = products.value.find { it.productId == form.productId }
                val updated = Product(
                    productId = form.productId,
                    productName = form.productName,
                    hsmCode = form.hsmCode,
                    category = form.category,
                    unit = form.unit,
                    ratePerUnit = rate,
                    openingStock = opening,
                    minimumStock = minStock,
                    currentStock = existing?.currentStock ?: opening,
                    description = form.description,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateProduct(updated)
                emitMessage("Product '${updated.productName}' updated successfully.")
            } else {
                val newProd = Product(
                    productId = form.productId.ifBlank { "PRD-" + UUID.randomUUID().toString().substring(0, 6).uppercase() },
                    productName = form.productName,
                    hsmCode = form.hsmCode,
                    category = form.category,
                    unit = form.unit,
                    ratePerUnit = rate,
                    openingStock = opening,
                    minimumStock = minStock,
                    currentStock = opening,
                    description = form.description
                )
                repository.saveProduct(newProd)
                emitMessage("New product '${newProd.productName}' added to catalog.")
            }
            closeProductDialog()
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            emitMessage("Product removed from catalog.")
        }
    }

    // ---------------- Excel Exports ----------------
    fun exportStockInExcel(context: Context) {
        viewModelScope.launch {
            val list = filteredStockIn.value
            if (list.isEmpty()) {
                emitError("No Stock In records to export.")
                return@launch
            }
            try {
                ExcelExporter.exportStockIn(context, list)
                emitMessage("Exporting ${list.size} Stock In records to Excel...")
            } catch (e: Exception) {
                emitError("Export failed: ${e.message}")
            }
        }
    }

    fun exportStockOutExcel(context: Context) {
        viewModelScope.launch {
            val list = filteredStockOut.value
            if (list.isEmpty()) {
                emitError("No Stock Out records to export.")
                return@launch
            }
            try {
                ExcelExporter.exportStockOut(context, list)
                emitMessage("Exporting ${list.size} Stock Out records to Excel...")
            } catch (e: Exception) {
                emitError("Export failed: ${e.message}")
            }
        }
    }

    fun exportLiveStockExcel(context: Context) {
        viewModelScope.launch {
            val list = filteredLiveStock.value
            if (list.isEmpty()) {
                emitError("No Live Stock records to export.")
                return@launch
            }
            try {
                ExcelExporter.exportLiveStock(context, list)
                emitMessage("Exporting ${list.size} Live Stock records to Excel...")
            } catch (e: Exception) {
                emitError("Export failed: ${e.message}")
            }
        }
    }

    fun exportProductsExcel(context: Context) {
        viewModelScope.launch {
            val list = products.value
            if (list.isEmpty()) {
                emitError("No products to export.")
                return@launch
            }
            try {
                ExcelExporter.exportProducts(context, list)
                emitMessage("Exporting ${list.size} Products to Excel...")
            } catch (e: Exception) {
                emitError("Export failed: ${e.message}")
            }
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch { _snackbarEvent.emit(msg) }
    }

    private fun emitError(err: String) {
        viewModelScope.launch { _errorEvent.emit(err) }
    }
}
