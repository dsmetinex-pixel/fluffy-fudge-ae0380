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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockInTransaction
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockInGreenLight
import com.example.ui.theme.StockInGreenText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockInReportScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stockInItems by viewModel.filteredStockIn.collectAsState()
    val searchQuery by viewModel.stockInSearchQuery.collectAsState()
    val firmFilter by viewModel.stockInFirmFilter.collectAsState()
    val dateFilter by viewModel.stockInDateFilter.collectAsState()

    var activeQuickFilter by remember { mutableStateOf("ALL") } // ALL, TODAY, THIS_MONTH

    val totalQty = stockInItems.sumOf { it.quantity }
    val totalAmount = stockInItems.sumOf { it.totalAmount }

    val todayStr = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) }
    val currentMonthStr = remember { SimpleDateFormat("-MM-yyyy", Locale.getDefault()).format(Date()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Heading & Excel Download Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STOCK IN REPORT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDark
                    )
                    Text(
                        text = "Detailed audit trail of raw materials received",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.exportStockInExcel(context) },
                    modifier = Modifier.testTag("btn_download_stock_in_excel"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DOWNLOAD STOCK IN EXCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary Counters: Total Stock In Quantity, Total Stock In Amount
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
                    colors = CardDefaults.cardColors(containerColor = StockInGreenLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOTAL STOCK IN QUANTITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StockInGreenText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f Units", totalQty),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StockInGreenText
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOTAL STOCK IN AMOUNT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("₹%.2f", totalAmount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }

        // Search & Filters Card
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
                        onValueChange = { viewModel.stockInSearchQuery.value = it },
                        placeholder = { Text("Search product name or HSM code...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.stockInSearchQuery.value = "" }) {
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

                    // Firm and Date filter row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = firmFilter,
                            onValueChange = { viewModel.stockInFirmFilter.value = it },
                            placeholder = { Text("Filter firm / supplier...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = dateFilter,
                            onValueChange = { viewModel.stockInDateFilter.value = it },
                            placeholder = { Text("DD-MM-YYYY", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }

                    // Quick Chips: All, Today, This Month
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = activeQuickFilter == "ALL",
                            onClick = {
                                activeQuickFilter = "ALL"
                                viewModel.stockInDateFilter.value = ""
                            },
                            label = { Text("All Records") }
                        )
                        FilterChip(
                            selected = activeQuickFilter == "TODAY",
                            onClick = {
                                activeQuickFilter = "TODAY"
                                viewModel.stockInDateFilter.value = todayStr
                            },
                            label = { Text("Daily (Today)") }
                        )
                        FilterChip(
                            selected = activeQuickFilter == "THIS_MONTH",
                            onClick = {
                                activeQuickFilter = "THIS_MONTH"
                                viewModel.stockInDateFilter.value = currentMonthStr
                            },
                            label = { Text("Monthly Report") }
                        )
                    }
                }
            }
        }

        // Section 5: Table Header & Horizontal Scroll Table
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
                        ReportHeaderCell("DATE", 90.dp)
                        ReportHeaderCell("PRODUCT NAME", 200.dp)
                        ReportHeaderCell("HSM CODE", 90.dp)
                        ReportHeaderCell("FIRM NAME", 130.dp)
                        ReportHeaderCell("PREV STOCK", 90.dp)
                        ReportHeaderCell("STOCK IN", 90.dp)
                        ReportHeaderCell("UNIT", 60.dp)
                        ReportHeaderCell("RATE", 80.dp)
                        ReportHeaderCell("TOTAL AMOUNT", 110.dp)
                        ReportHeaderCell("NEW STOCK", 90.dp)
                    }

                    if (stockInItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .width(1080.dp)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Stock In records match the current filters.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        stockInItems.forEachIndexed { index, item ->
                            val isEven = index % 2 == 0
                            Row(
                                modifier = Modifier
                                    .background(if (isEven) CardBackground else Color(0xFFF8FAFC))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ReportDataCell((index + 1).toString(), 50.dp)
                                ReportDataCell(item.date, 90.dp)
                                ReportDataCell(item.productName, 200.dp, isBold = true)
                                ReportDataCell(item.hsmCode, 90.dp)
                                ReportDataCell(item.firmName, 130.dp)
                                ReportDataCell(String.format("%.2f", item.previousStock), 90.dp)
                                ReportDataCell(String.format("+%.2f", item.quantity), 90.dp, color = StockInGreen, isBold = true)
                                ReportDataCell(item.unit, 60.dp)
                                ReportDataCell(String.format("₹%.2f", item.rate), 80.dp)
                                ReportDataCell(String.format("₹%.2f", item.totalAmount), 110.dp, color = PrimaryBlue, isBold = true)
                                ReportDataCell(String.format("%.2f", item.newStock), 90.dp, isBold = true)
                            }
                        }

                        // Summary Totals Row
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0))
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReportDataCell("TOTAL", 50.dp, isBold = true)
                            ReportDataCell("", 90.dp)
                            ReportDataCell("${stockInItems.size} Records", 200.dp, isBold = true)
                            ReportDataCell("", 90.dp)
                            ReportDataCell("", 130.dp)
                            ReportDataCell("", 90.dp)
                            ReportDataCell(String.format("%.2f", totalQty), 90.dp, color = StockInGreen, isBold = true)
                            ReportDataCell("", 60.dp)
                            ReportDataCell("", 80.dp)
                            ReportDataCell(String.format("₹%.2f", totalAmount), 110.dp, color = PrimaryBlue, isBold = true)
                            ReportDataCell("", 90.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.width(width)
    )
}

@Composable
fun ReportDataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    color: Color = TextPrimary,
    isBold: Boolean = false
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        modifier = Modifier.width(width)
    )
}
