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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardSummary
import com.example.data.model.StockInTransaction
import com.example.data.model.StockOutTransaction
import com.example.ui.components.SummaryStatCard
import com.example.ui.components.TransactionTypeBadge
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
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
fun HomeScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val stockInList by viewModel.allStockIn.collectAsState()
    val stockOutList by viewModel.allStockOut.collectAsState()

    // Combined recent transactions sorted by createdAt desc
    val recentTransactions = rememberRecentTransactions(stockInList, stockOutList)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 3: Dashboard Heading & Subtitle
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INVENTORY TRANSACTION",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyDark,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Manage your stock easily.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Low Stock Alert Badge
                    if (summary.lowStockCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AlertAmberLight,
                            modifier = Modifier.clickable { viewModel.navigateTo(AppNavDestination.LIVE_STOCK) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AlertAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${summary.lowStockCount} Low Stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertAmber
                                )
                            }
                        }
                    }
                }

                // Supabase Connection Status Banner
                Spacer(modifier = Modifier.height(10.dp))
                val supabaseStatus by viewModel.supabaseSyncStatus.collectAsState()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.testSupabaseConnection() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Supabase DB: bzzmtjoejpdfiqrzaqlg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyDark
                            )
                        }

                        Text(
                            text = when (val st = supabaseStatus) {
                                is com.example.data.remote.supabase.SupabaseSyncStatus.Connected -> if (st.tablesReady) "● Synchronized" else "● Setup Tables"
                                is com.example.data.remote.supabase.SupabaseSyncStatus.Syncing -> "● Syncing..."
                                is com.example.data.remote.supabase.SupabaseSyncStatus.Error -> "● Error"
                                else -> "● Connected"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (val st = supabaseStatus) {
                                is com.example.data.remote.supabase.SupabaseSyncStatus.Connected -> if (st.tablesReady) StockInGreen else AlertAmber
                                is com.example.data.remote.supabase.SupabaseSyncStatus.Syncing -> PrimaryBlue
                                else -> StockInGreen
                            }
                        )
                    }
                }
            }
        }

        // Section 3: Display 6 Dashboard Summary Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        title = "Total Products",
                        value = "${summary.totalProducts}",
                        subtitle = "Catalog items",
                        icon = Icons.Default.Category,
                        iconTint = PrimaryBlue,
                        iconBackground = PrimaryBlueLight,
                        modifier = Modifier.weight(1f),
                        testTag = "card_total_products"
                    )
                    SummaryStatCard(
                        title = "Available Stock",
                        value = String.format("%.0f", summary.totalAvailableStock),
                        subtitle = "Units on hand",
                        icon = Icons.Default.Inventory2,
                        iconTint = Color(0xFF0D9488),
                        iconBackground = Color(0xFFCCFBF1),
                        modifier = Modifier.weight(1f),
                        testTag = "card_available_stock"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        title = "Total Stock In",
                        value = String.format("%.0f", summary.totalStockIn),
                        subtitle = "Received to date",
                        icon = Icons.Default.ArrowDownward,
                        iconTint = StockInGreen,
                        iconBackground = StockInGreenLight,
                        modifier = Modifier.weight(1f),
                        testTag = "card_total_stock_in"
                    )
                    SummaryStatCard(
                        title = "Total Stock Out",
                        value = String.format("%.0f", summary.totalStockOut),
                        subtitle = "Dispatched to date",
                        icon = Icons.Default.ArrowUpward,
                        iconTint = StockOutRed,
                        iconBackground = StockOutRedLight,
                        modifier = Modifier.weight(1f),
                        testTag = "card_total_stock_out"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        title = "Inventory Valuation",
                        value = String.format("₹%.0f", summary.totalInventoryAmount),
                        subtitle = "Total stock worth",
                        icon = Icons.Default.MonetizationOn,
                        iconTint = Color(0xFFD97706),
                        iconBackground = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f),
                        testTag = "card_inventory_amount"
                    )
                    SummaryStatCard(
                        title = "Today's Activity",
                        value = "${summary.todayTransactionsCount}",
                        subtitle = "Stock transactions today",
                        icon = Icons.Default.Schedule,
                        iconTint = Color(0xFF7C3AED),
                        iconBackground = Color(0xFFEDE9FE),
                        modifier = Modifier.weight(1f),
                        testTag = "card_today_transactions"
                    )
                }
            }
        }

        // Quick Action Buttons
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "QUICK ACTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(AppNavDestination.STOCK_IN) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_stock_in"),
                            colors = ButtonDefaults.buttonColors(containerColor = StockInGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("STOCK IN (+)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.navigateTo(AppNavDestination.STOCK_OUT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_stock_out"),
                            colors = ButtonDefaults.buttonColors(containerColor = StockOutRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("STOCK OUT (-)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppNavDestination.LIVE_STOCK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_live_stock"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Stock", fontSize = 12.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppNavDestination.REPORTS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_reports"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reports", fontSize = 12.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Section 3: Recent Stock Transactions Table
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = "View All (${recentTransactions.size})",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppNavDestination.LOGS) }
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No stock transactions recorded yet.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Use Stock In or Stock Out buttons above to start logging inventory.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(recentTransactions.take(10)) { item ->
                RecentTransactionCard(item = item)
            }
        }
    }
}

data class UnifiedRecentItem(
    val id: String,
    val type: String, // IN or OUT
    val productName: String,
    val quantity: Double,
    val unit: String,
    val rate: Double,
    val totalAmount: Double,
    val date: String,
    val partyOrFirm: String,
    val resultingStock: Double,
    val timestamp: Long
)

@Composable
fun RecentTransactionCard(item: UnifiedRecentItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TransactionTypeBadge(type = item.type)
                Text(
                    text = item.date,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (item.type == "IN") "From: ${item.partyOrFirm}" else "Party: ${item.partyOrFirm}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "New/Remaining Stock: ${String.format("%.2f %s", item.resultingStock, item.unit)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (item.type == "IN") "+" else "-"} ${String.format("%.2f", item.quantity)} ${item.unit}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.type == "IN") StockInGreen else StockOutRed
                    )
                    Text(
                        text = "₹${String.format("%.2f", item.totalAmount)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}

private fun rememberRecentTransactions(
    stockIn: List<StockInTransaction>,
    stockOut: List<StockOutTransaction>
): List<UnifiedRecentItem> {
    val inItems = stockIn.map {
        UnifiedRecentItem(
            id = it.transactionId,
            type = "IN",
            productName = it.productName,
            quantity = it.quantity,
            unit = it.unit,
            rate = it.rate,
            totalAmount = it.totalAmount,
            date = it.date,
            partyOrFirm = it.firmName,
            resultingStock = it.newStock,
            timestamp = it.createdAt
        )
    }

    val outItems = stockOut.map {
        UnifiedRecentItem(
            id = it.transactionId,
            type = "OUT",
            productName = it.productName,
            quantity = it.quantity,
            unit = it.unit,
            rate = it.rate,
            totalAmount = it.totalAmount,
            date = it.date,
            partyOrFirm = it.partyName,
            resultingStock = it.remainingStock,
            timestamp = it.createdAt
        )
    }

    return (inItems + outItems).sortedByDescending { it.timestamp }
}
