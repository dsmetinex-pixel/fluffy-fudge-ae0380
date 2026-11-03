package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockInGreenLight
import com.example.ui.theme.StockInGreenText
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.StockOutRedLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun LiveStockScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by viewModel.filteredLiveStock.collectAsState()
    val searchQuery by viewModel.liveStockSearchQuery.collectAsState()
    val categoryFilter by viewModel.liveStockCategoryFilter.collectAsState()
    val statusFilter by viewModel.liveStockStatusFilter.collectAsState()

    val totalAvailableQty = items.sumOf { it.availableStock }
    val totalInventoryVal = items.sumOf { it.totalInventoryValue }
    val lowStockCount = items.count { it.isLowStock }
    val outOfStockCount = items.count { it.isOutOfStock }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 8: Heading
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REAL-TIME INVENTORY",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDark
                    )
                    Text(
                        text = "Live stock formula: Opening + Total In − Total Out",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.exportLiveStockExcel(context) },
                    modifier = Modifier.testTag("btn_export_live_stock"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary Statistics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOTAL AVAILABLE UNITS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f", totalAvailableQty),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlue
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = StockInGreenLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOTAL INVENTORY VALUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StockInGreenText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("₹%.2f", totalInventoryVal),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StockInGreenText
                        )
                    }
                }
            }
        }

        // Search & Category / Status Filters Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.liveStockSearchQuery.value = it },
                        placeholder = { Text("Search by product name or HSM code...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.liveStockSearchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        )
                    )

                    // Stock Status Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = statusFilter == "ALL",
                            onClick = { viewModel.liveStockStatusFilter.value = "ALL" },
                            label = { Text("All (${items.size})") }
                        )
                        FilterChip(
                            selected = statusFilter == "LOW_STOCK",
                            onClick = { viewModel.liveStockStatusFilter.value = "LOW_STOCK" },
                            label = { Text("Low Stock ($lowStockCount)") }
                        )
                        FilterChip(
                            selected = statusFilter == "OUT_OF_STOCK",
                            onClick = { viewModel.liveStockStatusFilter.value = "OUT_OF_STOCK" },
                            label = { Text("Out of Stock ($outOfStockCount)") }
                        )
                    }

                    // Category Chips (Horizontal row)
                    val categories = listOf("ALL", "Extrusions", "Channels", "Architectural", "Hardware", "Sheets", "Accessories", "Powder Coated")
                    val catScroll = rememberScrollState()
                    Row(
                        modifier = Modifier.horizontalScroll(catScroll),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = categoryFilter == cat,
                                onClick = { viewModel.liveStockCategoryFilter.value = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Section 8 Table: SL NO, PRODUCT NAME, HSM CODE, UNIT, OPENING STOCK, TOTAL STOCK IN, TOTAL STOCK OUT, AVAILABLE STOCK, RATE PER UNIT, TOTAL INVENTORY VALUE
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                val horizontalScroll = rememberScrollState()

                Column(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .background(NavySurface)
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReportHeaderCell("SL NO", 50.dp)
                        ReportHeaderCell("PRODUCT NAME", 210.dp)
                        ReportHeaderCell("HSM CODE", 90.dp)
                        ReportHeaderCell("CATEGORY", 110.dp)
                        ReportHeaderCell("UNIT", 60.dp)
                        ReportHeaderCell("OPENING", 80.dp)
                        ReportHeaderCell("TOTAL IN", 80.dp)
                        ReportHeaderCell("TOTAL OUT", 80.dp)
                        ReportHeaderCell("AVAILABLE", 100.dp)
                        ReportHeaderCell("RATE/UNIT", 90.dp)
                        ReportHeaderCell("TOTAL VALUE", 120.dp)
                        ReportHeaderCell("STATUS", 100.dp)
                    }

                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .width(1150.dp)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No inventory items match search criteria.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        items.forEachIndexed { index, item ->
                            val isEven = index % 2 == 0
                            val rowBg = when {
                                item.isOutOfStock -> Color(0xFFFEF2F2)
                                item.isLowStock -> Color(0xFFFFFBEB)
                                isEven -> CardBackground
                                else -> Color(0xFFF8FAFC)
                            }

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ReportDataCell((index + 1).toString(), 50.dp)
                                ReportDataCell(item.productName, 210.dp, isBold = true)
                                ReportDataCell(item.hsmCode, 90.dp)
                                ReportDataCell(item.category, 110.dp)
                                ReportDataCell(item.unit, 60.dp)
                                ReportDataCell(String.format("%.2f", item.openingStock), 80.dp)
                                ReportDataCell(String.format("+%.2f", item.totalStockIn), 80.dp, color = StockInGreen)
                                ReportDataCell(String.format("-%.2f", item.totalStockOut), 80.dp, color = StockOutRed)
                                ReportDataCell(
                                    String.format("%.2f", item.availableStock),
                                    100.dp,
                                    color = if (item.isOutOfStock) StockOutRed else if (item.isLowStock) AlertAmber else NavyDark,
                                    isBold = true
                                )
                                ReportDataCell(String.format("₹%.2f", item.ratePerUnit), 90.dp)
                                ReportDataCell(String.format("₹%.2f", item.totalInventoryValue), 120.dp, color = PrimaryBlue, isBold = true)

                                Box(modifier = Modifier.width(100.dp)) {
                                    if (item.isOutOfStock) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = StockOutRedLight) {
                                            Text("OUT OF STOCK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StockOutRed, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    } else if (item.isLowStock) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = AlertAmberLight) {
                                            Text("LOW STOCK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AlertAmber, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    } else {
                                        Surface(shape = RoundedCornerShape(4.dp), color = StockInGreenLight) {
                                            Text("HEALTHY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StockInGreenText, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Totals Summary Row
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0))
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReportDataCell("TOTAL", 50.dp, isBold = true)
                            ReportDataCell("${items.size} Products", 210.dp, isBold = true)
                            ReportDataCell("", 90.dp)
                            ReportDataCell("", 110.dp)
                            ReportDataCell("", 60.dp)
                            ReportDataCell("", 80.dp)
                            ReportDataCell("", 80.dp)
                            ReportDataCell("", 80.dp)
                            ReportDataCell(String.format("%.2f", totalAvailableQty), 100.dp, color = PrimaryBlue, isBold = true)
                            ReportDataCell("", 90.dp)
                            ReportDataCell(String.format("₹%.2f", totalInventoryVal), 120.dp, color = PrimaryBlue, isBold = true)
                            ReportDataCell("", 100.dp)
                        }
                    }
                }
            }
        }
    }
}
