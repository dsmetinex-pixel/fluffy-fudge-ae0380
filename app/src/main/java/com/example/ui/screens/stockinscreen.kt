package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
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
import com.example.data.model.StockInTransaction
import com.example.ui.components.CalculationPreviewBox
import com.example.ui.components.ProductSearchDropdown
import com.example.ui.components.UnitDropdownSelector
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockInGreenText
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.InventoryViewModel
import java.util.Calendar

@Composable
fun StockInScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val form by viewModel.stockInForm.collectAsState()
    val products by viewModel.products.collectAsState()
    val stockInList by viewModel.allStockIn.collectAsState()

    var transactionToDelete by remember { mutableStateOf<StockInTransaction?>(null) }

    // Date picker setup
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
            viewModel.updateStockInDate(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Live calculation values
    val qtyVal = form.quantity.toDoubleOrNull() ?: 0.0
    val rateVal = form.rate.toDoubleOrNull() ?: 0.0
    val totalAmount = qtyVal * rateVal
    val newStock = form.previousStock + qtyVal

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
                        text = "STOCK IN – ADD MATERIAL",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDark
                    )
                    Text(
                        text = "Receive raw material & stock into Metinex inventory",
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
                    // 1. Product Name Dropdown (auto fetch HSM, Unit, Rate, Prev Stock)
                    ProductSearchDropdown(
                        products = products,
                        selectedProductId = form.selectedProductId,
                        onProductSelected = { viewModel.onSelectStockInProduct(it) },
                        label = "1. Product Name *"
                    )

                    // 4 & 5. Previous Stock & HSM Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (form.selectedProductId.isNotBlank()) String.format("%.2f %s", form.previousStock, form.unit) else "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("4. Previous Stock") },
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
                            label = { Text("5. HSM Code") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }

                    // 2 & 3. Quantity & Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = form.quantity,
                            onValueChange = { viewModel.updateStockInQuantity(it) },
                            label = { Text("2. Quantity *") },
                            placeholder = { Text("e.g. 50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stock_in_quantity_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        UnitDropdownSelector(
                            selectedUnit = form.unit,
                            onUnitSelected = { viewModel.updateStockInUnit(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 6 & 7. Rate & Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = form.rate,
                            onValueChange = { viewModel.updateStockInRate(it) },
                            label = { Text("6. Rate (₹/unit)") },
                            placeholder = { Text("e.g. 340") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stock_in_rate_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = form.date,
                            onValueChange = { viewModel.updateStockInDate(it) },
                            label = { Text("7. Date") },
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

                    // 8. Firm Name
                    OutlinedTextField(
                        value = form.firmName,
                        onValueChange = { viewModel.updateStockInFirm(it) },
                        label = { Text("8. Firm / Supplier Name *") },
                        placeholder = { Text("e.g. Hindalco Industries / Metinex Unit 1") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stock_in_firm_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        )
                    )

                    // Calculated Fields Preview Box
                    CalculationPreviewBox(
                        previousStock = form.previousStock,
                        transactionQty = qtyVal,
                        rate = rateVal,
                        totalAmount = totalAmount,
                        resultingStock = newStock,
                        isStockIn = true,
                        unit = form.unit
                    )

                    // Buttons: ADD STOCK (IN), RESET
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.submitStockIn() },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("btn_add_stock_in"),
                            colors = ButtonDefaults.buttonColors(containerColor = StockInGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADD STOCK (IN)", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetStockInForm() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_reset_stock_in"),
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

        // Section: Recent Stock In Entries with Edit & Delete actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT STOCK IN LOGS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                TextButton(onClick = { viewModel.exportStockInExcel(context) }) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (stockInList.isEmpty()) {
            item {
                Text(
                    text = "No stock in records yet. Add materials above.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(stockInList.take(8)) { item ->
                StockInItemRow(
                    item = item,
                    onDelete = { transactionToDelete = item }
                )
            }
        }
    }

    // Confirmation dialog for deleting Stock In
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete & Reverse Stock In?") },
            text = {
                Text(
                    "Are you sure you want to delete transaction ${tx.transactionId} for ${tx.productName}? This will automatically deduct ${tx.quantity} ${tx.unit} from the product stock."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStockIn(tx.transactionId)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StockOutRed)
                ) {
                    Text("Delete & Reverse")
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
fun StockInItemRow(
    item: StockInTransaction,
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
                        color = StockInGreenText
                    )
                    Text(text = "•", fontSize = 11.sp, color = TextMuted)
                    Text(text = item.firmName, fontSize = 11.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Prev: ${item.previousStock} → New: ${item.newStock} ${item.unit} | ₹${item.rate}/${item.unit}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${item.quantity} ${item.unit}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StockInGreen
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
