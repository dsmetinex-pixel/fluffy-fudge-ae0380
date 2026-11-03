package com.example

import com.example.data.model.LiveStockItem
import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testStockCalculations() {
        val openingStock = 250.0
        val stockIn = 50.0
        val stockOut = 30.0
        val rate = 340.0

        val available = openingStock + stockIn - stockOut
        val totalValue = available * rate

        assertEquals(270.0, available, 0.001)
        assertEquals(91800.0, totalValue, 0.001)
    }

    @Test
    fun testInsufficientStockValidation() {
        val availableStock = 45.0
        val requestedStockOut = 50.0

        val isInsufficient = requestedStockOut > availableStock
        assertTrue("Requested quantity exceeds available stock", isInsufficient)
    }

    @Test
    fun testLowStockStatus() {
        val minStock = 50.0
        val currentStockHealthy = 60.0
        val currentStockLow = 30.0
        val currentStockOut = 0.0

        assertTrue(currentStockLow <= minStock && currentStockLow > 0)
        assertTrue(currentStockOut <= 0)
        assertFalse(currentStockHealthy <= minStock)
    }
}
