package com.supermarket.app.ui.inventory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarket.app.data.models.Product
import com.supermarket.app.data.models.ProductCategory
import com.supermarket.app.ui.theme.SMColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    showExpiring: Boolean = false,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val products       by viewModel.products.collectAsState()
    val searchQuery    by viewModel.searchQuery.collectAsState()
    val selectedCat    by viewModel.selectedCategory.collectAsState()
    var deleteTarget   by remember { mutableStateOf<Product?>(null) }

    val displayed = if (showExpiring) {
        val in7Days = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000
        products.filter { it.expiryDate in 1..in7Days }
    } else products

    Column(
        Modifier.fillMaxSize().background(SMColors.BgDeep).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search
        OutlinedTextField(
            value = searchQuery, onValueChange = viewModel::onSearchChange,
            placeholder = { Text("بحث بالاسم أو الباركود...") },
            leadingIcon  = { Icon(Icons.Outlined.Search, null, tint = SMColors.TextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty())
                    IconButton({ viewModel.onSearchChange("") }) {
                        Icon(Icons.Filled.Clear, null, tint = SMColors.TextSecondary)
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = smOutlinedColors(),
            singleLine = true
        )

        // Category chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCat == null,
                    onClick  = { viewModel.onCategoryFilter(null) },
                    label    = { Text("الكل") },
                    colors   = smChipColors()
                )
            }
            items(ProductCategory.values().toList()) { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick  = { viewModel.onCategoryFilter(cat) },
                    label    = { Text("${cat.emoji} ${cat.nameAr}") },
                    colors   = smChipColors()
                )
            }
        }

        // Stats strip
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill("الكل",    "${displayed.size}",        SMColors.AccentCyan,   Modifier.weight(1f))
            StatPill("منخفض",   "${displayed.count { it.quantity <= it.minQuantity && it.quantity > 0 }}", SMColors.Warning, Modifier.weight(1f))
            StatPill("نافد",    "${displayed.count { it.quantity == 0 }}", SMColors.Error, Modifier.weight(1f))
        }

        // List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (displayed.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📦", fontSize = 48.sp)
                            Spacer(Modifier.height(10.dp))
                            Text("لا توجد منتجات", color = SMColors.TextMuted, fontSize = 15.sp)
                        }
                    }
                }
            }
            items(displayed, key = { it.id }) { product ->
                ProductCard(
                    product  = product,
                    onEdit   = { onEditProduct(product.id) },
                    onDelete = { deleteTarget = product }
                )
            }
        }
    }

    // FAB handled by TopAppBar action - here we also add bottom FAB
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        ExtendedFloatingActionButton(
            onClick          = onAddProduct,
            containerColor   = SMColors.Primary,
            contentColor     = Color.Black,
            modifier         = Modifier.padding(24.dp),
            icon             = { Icon(Icons.Filled.Add, null) },
            text             = { Text("إضافة منتج", fontWeight = FontWeight.Bold) }
        )
    }

    // Delete dialog
    deleteTarget?.let { product ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = SMColors.BgCard,
            title = { Text("حذف المنتج", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text  = { Text("هل تريد حذف \"${product.name}\"؟", color = SMColors.TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteProduct(product.id); deleteTarget = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = SMColors.Error)
                ) { Text("حذف", color = Color.White) }
            },
            dismissButton = {
                TextButton({ deleteTarget = null }) { Text("إلغاء", color = SMColors.TextSecondary) }
            }
        )
    }
}

@Composable
fun ProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    val catColor = Color(product.category.color)
    val stockColor = when {
        product.quantity == 0               -> SMColors.Error
        product.quantity <= product.minQuantity -> SMColors.Warning
        else                                -> SMColors.Primary
    }
    val expiryWarning = product.expiryDate > 0 &&
        product.expiryDate < System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000

    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
        border = BorderStroke(1.dp, if (expiryWarning) SMColors.Warning.copy(0.4f) else SMColors.BgCardBorder)
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Category emoji box
            Box(
                Modifier.size(50.dp).background(catColor.copy(0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Text(product.category.emoji, fontSize = 22.sp) }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(product.name, color = SMColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (product.barcode.isNotEmpty())
                    Text(product.barcode, color = SMColors.TextMuted, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ش: ${product.buyPrice} ر", color = SMColors.TextSecondary, fontSize = 11.sp)
                    Text("ب: ${product.sellPrice} ر", color = SMColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                if (expiryWarning) {
                    val fmt = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    Text("⚠️ ينتهي: ${fmt.format(Date(product.expiryDate))}",
                        color = SMColors.Warning, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${product.quantity}", color = stockColor, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(product.unit, color = SMColors.TextMuted, fontSize = 10.sp)
                Row {
                    IconButton(onEdit, Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Edit, null, tint = SMColors.AccentCyan, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onDelete, Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Delete, null, tint = SMColors.Error, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = color.copy(0.1f)),
        border   = BorderStroke(1.dp, color.copy(0.25f))
    ) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(label, color = SMColors.TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun smOutlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor    = SMColors.Primary,
    unfocusedBorderColor  = SMColors.BgCardBorder,
    focusedLabelColor     = SMColors.Primary,
    unfocusedLabelColor   = SMColors.TextSecondary,
    cursorColor           = SMColors.Primary,
    focusedTextColor      = SMColors.TextPrimary,
    unfocusedTextColor    = SMColors.TextPrimary,
    focusedContainerColor = SMColors.BgCard,
    unfocusedContainerColor = SMColors.BgSurface
)

@Composable
fun smChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = SMColors.Primary.copy(0.2f),
    selectedLabelColor     = SMColors.Primary,
    selectedLeadingIconColor = SMColors.Primary,
    containerColor         = SMColors.BgCard,
    labelColor             = SMColors.TextSecondary
)
