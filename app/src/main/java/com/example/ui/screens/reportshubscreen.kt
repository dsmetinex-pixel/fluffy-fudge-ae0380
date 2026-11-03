package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockInGreenLight
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.StockOutRedLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun ReportsHubScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary by viewModel.dashboardSummary.collectAsState()
    var selectedReportTab by remember { mutableIntStateOf(0) } // 0: Hub, 1: Stock In, 2: Stock Out

    Column(modifier = modifier.fillMaxSize()) {
        // Tab header
        TabRow(
            selectedTabIndex = selectedReportTab,
            containerColor = CardBackground,
            contentColor = PrimaryBlue
        ) {
            Tab(
                selected = selectedReportTab == 0,
                onClick = { selectedReportTab = 0 },
                text = { Text("All Reports (7)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedReportTab == 1,
                onClick = { selectedReportTab = 1 },
                text = { Text("Stock In Report", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedReportTab == 2,
                onClick = { selectedReportTab = 2 },
                text = { Text("Stock Out Report", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        when (selectedReportTab) {
            1 -> StockInReportScreen(viewModel = viewModel)
            2 -> StockOutReportScreen(viewModel = viewModel)
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Column {
                            Text(
                                text = "METINEX CENTRAL REPORTS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyDark
                            )
                            Text(
                                text = "Generate, filter, and export Excel-compatible audit reports",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // 1. Stock In Report
                    item {
                        ReportCardItem(
                            title = "1. Stock In Report",
                            description = "All incoming raw materials with SL, date, HSM, supplier firm, quantities and rates.",
                            icon = Icons.Default.ReceiptLong,
                            iconTint = StockInGreen,
                            iconBg = StockInGreenLight,
                            onView = { selectedReportTab = 1 },
                            onDownloadExcel = { viewModel.exportStockInExcel(context) }
                        )
                    }

                    // 2. Stock Out Report
                    item {
                        ReportCardItem(
                            title = "2. Stock Out Report",
                            description = "Full dispatch audit trail with party names, delivery dates, quantities and remaining balances.",
                            icon = Icons.Default.PointOfSale,
                            iconTint = StockOutRed,
                            iconBg = StockOutRedLight,
                            onView = { selectedReportTab = 2 },
                            onDownloadExcel = { viewModel.exportStockOutExcel(context) }
                        )
                    }

                    // 3. Live Stock Report
                    item {
                        ReportCardItem(
                            title = "3. Live Stock & Valuation Report",
                            description = "Real-time stock formula calculations (Opening + Total In - Total Out) and inventory values.",
                            icon = Icons.Default.Inventory,
                            iconTint = PrimaryBlue,
                            iconBg = PrimaryBlueLight,
                            onView = { viewModel.navigateTo(AppNavDestination.LIVE_STOCK) },
                            onDownloadExcel = { viewModel.exportLiveStockExcel(context) }
                        )
                    }

                    // 4. Product Catalog Report
                    item {
                        ReportCardItem(
                            title = "4. Product Catalog Master",
                            description = "Master database of all aluminium extrusion sections, channels, hardware, and rates.",
                            icon = Icons.Default.Assessment,
                            iconTint = Color(0xFF7C3AED),
                            iconBg = Color(0xFFEDE9FE),
                            onView = { viewModel.navigateTo(AppNavDestination.PRODUCTS) },
                            onDownloadExcel = { viewModel.exportProductsExcel(context) }
                        )
                    }

                    // 5. Inventory Valuation Report
                    item {
                        ReportCardItem(
                            title = "5. Inventory Valuation Report",
                            description = "Financial summary of current stock valuation totaling ₹${String.format("%.2f", summary.totalInventoryAmount)}.",
                            icon = Icons.Default.MonetizationOn,
                            iconTint = Color(0xFFD97706),
                            iconBg = Color(0xFFFEF3C7),
                            onView = { viewModel.navigateTo(AppNavDestination.LIVE_STOCK) },
                            onDownloadExcel = { viewModel.exportLiveStockExcel(context) }
                        )
                    }

                    // 6. Date-wise Transaction Report
                    item {
                        ReportCardItem(
                            title = "6. Date-wise Transaction Audit",
                            description = "Chronological transaction log with user IDs, timestamps, and stock revisions.",
                            icon = Icons.Default.DateRange,
                            iconTint = Color(0xFF0D9488),
                            iconBg = Color(0xFFCCFBF1),
                            onView = { viewModel.navigateTo(AppNavDestination.LOGS) },
                            onDownloadExcel = { viewModel.exportStockInExcel(context) }
                        )
                    }

                    // 7. Monthly Inventory Summary
                    item {
                        ReportCardItem(
                            title = "7. Monthly Inventory Summary",
                            description = "Aggregated monthly consumption, inflows, and balance trends for operational planning.",
                            icon = Icons.Default.CalendarMonth,
                            iconTint = NavyDark,
                            iconBg = Color(0xFFE2E8F0),
                            onView = { selectedReportTab = 1 },
                            onDownloadExcel = { viewModel.exportStockInExcel(context) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCardItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onView: () -> Unit,
    onDownloadExcel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = description, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onView,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("View Online", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onDownloadExcel,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
