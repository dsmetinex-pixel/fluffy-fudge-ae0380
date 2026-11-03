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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.StockOutRedLight
import com.example.ui.theme.StockOutRedText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockOutReportScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stockOutItems by viewModel.filteredStockOut.collectAsState()
    val searchQuery by viewModel.stockOutSearchQuery.collectAsState()
    val partyFilter by viewModel.stockOutPartyFilter.collectAsState()
    val firmFilter by viewModel.stockOutFirmFilter.collectAsState()
    val dateFilter by viewModel.stockOutDateFilter.collectAsState()

    var activeQuickFilter by remember { mutableStateOf("ALL") }

    val totalQty = stockOutItems.sumOf { it.quantity }
    val totalAmount = stockOutItems.sumOf { it.totalAmount }

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
                        text = "STOCK OUT REPORT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDark
                    )
                    Text(
                        text = "Complete dispatch & customer delivery records",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.exportStockOutExcel(context) },
                    modifier = Modifier.testTag("btn_download_stock_out_excel"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DOWNLOAD STOCK OUT EXCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary Counters: Total Stock Out Quantity, Total Stock Out Amount
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
                    colors = CardDefaults.cardColors(containerColor = StockOutRedLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOTAL STOCK OUT QUANTITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StockOutRedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f Units", totalQty),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StockOutRedText
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
                            text = "TOTAL STOCK OUT AMOUNT",
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
                        onValueChange = { viewModel.stockOutSearchQuery.value = it },
                        placeholder = { Text("Search product name or HSM code...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.stockOutSearchQuery.value = "" }) {
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

                    // Party and Firm filter row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = partyFilter,
                            onValueChange = { viewModel.stockOutPartyFilter.value = it },
                            placeholder = { Text("Filter Party / Customer...", fontSize = 12.sp) },
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
                            onValueChange = { viewModel.stockOutDateFilter.value = it },
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

                    // Quick Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = activeQuickFilter == "ALL",
                            onClick = {
                                activeQuickFilter = "ALL"
                                viewModel.stockOutDateFilter.value = ""
                            },
                            label = { Text("All Dispatches") }
                        )
                        FilterChip(
                            selected = activeQuickFilter == "TODAY",
                            onClick = {
                                activeQuickFilter = "TODAY"
                                viewModel.stockOutDateFilter.value = todayStr
                            },
                            label = { Text("Today's Dispatches") }
                        )
                        FilterChip(
                            selected = activeQuickFilter == "THIS_MONTH",
                            onClick = {
                                activeQuickFilter = "THIS_MONTH"
                                viewModel.stockOutDateFilter.value = currentMonthStr
                            },
                            label = { Text("Monthly Dispatches") }
                        )
                    }
                }
            }
        }

        // Section 7 Table: SL NO, DATE, PARTY NAME, PRODUCT NAME, HSM CODE, FIRM NAME, PREVIOUS STOCK, STOCK OUT, UNIT, RATE, TOTAL AMOUNT, REMAINING STOCK
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
                        ReportHeaderCell("PARTY NAME", 150.dp)
                        ReportHeaderCell("PRODUCT NAME", 200.dp)
                        ReportHeaderCell("HSM CODE", 90.dp)
                        ReportHeaderCell("FIRM NAME", 130.dp)
                        ReportHeaderCell("PREV STOCK", 90.dp)
                        ReportHeaderCell("STOCK OUT", 90.dp)
                        ReportHeaderCell("UNIT", 60.dp)
                        ReportHeaderCell("RATE", 80.dp)
                        ReportHeaderCell("TOTAL AMOUNT", 110.dp)
                        ReportHeaderCell("REMAINING STOCK", 110.dp)
                    }

                    if (stockOutItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .width(1200.dp)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Stock Out records match the filters.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        stockOutItems.forEachIndexed { index, item ->
                            val isEven = index % 2 == 0
                            Row(
                                modifier = Modifier
                                    .background(if (isEven) CardBackground else Color(0xFFF8FAFC))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ReportDataCell((index + 1).toString(), 50.dp)
                                ReportDataCell(item.date, 90.dp)
                                ReportDataCell(item.partyName, 150.dp, isBold = true)
                                ReportDataCell(item.productName, 200.dp, isBold = true)
                                ReportDataCell(item.hsmCode, 90.dp)
                                ReportDataCell(item.firmName, 130.dp)
                                ReportDataCell(String.format("%.2f", item.previousStock), 90.dp)
                                ReportDataCell(String.format("-%.2f", item.quantity), 90.dp, color = StockOutRed, isBold = true)
                                ReportDataCell(item.unit, 60.dp)
                                ReportDataCell(String.format("₹%.2f", item.rate), 80.dp)
                                ReportDataCell(String.format("₹%.2f", item.totalAmount), 110.dp, color = PrimaryBlue, isBold = true)
                                ReportDataCell(String.format("%.2f", item.remainingStock), 110.dp, isBold = true)
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
                            ReportDataCell("${stockOutItems.size} Records", 150.dp, isBold = true)
                            ReportDataCell("", 200.dp)
                            ReportDataCell("", 90.dp)
                            ReportDataCell("", 130.dp)
                            ReportDataCell("", 90.dp)
                            ReportDataCell(String.format("%.2f", totalQty), 90.dp, color = StockOutRed, isBold = true)
                            ReportDataCell("", 60.dp)
                            ReportDataCell("", 80.dp)
                            ReportDataCell(String.format("₹%.2f", totalAmount), 110.dp, color = PrimaryBlue, isBold = true)
                            ReportDataCell("", 110.dp)
                        }
                    }
                }
            }
        }
    }
}
