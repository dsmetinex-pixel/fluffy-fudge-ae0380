package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityLog
import com.example.data.model.Product
import com.example.data.model.StockInTransaction
import com.example.data.model.StockOutTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY productName ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE productId = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE productName = :name LIMIT 1")
    suspend fun getProductByName(name: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE productId = :id")
    suspend fun deleteProductById(id: String)

    @Query("UPDATE products SET currentStock = :stock, updatedAt = :updatedAt WHERE productId = :id")
    suspend fun updateStock(id: String, stock: Double, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface StockInDao {
    @Query("SELECT * FROM stock_in ORDER BY createdAt DESC")
    fun getAllStockIn(): Flow<List<StockInTransaction>>

    @Query("SELECT * FROM stock_in WHERE productId = :productId ORDER BY createdAt DESC")
    fun getStockInForProduct(productId: String): Flow<List<StockInTransaction>>

    @Query("SELECT * FROM stock_in WHERE transactionId = :id LIMIT 1")
    suspend fun getStockInById(id: String): StockInTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockIn(transaction: StockInTransaction)

    @Update
    suspend fun updateStockIn(transaction: StockInTransaction)

    @Delete
    suspend fun deleteStockIn(transaction: StockInTransaction)

    @Query("DELETE FROM stock_in WHERE transactionId = :id")
    suspend fun deleteStockInById(id: String)
}

@Dao
interface StockOutDao {
    @Query("SELECT * FROM stock_out ORDER BY createdAt DESC")
    fun getAllStockOut(): Flow<List<StockOutTransaction>>

    @Query("SELECT * FROM stock_out WHERE productId = :productId ORDER BY createdAt DESC")
    fun getStockOutForProduct(productId: String): Flow<List<StockOutTransaction>>

    @Query("SELECT * FROM stock_out WHERE transactionId = :id LIMIT 1")
    suspend fun getStockOutById(id: String): StockOutTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockOut(transaction: StockOutTransaction)

    @Update
    suspend fun updateStockOut(transaction: StockOutTransaction)

    @Delete
    suspend fun deleteStockOut(transaction: StockOutTransaction)

    @Query("DELETE FROM stock_out WHERE transactionId = :id")
    suspend fun deleteStockOutById(id: String)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs WHERE logId = :id")
    suspend fun deleteLogById(id: String)
}
