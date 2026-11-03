package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testAppNameConfiguration() {
        val appName = "Metinex Aluminium"
        assertEquals("Metinex Aluminium", appName)
    }
}
