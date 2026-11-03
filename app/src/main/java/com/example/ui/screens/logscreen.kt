package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLog
import com.example.ui.components.TransactionTypeBadge
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.allLogs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var actionFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = remember(logs, searchQuery, actionFilter) {
        logs.filter { log ->
            val matchesSearch = searchQuery.isBlank() ||
                log.action.contains(searchQuery, ignoreCase = true) ||
                log.productName.contains(searchQuery, ignoreCase = true) ||
                log.userName.contains(searchQuery, ignoreCase = true) ||
                log.description.contains(searchQuery, ignoreCase = true)

            val matchesAction = when (actionFilter) {
                "IN" -> log.transactionType == "IN"
                "OUT" -> log.transactionType == "OUT"
                "PRODUCT" -> log.transactionType == "PRODUCT"
                else -> true
            }

            matchesSearch && matchesAction
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "LOG – TRANSACTION HISTORY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyDark
                )
                Text(
                    text = "Permanent audit log of all stock movements, creations, and revisions",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Search & Filter
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search logs by user, product, or action...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = actionFilter == "ALL",
                            onClick = { actionFilter = "ALL" },
                            label = { Text("All (${logs.size})") }
                        )
                        FilterChip(
                            selected = actionFilter == "IN",
                            onClick = { actionFilter = "IN" },
                            label = { Text("Stock In") }
                        )
                        FilterChip(
                            selected = actionFilter == "OUT",
                            onClick = { actionFilter = "OUT" },
                            label = { Text("Stock Out") }
                        )
                        FilterChip(
                            selected = actionFilter == "PRODUCT",
                            onClick = { actionFilter = "PRODUCT" },
                            label = { Text("Product Updates") }
                        )
                    }
                }
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No activity logs found.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredLogs) { log ->
                ActivityLogCard(log = log)
            }
        }
    }
}

@Composable
fun ActivityLogCard(log: ActivityLog) {
    val timeFormatted = remember(log.timestamp) {
        SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    val typeColor = when (log.transactionType) {
        "IN" -> StockInGreen
        "OUT" -> StockOutRed
        "PRODUCT" -> PrimaryBlue
        else -> TextSecondary
    }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = log.action.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "by ${log.userName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Text(
                    text = timeFormatted,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (log.productName.isNotBlank()) {
                Text(
                    text = log.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Text(
                text = log.description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            if (log.quantity > 0.0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quantity: ${log.quantity}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Text(
                        text = "Prev Stock: ${log.previousStock} → New: ${log.updatedStock}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
