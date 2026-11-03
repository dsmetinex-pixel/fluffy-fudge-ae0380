package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
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
import com.example.ui.theme.StockInGreenText
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.StockOutRedLight
import com.example.ui.theme.StockOutRedText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val UNIT_OPTIONS = listOf("PC", "KG", "ROLL", "SQFT", "PKT", "SET", "BOX", "PAIR", "BAG")
val CATEGORY_OPTIONS = listOf("Extrusions", "Channels", "Architectural", "Hardware", "Sheets", "Accessories", "Powder Coated")

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionTypeBadge(type: String) {
    val isStockIn = type.contains("IN", ignoreCase = true)
    val bgColor = if (isStockIn) StockInGreenLight else StockOutRedLight
    val textColor = if (isStockIn) StockInGreenText else StockOutRedText
    val label = if (isStockIn) "STOCK IN (+)" else "STOCK OUT (-)"

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StockStatusBadge(
    availableStock: Double,
    minimumStock: Double
) {
    val (label, bg, fg) = when {
        availableStock <= 0 -> Triple("OUT OF STOCK", StockOutRedLight, StockOutRedText)
        availableStock <= minimumStock -> Triple("LOW STOCK", AlertAmberLight, AlertAmber)
        else -> Triple("IN STOCK", StockInGreenLight, StockInGreenText)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ProductSearchDropdown(
    products: List<Product>,
    selectedProductId: String,
    onProductSelected: (Product) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Product *"
) {
    var expanded by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf("") }
    val selectedProduct = products.find { it.productId == selectedProductId }

    val filtered = remember(products, filterText) {
        if (filterText.isBlank()) products
        else products.filter {
            it.productName.contains(filterText, ignoreCase = true) ||
            it.hsmCode.contains(filterText, ignoreCase = true) ||
            it.category.contains(filterText, ignoreCase = true)
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedProduct?.let { "${it.productName} (${it.unit}) - Stock: ${it.currentStock}" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Search & select aluminium profile or item...") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Open dropdown")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .testTag("product_dropdown_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = BorderLight
            ),
            shape = RoundedCornerShape(8.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(CardBackground)
        ) {
            // Search field inside dropdown
            Box(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    placeholder = { Text("Filter products or HSM code...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (filterText.isNotEmpty()) {
                            IconButton(onClick = { filterText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            if (filtered.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No matching products found", color = TextMuted) },
                    onClick = { }
                )
            } else {
                filtered.forEach { prod ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = prod.productName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "HSM: ${prod.hsmCode}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "•",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "Available: ${prod.currentStock} ${prod.unit}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (prod.currentStock <= prod.minimumStock) StockOutRed else StockInGreenText
                                    )
                                    Text(
                                        text = "•",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "₹${prod.ratePerUnit}/${prod.unit}",
                                        fontSize = 11.sp,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        },
                        onClick = {
                            onProductSelected(prod)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UnitDropdownSelector(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit *") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .testTag("unit_dropdown_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = BorderLight
            ),
            shape = RoundedCornerShape(8.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBackground)
        ) {
            UNIT_OPTIONS.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit, fontWeight = FontWeight.Medium) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CalculationPreviewBox(
    previousStock: Double,
    transactionQty: Double,
    rate: Double,
    totalAmount: Double,
    resultingStock: Double,
    isStockIn: Boolean,
    unit: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isStockIn) Color(0xFFF0FDF4) else Color(0xFFFEF2F2))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "AUTOMATIC TRANSACTION CALCULATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isStockIn) StockInGreenText else StockOutRedText,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CalcItem(
                    label = "Previous Stock",
                    value = String.format("%.2f %s", previousStock, unit)
                )
                CalcItem(
                    label = if (isStockIn) "Stock In (+)" else "Stock Out (-)",
                    value = String.format("%.2f %s", transactionQty, unit),
                    valueColor = if (isStockIn) StockInGreen else StockOutRed
                )
                CalcItem(
                    label = "Rate Per Unit",
                    value = String.format("₹%.2f", rate)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CalcItem(
                    label = "Total Amount (Qty × Rate)",
                    value = String.format("₹%.2f", totalAmount),
                    valueColor = PrimaryBlue,
                    isBold = true
                )
                CalcItem(
                    label = if (isStockIn) "New Stock" else "Remaining Stock",
                    value = String.format("%.2f %s", resultingStock, unit),
                    valueColor = if (resultingStock <= 0) StockOutRed else if (isStockIn) StockInGreenText else TextPrimary,
                    isBold = true
                )
            }
        }
    }
}

@Composable
private fun CalcItem(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    isBold: Boolean = false
) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
