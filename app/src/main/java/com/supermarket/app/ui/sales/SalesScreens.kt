package com.supermarket.app.ui.sales

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarket.app.data.models.*
import com.supermarket.app.ui.inventory.smOutlinedColors
import com.supermarket.app.ui.theme.SMColors
import java.text.SimpleDateFormat
import java.util.*

// ============================
// NEW SALE SCREEN (POS)
// ============================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    onBack: () -> Unit,
    onSaleComplete: () -> Unit,
    viewModel: NewSaleViewModel = hiltViewModel()
) {
    val cartItems   by viewModel.cartItems.collectAsState()
    val results     by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val subtotal    by viewModel.subtotal.collectAsState()
    val total       by viewModel.total.collectAsState()
    val discount    by viewModel.discount.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()

    var showCheckout   by remember { mutableStateOf(false) }
    var customerName   by remember { mutableStateOf("") }
    var paidAmount     by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }
    var discountText   by remember { mutableStateOf("") }
    var showSuccess    by remember { mutableStateOf(false) }
    var changeAmount   by remember { mutableStateOf(0.0) }

    if (showSuccess) {
        SaleSuccessDialog(total, changeAmount) { onSaleComplete() }
    }

    Column(Modifier.fillMaxSize().background(SMColors.BgDeep)) {

        // Search bar
        OutlinedTextField(
            value = searchQuery, onValueChange = viewModel::onSearch,
            placeholder = { Text("ابحث عن منتج أو امسح الباركود...") },
            leadingIcon  = { Icon(Icons.Outlined.Search, null, tint = SMColors.TextSecondary) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty())
                        IconButton({ viewModel.onSearch("") }) {
                            Icon(Icons.Filled.Clear, null, tint = SMColors.TextSecondary)
                        }
                    Icon(Icons.Outlined.QrCodeScanner, null, tint = SMColors.AccentCyan,
                        modifier = Modifier.padding(end = 8.dp))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape  = RoundedCornerShape(16.dp), colors = smOutlinedColors(), singleLine = true
        )

        // Search results dropdown
        if (results.isNotEmpty() && searchQuery.isNotEmpty()) {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp).heightIn(max = 200.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
                border = BorderStroke(1.dp, SMColors.BgCardBorder)
            ) {
                LazyColumn {
                    items(results) { product ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { viewModel.addToCart(product); viewModel.onSearch("") }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(product.category.emoji, fontSize = 20.sp)
                                Column {
                                    Text(product.name, color = SMColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("${product.sellPrice} ر/${product.unit} • متبقي: ${product.quantity}",
                                        color = SMColors.TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Icon(Icons.Filled.AddCircle, null, tint = SMColors.Primary, modifier = Modifier.size(28.dp))
                        }
                        Divider(color = SMColors.BgCardBorder, thickness = 0.5.dp)
                    }
                }
            }
        }

        // Cart
        if (cartItems.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 60.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("السلة فارغة", color = SMColors.TextMuted, fontSize = 16.sp)
                    Text("ابحث عن منتج لإضافته", color = SMColors.TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            Text(
                "المنتجات (${cartItems.size})",
                color = SMColors.TextSecondary, fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyColumn(
                Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(cartItems, key = { it.productId }) { item ->
                    CartRow(item,
                        onIncrease = { viewModel.increaseQty(item.productId) },
                        onDecrease = { viewModel.decreaseQty(item.productId) },
                        onRemove   = { viewModel.removeFromCart(item.productId) }
                    )
                }
            }
        }

        // Totals bottom sheet
        if (cartItems.isNotEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = SMColors.BgCard)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Discount row
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it; viewModel.setDiscount(it.toDoubleOrNull() ?: 0.0) },
                            label = { Text("خصم (ريال)", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = smOutlinedColors(), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            if (discount > 0) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("خصم:", color = SMColors.TextSecondary, fontSize = 12.sp)
                                Text("-${"%.2f".format(discount)} ر", color = SMColors.AccentYellow, fontSize = 12.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("الإجمالي:", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${"%.2f".format(total)} ر", color = SMColors.Primary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }

                    // Payment method chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PaymentMethod.values().toList()) { method ->
                            FilterChip(
                                selected = selectedPayment == method,
                                onClick  = { selectedPayment = method },
                                label    = { Text("${method.icon} ${method.nameAr}", fontSize = 12.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SMColors.Primary.copy(0.2f),
                                    selectedLabelColor     = SMColors.Primary
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showCheckout = true },
                        Modifier.fillMaxWidth().height(52.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("إتمام البيع • ${"%.2f".format(total)} ر",
                            color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    // Checkout dialog
    if (showCheckout) {
        AlertDialog(
            onDismissRequest = { showCheckout = false },
            containerColor   = SMColors.BgCard,
            title = { Text("تأكيد البيع", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = customerName, onValueChange = { customerName = it },
                        label = { Text("اسم العميل (اختياري)") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                    OutlinedTextField(value = paidAmount, onValueChange = { paidAmount = it },
                        label = { Text("المبلغ المدفوع *") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    val change = (paidAmount.toDoubleOrNull() ?: 0.0) - total
                    if (change >= 0)
                        Text("الباقي: ${"%.2f".format(change)} ر", color = SMColors.Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    else if (paidAmount.isNotEmpty())
                        Text("المبلغ غير كافٍ!", color = SMColors.Error, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paid = paidAmount.toDoubleOrNull() ?: total
                        changeAmount = (paid - total).coerceAtLeast(0.0)
                        viewModel.completeSale(customerName, paid, selectedPayment) {
                            showCheckout = false; showSuccess = true
                        }
                    },
                    colors  = ButtonDefaults.buttonColors(containerColor = SMColors.Primary),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("تأكيد", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton({ showCheckout = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }
}

@Composable
fun CartRow(item: SaleItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgSurface),
        border = BorderStroke(1.dp, SMColors.BgCardBorder)
    ) {
        Row(Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.productName, color = SMColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("${item.unitPrice} ر × ${item.quantity} ${item.unit}", color = SMColors.TextSecondary, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${"%.2f".format(item.total)} ر", color = SMColors.Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                IconButton(onDecrease, Modifier.size(30.dp)) { Icon(Icons.Filled.RemoveCircle, null, tint = SMColors.Warning, modifier = Modifier.size(20.dp)) }
                Text("${item.quantity.toInt()}", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                IconButton(onIncrease, Modifier.size(30.dp)) { Icon(Icons.Filled.AddCircle, null, tint = SMColors.Primary, modifier = Modifier.size(20.dp)) }
                IconButton(onRemove, Modifier.size(30.dp)) { Icon(Icons.Filled.Close, null, tint = SMColors.Error, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@Composable
fun SaleSuccessDialog(total: Double, change: Double, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SMColors.BgCard,
        title = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✅", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("تمت عملية البيع!", color = SMColors.Primary, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("الإجمالي: ${"%.2f".format(total)} ر", color = SMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (change > 0) Text("الباقي: ${"%.2f".format(change)} ر", color = SMColors.AccentYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)) {
                Text("حسناً", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ============================
// SALES HISTORY SCREEN
// ============================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNewSale: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val sales       by viewModel.sales.collectAsState()
    val totalToday  by viewModel.totalToday.collectAsState()
    val totalMonth  by viewModel.totalMonth.collectAsState()
    val dateFormat  = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    var selectedSale by remember { mutableStateOf<Sale?>(null) }

    LazyColumn(
        Modifier.fillMaxSize().background(SMColors.BgDeep),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryTile("اليوم", "${"%.2f".format(totalToday)} ر", SMColors.Primary, Modifier.weight(1f))
                SummaryTile("الشهر", "${"%.0f".format(totalMonth)} ر", SMColors.AccentCyan, Modifier.weight(1f))
            }
        }
        item {
            Text("آخر الفواتير", color = SMColors.TextSecondary, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }
        if (sales.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧾", fontSize = 48.sp)
                        Spacer(Modifier.height(10.dp))
                        Text("لا توجد فواتير بعد", color = SMColors.TextMuted)
                    }
                }
            }
        }
        items(sales, key = { it.id }) { sale ->
            SaleCard(sale, dateFormat) { selectedSale = sale }
        }
    }

    selectedSale?.let { sale ->
        SaleDetailDialog(sale, dateFormat) { selectedSale = null }
    }
}

@Composable
fun SummaryTile(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.1f)),
        border = BorderStroke(1.dp, color.copy(0.3f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(label, color = SMColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun SaleCard(sale: Sale, dateFormat: SimpleDateFormat, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
        border = BorderStroke(1.dp, SMColors.BgCardBorder)
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(SMColors.Primary.copy(0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Receipt, null, tint = SMColors.Primary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(sale.invoiceNumber, color = SMColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (sale.customerName.isNotEmpty()) Text(sale.customerName, color = SMColors.TextSecondary, fontSize = 11.sp)
                    Text("${sale.items.size} منتج • ${sale.paymentMethod.nameAr} • ${dateFormat.format(Date(sale.createdAt))}",
                        color = SMColors.TextMuted, fontSize = 10.sp)
                }
            }
            Text("${"%.2f".format(sale.total)} ر", color = SMColors.Primary, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun SaleDetailDialog(sale: Sale, dateFormat: SimpleDateFormat, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = SMColors.BgCard,
        title = { Text(sale.invoiceNumber, color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(dateFormat.format(Date(sale.createdAt)), color = SMColors.TextMuted, fontSize = 11.sp)
                if (sale.customerName.isNotEmpty())
                    Text("العميل: ${sale.customerName}", color = SMColors.TextSecondary, fontSize = 13.sp)
                Divider(color = SMColors.BgCardBorder)
                sale.items.forEach { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} × ${item.quantity.toInt()} ${item.unit}",
                            color = SMColors.TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("${"%.2f".format(item.total)} ر", color = SMColors.Primary, fontSize = 13.sp)
                    }
                }
                Divider(color = SMColors.BgCardBorder)
                if (sale.discount > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("خصم", color = SMColors.AccentYellow)
                    Text("-${"%.2f".format(sale.discount)} ر", color = SMColors.AccentYellow)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الإجمالي", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${"%.2f".format(sale.total)} ر", color = SMColors.Primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text("الكاشير: ${sale.cashierName}", color = SMColors.TextSecondary, fontSize = 12.sp)
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("إغلاق", color = SMColors.Primary) } }
    )
}
