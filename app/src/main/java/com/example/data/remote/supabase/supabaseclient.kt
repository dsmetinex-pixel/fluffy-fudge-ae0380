package com.example.data.remote.supabase

import android.content.Context
import android.util.Log
import com.example.data.model.ActivityLog
import com.example.data.model.Product
import com.example.data.model.StockInTransaction
import com.example.data.model.StockOutTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class SupabaseSyncStatus {
    object Idle : SupabaseSyncStatus()
    data class Syncing(val message: String) : SupabaseSyncStatus()
    data class Connected(
        val tablesReady: Boolean,
        val lastSyncTime: String,
        val message: String
    ) : SupabaseSyncStatus()
    data class Error(val error: String, val lastAttempt: String) : SupabaseSyncStatus()
}

class SupabaseClient(
    private val baseUrl: String = SupabaseConfig.DEFAULT_BASE_URL,
    private val apiKey: String = SupabaseConfig.DEFAULT_API_KEY
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val _syncStatus = MutableStateFlow<SupabaseSyncStatus>(SupabaseSyncStatus.Idle)
    val syncStatus: StateFlow<SupabaseSyncStatus> = _syncStatus.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<String>>(emptyList())
    val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()

    private fun addLog(entry: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val list = _syncLogs.value.toMutableList()
        list.add(0, "[$time] $entry")
        if (list.size > 50) list.removeAt(list.lastIndex)
        _syncLogs.value = list
    }

    private fun buildRequest(endpoint: String, method: String = "GET", body: String? = null, upsert: Boolean = false): Request {
        val url = "$baseUrl/rest/v1/$endpoint"
        val builder = Request.Builder()
            .url(url)
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")

        if (upsert) {
            builder.addHeader("Prefer", "resolution=merge-duplicates")
        }

        when (method) {
            "POST" -> {
                val reqBody = (body ?: "{}").toRequestBody(jsonMediaType)
                builder.post(reqBody)
            }
            "PATCH" -> {
                val reqBody = (body ?: "{}").toRequestBody(jsonMediaType)
                builder.patch(reqBody)
            }
            "DELETE" -> builder.delete()
            else -> builder.get()
        }

        return builder.build()
    }

    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            _syncStatus.value = SupabaseSyncStatus.Syncing("Testing connection to Supabase...")
            val request = buildRequest("products?select=product_id&limit=1")
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                val time = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())

                when {
                    response.isSuccessful -> {
                        val msg = "Connected! Supabase database tables are active and verified."
                        _syncStatus.value = SupabaseSyncStatus.Connected(tablesReady = true, lastSyncTime = time, message = msg)
                        addLog("Connection verified: 200 OK (Tables Ready)")
                        Result.success(true)
                    }
                    response.code == 404 && bodyStr.contains("PGRST205") -> {
                        val msg = "Connected to Supabase project! PostgreSQL tables (products, stock_in, etc.) need to be created via SQL Editor."
                        _syncStatus.value = SupabaseSyncStatus.Connected(tablesReady = false, lastSyncTime = time, message = msg)
                        addLog("Connected to Supabase gateway: Tables need creation in SQL Editor")
                        Result.success(false)
                    }
                    else -> {
                        val errMsg = "HTTP ${response.code}: $bodyStr"
                        _syncStatus.value = SupabaseSyncStatus.Error(errMsg, time)
                        addLog("Connection test failed: $errMsg")
                        Result.failure(IOException(errMsg))
                    }
                }
            }
        } catch (e: Exception) {
            val time = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            _syncStatus.value = SupabaseSyncStatus.Error(e.message ?: "Connection error", time)
            addLog("Network error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun upsertProduct(product: Product): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("product_id", product.productId)
                put("product_name", product.productName)
                put("hsm_code", product.hsmCode)
                put("category", product.category)
                put("unit", product.unit)
                put("rate_per_unit", product.ratePerUnit)
                put("opening_stock", product.openingStock)
                put("minimum_stock", product.minimumStock)
                put("current_stock", product.currentStock)
                put("description", product.description)
                put("created_at", product.createdAt)
                put("updated_at", product.updatedAt)
            }

            val request = buildRequest("products", method = "POST", body = json.toString(), upsert = true)
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful || response.code == 201) {
                    addLog("Product synced to Supabase: ${product.productName}")
                    Result.success(Unit)
                } else {
                    addLog("Failed syncing product ${product.productId}: ${response.code} $bodyStr")
                    Result.failure(IOException("HTTP ${response.code}: $bodyStr"))
                }
            }
        } catch (e: Exception) {
            addLog("Product sync error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("products?product_id=eq.$productId", method = "DELETE")
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    addLog("Product deleted from Supabase: $productId")
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertStockIn(tx: StockInTransaction): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("transaction_id", tx.transactionId)
                put("product_id", tx.productId)
                put("product_name", tx.productName)
                put("quantity", tx.quantity)
                put("unit", tx.unit)
                put("previous_stock", tx.previousStock)
                put("hsm_code", tx.hsmCode)
                put("rate", tx.rate)
                put("total_amount", tx.totalAmount)
                put("new_stock", tx.newStock)
                put("date", tx.date)
                put("firm_name", tx.firmName)
                put("created_at", tx.createdAt)
                put("created_by", tx.createdBy)
            }

            val request = buildRequest("stock_in", method = "POST", body = json.toString(), upsert = true)
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful || response.code == 201) {
                    addLog("Stock In synced to Supabase: ${tx.transactionId} (${tx.quantity} ${tx.unit} ${tx.productName})")
                    Result.success(Unit)
                } else {
                    addLog("Failed syncing Stock In: ${response.code} $bodyStr")
                    Result.failure(IOException("HTTP ${response.code}: $bodyStr"))
                }
            }
        } catch (e: Exception) {
            addLog("Stock In sync error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteStockIn(transactionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("stock_in?transaction_id=eq.$transactionId", method = "DELETE")
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    addLog("Stock In deleted from Supabase: $transactionId")
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertStockOut(tx: StockOutTransaction): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("transaction_id", tx.transactionId)
                put("product_id", tx.productId)
                put("product_name", tx.productName)
                put("party_name", tx.partyName)
                put("quantity", tx.quantity)
                put("unit", tx.unit)
                put("previous_stock", tx.previousStock)
                put("hsm_code", tx.hsmCode)
                put("rate", tx.rate)
                put("total_amount", tx.totalAmount)
                put("remaining_stock", tx.remainingStock)
                put("date", tx.date)
                put("firm_name", tx.firmName)
                put("created_at", tx.createdAt)
                put("created_by", tx.createdBy)
            }

            val request = buildRequest("stock_out", method = "POST", body = json.toString(), upsert = true)
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful || response.code == 201) {
                    addLog("Stock Out synced to Supabase: ${tx.transactionId} (${tx.quantity} ${tx.unit} to ${tx.partyName})")
                    Result.success(Unit)
                } else {
                    addLog("Failed syncing Stock Out: ${response.code} $bodyStr")
                    Result.failure(IOException("HTTP ${response.code}: $bodyStr"))
                }
            }
        } catch (e: Exception) {
            addLog("Stock Out sync error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteStockOut(transactionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("stock_out?transaction_id=eq.$transactionId", method = "DELETE")
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    addLog("Stock Out deleted from Supabase: $transactionId")
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertActivityLog(log: ActivityLog): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("log_id", log.logId)
                put("user_id", log.userId)
                put("user_name", log.userName)
                put("action", log.action)
                put("description", log.description)
                put("product_name", log.productName)
                put("transaction_type", log.transactionType)
                put("quantity", log.quantity)
                put("previous_stock", log.previousStock)
                put("updated_stock", log.updatedStock)
                put("date", log.date)
                put("timestamp", log.timestamp)
            }

            val request = buildRequest("activity_logs", method = "POST", body = json.toString(), upsert = true)
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 201) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAll(
        products: List<Product>,
        stockIn: List<StockInTransaction>,
        stockOut: List<StockOutTransaction>,
        logs: List<ActivityLog>
    ): Result<Int> = withContext(Dispatchers.IO) {
        _syncStatus.value = SupabaseSyncStatus.Syncing("Pushing all inventory data to Supabase...")
        var count = 0

        try {
            // 1. Sync Products
            for (p in products) {
                if (upsertProduct(p).isSuccess) count++
            }

            // 2. Sync Stock In
            for (inTx in stockIn) {
                if (insertStockIn(inTx).isSuccess) count++
            }

            // 3. Sync Stock Out
            for (outTx in stockOut) {
                if (insertStockOut(outTx).isSuccess) count++
            }

            // 4. Sync Logs
            for (log in logs.take(50)) {
                if (insertActivityLog(log).isSuccess) count++
            }

            val time = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            _syncStatus.value = SupabaseSyncStatus.Connected(
                tablesReady = true,
                lastSyncTime = time,
                message = "Successfully synchronized $count items to Supabase cloud."
            )
            addLog("Full sync completed: $count items sent to Supabase")
            Result.success(count)
        } catch (e: Exception) {
            val time = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            _syncStatus.value = SupabaseSyncStatus.Error(e.message ?: "Sync failed", time)
            addLog("Full sync error: ${e.message}")
            Result.failure(e)
        }
    }
}
