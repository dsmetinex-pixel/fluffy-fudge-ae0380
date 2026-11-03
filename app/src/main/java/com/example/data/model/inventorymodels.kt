package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val productId: String,
    val productName: String,
    val hsmCode: String,
    val category: String,
    val unit: String,
    val ratePerUnit: Double,
    val openingStock: Double,
    val minimumStock: Double,
    val currentStock: Double,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_in")
data class StockInTransaction(
    @PrimaryKey
    val transactionId: String,
    val productId: String,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val previousStock: Double,
    val hsmCode: String,
    val rate: Double,
    val totalAmount: Double,
    val newStock: Double,
    val date: String, // DD-MM-YYYY
    val firmName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "Admin (dsmetinex@gmail.com)"
)

@Entity(tableName = "stock_out")
data class StockOutTransaction(
    @PrimaryKey
    val transactionId: String,
    val productId: String,
    val productName: String,
    val partyName: String,
    val quantity: Double,
    val unit: String,
    val previousStock: Double,
    val hsmCode: String,
    val rate: Double,
    val totalAmount: Double,
    val remainingStock: Double,
    val date: String, // DD-MM-YYYY
    val firmName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "Admin (dsmetinex@gmail.com)"
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey
    val logId: String,
    val userId: String = "usr_admin",
    val userName: String = "Admin User",
    val action: String, // e.g. "Stock In", "Stock Out", "Product Added", "Product Edited", "Product Deleted"
    val description: String,
    val productName: String = "",
    val transactionType: String = "", // IN, OUT, PRODUCT, SYSTEM
    val quantity: Double = 0.0,
    val previousStock: Double = 0.0,
    val updatedStock: Double = 0.0,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DashboardSummary(
    val totalProducts: Int = 0,
    val totalAvailableStock: Double = 0.0,
    val totalStockIn: Double = 0.0,
    val totalStockOut: Double = 0.0,
    val totalInventoryAmount: Double = 0.0,
    val todayTransactionsCount: Int = 0,
    val lowStockCount: Int = 0
)

data class LiveStockItem(
    val slNo: Int,
    val productId: String,
    val productName: String,
    val hsmCode: String,
    val category: String,
    val unit: String,
    val openingStock: Double,
    val totalStockIn: Double,
    val totalStockOut: Double,
    val availableStock: Double,
    val ratePerUnit: Double,
    val totalInventoryValue: Double,
    val isLowStock: Boolean,
    val isOutOfStock: Boolean
)
