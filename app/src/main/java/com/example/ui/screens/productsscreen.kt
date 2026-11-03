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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
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
import com.example.data.model.Product
import com.example.ui.components.CATEGORY_OPTIONS
import com.example.ui.components.StockStatusBadge
import com.example.ui.components.UnitDropdownSelector
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val productForm by viewModel.productForm.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productDetailsToShow by remember { mutableStateOf<Product?>(null) }

    val filtered = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            (searchQuery.isBlank() ||
             p.productName.contains(searchQuery, ignoreCase = true) ||
             p.hsmCode.contains(searchQuery, ignoreCase = true) ||
             p.category.contains(searchQuery, ignoreCase = true)) &&
            (selectedCategory == "ALL" || p.category.equals(selectedCategory, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 9: Heading & Subtitle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MY PRODUCTS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDark
                    )
                    Text(
                        text = "Manage product catalog",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { viewModel.openAddProductDialog() },
                        modifier = Modifier.testTag("btn_add_product"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportProductsExcel(context) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
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
                        placeholder = { Text("Search by name, HSM code, or category...", fontSize = 12.sp) },
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
                }
            }
        }

        // Section 9: Product Catalog List Cards with Actions
        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No products match the search query.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            itemsIndexed(filtered) { index, product ->
                ProductCatalogCard(
                    slNo = index + 1,
                    product = product,
                    onEdit = { viewModel.openEditProductDialog(product) },
                    onDelete = { productToDelete = product },
                    onViewDetails = { productDetailsToShow = product }
                )
            }
        }
    }

    // Add / Edit Product Modal Bottom Sheet or Dialog
    productForm?.let { form ->
        ProductFormDialog(
            form = form,
            onDismiss = { viewModel.closeProductDialog() },
            onUpdateField = { viewModel.updateProductFormField(it) },
            onSave = { viewModel.saveProductFromDialog() }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to delete '${prod.productName}'? This product will be removed from your catalog.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod.productId)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StockOutRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Product Details Modal
    productDetailsToShow?.let { prod ->
        AlertDialog(
            onDismissRequest = { productDetailsToShow = null },
            title = { Text(prod.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Product ID: ${prod.productId}", fontSize = 12.sp, color = TextSecondary)
                    Text("HSM Code: ${prod.hsmCode}", fontSize = 12.sp, color = TextSecondary)
                    Text("Category: ${prod.category}", fontSize = 12.sp, color = TextSecondary)
                    Text("Unit: ${prod.unit}", fontSize = 12.sp, color = TextSecondary)
                    Text("Rate: ₹${prod.ratePerUnit} / ${prod.unit}", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    Text("Opening Stock: ${prod.openingStock} ${prod.unit}", fontSize = 12.sp)
                    Text("Current Available Stock: ${prod.currentStock} ${prod.unit}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Min Stock Alert: ${prod.minimumStock} ${prod.unit}", fontSize = 12.sp)
                    if (prod.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Description: ${prod.description}", fontSize = 12.sp, color = TextPrimary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { productDetailsToShow = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ProductCatalogCard(
    slNo: Int,
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9)) {
                        Text("#$slNo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Text(
                        text = product.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                    Text(text = "•", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "HSM: ${product.hsmCode}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                StockStatusBadge(
                    availableStock = product.currentStock,
                    minimumStock = product.minimumStock
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = product.productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rate: ₹${product.ratePerUnit} / ${product.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Opening: ${product.openingStock} ${product.unit} | Available: ${product.currentStock} ${product.unit}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onViewDetails, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "View Details", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = NavyLight, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = StockOutRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    form: com.example.ui.viewmodel.ProductFormState,
    onDismiss: () -> Unit,
    onUpdateField: ((com.example.ui.viewmodel.ProductFormState) -> com.example.ui.viewmodel.ProductFormState) -> Unit,
    onSave: () -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (form.isEditing) "UPDATE PRODUCT" else "ADD NEW PRODUCT",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyDark
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = form.productName,
                        onValueChange = { str -> onUpdateField { it.copy(productName = str) } },
                        label = { Text("1. Product Name *") },
                        placeholder = { Text("e.g. Aluminium Casement Profile") },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = form.hsmCode,
                            onValueChange = { str -> onUpdateField { it.copy(hsmCode = str) } },
                            label = { Text("2. HSM Code *") },
                            placeholder = { Text("76042100") },
                            modifier = Modifier.weight(1f).testTag("product_hsm_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = form.category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("3. Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                CATEGORY_OPTIONS.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            onUpdateField { it.copy(category = cat) }
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UnitDropdownSelector(
                            selectedUnit = form.unit,
                            onUnitSelected = { u -> onUpdateField { it.copy(unit = u) } },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = form.ratePerUnit,
                            onValueChange = { str -> onUpdateField { it.copy(ratePerUnit = str) } },
                            label = { Text("5. Rate / Unit *") },
                            placeholder = { Text("340.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = form.openingStock,
                            onValueChange = { str -> onUpdateField { it.copy(openingStock = str) } },
                            label = { Text("6. Opening Stock") },
                            placeholder = { Text("100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = form.minimumStock,
                            onValueChange = { str -> onUpdateField { it.copy(minimumStock = str) } },
                            label = { Text("7. Min Stock Alert") },
                            placeholder = { Text("20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = form.description,
                        onValueChange = { str -> onUpdateField { it.copy(description = str) } },
                        label = { Text("8. Description (Optional)") },
                        placeholder = { Text("Specification or alloy details") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (form.isEditing) "UPDATE PRODUCT" else "SAVE PRODUCT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("RESET / CANCEL")
            }
        }
    )
}
