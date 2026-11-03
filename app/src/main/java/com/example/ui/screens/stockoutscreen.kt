package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockOutTransaction
import com.example.ui.components.CalculationPreviewBox
import com.example.ui.components.ProductSearchDropdown
import com.example.ui.components.UnitDropdownSelector
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.StockOutRedLight
import com.example.ui.theme.StockOutRedText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.InventoryViewModel
import java.util.Calendar

@Composable
fun StockOutScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val form by viewModel.stockOutForm.collectAsState()
    val products by viewModel.products.collectAsState()
    val stockOutList by viewModel.allStockOut.collectAsState()

    var transactionToDelete by remember { mutableStateOf<StockOutTransaction?>(null) }

    // Date picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
            viewModel.updateStockOutDate(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Calculation values
    val qtyVal = form.quantity.toDoubleOrNull() ?: 0.0
    val rateVal = form.rate.toDoubleOrNull() ?: 0.0
    val totalAmount = qtyVal * rateVal
    val remainingStock = form.previousStock - qtyVal
    val isInsufficientStock = form.selectedProductId.isNotBlank() && qtyVal > form.previousStock

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STOCK OUT – REMOVE MATERIAL",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDark
                    )
                    Text(
                        text = "Dispatch aluminium profiles & goods to customer / site",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppNavDestination.REPORTS) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Report", fontSize = 12.sp)
                }
            }
        }

        // Section 6 VALIDATION: Insufficient Stock Warning Alert
        if (isInsufficientStock) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StockOutRedLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, StockOutRed, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Warning",
                            tint = StockOutRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "INSUFFICIENT STOCK WARNING!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StockOutRedText
                            )
                            Text(
                                text = "Requested quantity (${qtyVal} ${form.unit}) exceeds available stock (${form.previousStock} ${form.unit}). Negative inventory is strictly prohibited.",
                                fontSize = 11.sp,
                                color = StockOutRedText
                            )
                        }
                    }
                }
            }
        }

        // Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Party Name
                    OutlinedTextField(
                        value = form.partyName,
                        onValueChange = { viewModel.updateStockOutParty(it) },
                        label = { Text("1. Party / Customer / Project Name *") },
                        placeholder = { Text("e.g. Apex Builders / Metro Tower Site B") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stock_out_party_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        )
                    )

                    // 2. Product Name (Searchable dropdown)
                    ProductSearchDropdown(
                        products = products,
                        selectedProductId = form.selectedProductId,
                        onProductSelected = { viewModel.onSelectStockOutProduct(it) },
                        label = "2. Product Name *"
                    )

                    // 5 & 6. Previous Stock & HSM Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (form.selectedProductId.isNotBlank()) String.format("%.2f %s", form.previousStock, form.unit) else "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("5. Previous Stock") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = form.hsmCode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("6. HSM Code") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }

                    // 3 & 4. Quantity & Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = form.quantity,
                            onValueChange = { viewModel.updateStockOutQuantity(it) },
                            label = { Text("3. Quantity *") },
                            placeholder = { Text("e.g. 20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = isInsufficientStock,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stock_out_quantity_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        UnitDropdownSelector(
                            selectedUnit = form.unit,
                            onUnitSelected = { viewModel.updateStockOutUnit(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 7 & 8. Rate & Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = form.rate,
                            onValueChange = { viewModel.updateStockOutRate(it) },
                            label = { Text("7. Rate (₹/unit)") },
                            placeholder = { Text("e.g. 360") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stock_out_rate_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = form.date,
                            onValueChange = { viewModel.updateStockOutDate(it) },
                            label = { Text("8. Date") },
                            trailingIcon = {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", modifier = Modifier.size(18.dp))
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { datePickerDialog.show() },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }

                    // 9. Firm Name
                    OutlinedTextField(
                        value = form.firmName,
                        onValueChange = { viewModel.updateStockOutFirm(it) },
                        label = { Text("9. Firm / Branch Name *") },
                        placeholder = { Text("e.g. Metinex Aluminium Plant 1") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stock_out_firm_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        )
                    )

                    // Automatic Calculations Box
                    CalculationPreviewBox(
                        previousStock = form.previousStock,
                        transactionQty = qtyVal,
                        rate = rateVal,
                        totalAmount = totalAmount,
                        resultingStock = remainingStock,
                        isStockIn = false,
                        unit = form.unit
                    )

                    // Action buttons: REMOVE STOCK (OUT), RESET
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.submitStockOut() },
                            enabled = !isInsufficientStock,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("btn_remove_stock_out"),
                            colors = ButtonDefaults.buttonColors(containerColor = StockOutRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REMOVE STOCK (OUT)", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetStockOutForm() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_reset_stock_out"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESET", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Recent Stock Out Entries with Reversal action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT STOCK OUT LOGS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                TextButton(onClick = { viewModel.exportStockOutExcel(context) }) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (stockOutList.isEmpty()) {
            item {
                Text(
                    text = "No stock out records yet.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(stockOutList.take(8)) { item ->
                StockOutItemRow(
                    item = item,
                    onDelete = { transactionToDelete = item }
                )
            }
        }
    }

    // Confirmation dialog for deleting Stock Out
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete & Restore Stock Out?") },
            text = {
                Text(
                    "Are you sure you want to delete transaction ${tx.transactionId} for party '${tx.partyName}'? This will restore ${tx.quantity} ${tx.unit} back to available stock."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStockOut(tx.transactionId)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StockOutRed)
                ) {
                    Text("Delete & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StockOutItemRow(
    item: StockOutTransaction,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = item.date,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StockOutRedText
                    )
                    Text(text = "•", fontSize = 11.sp, color = TextMuted)
                    Text(text = "Party: ${item.partyName}", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Prev: ${item.previousStock} → Remaining: ${item.remainingStock} ${item.unit} | ₹${item.rate}/${item.unit}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-${item.quantity} ${item.unit}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StockOutRed
                )
                Text(
                    text = "₹${String.format("%.2f", item.totalAmount)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete transaction",
                        tint = StockOutRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
