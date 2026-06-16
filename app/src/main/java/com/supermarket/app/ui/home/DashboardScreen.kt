package com.supermarket.app.ui.home
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.* import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.supermarket.app.data.models.DashboardStats
import com.supermarket.app.data.models.ProductCategory
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.ui.navigation.Screen
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.layout.ContentScale
import com.supermarket.app.ui.sales.SalesViewModel
import com.supermarket.app.ui.sales.NewSaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val recentSales by viewModel.recentSales.collectAsState()
    val weeklySales by viewModel.weeklySales.collectAsState()
    val recentProducts by viewModel.recentProducts.collectAsState(initial = emptyList())

    val context = LocalContext.current
    val barcodeScanner = remember { GmsBarcodeScanning.getClient(context) }
    val showNotFoundDialog by viewModel.showNotFoundDialog.collectAsState()
    val scannedBarcode by viewModel.scannedBarcode.collectAsState()

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale("ar")) }
    val dateFormat = remember { SimpleDateFormat("EEEE، d MMMM", Locale("ar")) }
    val now = remember { Date() }

    var saleToEdit by remember { mutableStateOf<com.supermarket.app.data.models.Sale?>(null) }

    if (showNotFoundDialog && scannedBarcode != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNotFoundDialog() },
            containerColor = SMColors.BgCard,
            title = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, null, tint = SMColors.Primary)
                    Text("صنف غير مسجل", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text("الباركود ($scannedBarcode) غير موجود في النظام. هل تريد الانتقال لتسجيله الآن؟", color = SMColors.TextSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissNotFoundDialog()
                        onNavigate(Screen.AddProduct.createRoute(barcode = scannedBarcode))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)
                ) { Text("تسجيل الآن", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissNotFoundDialog() }) { Text("إلغاء", color = SMColors.TextMuted) }
            }
        )
    }

    val isLightMode = !isSystemInDarkTheme()
    val screenBackground = if (isLightMode) Color(0xFFF4F6F8) else SMColors.BgDeep

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(screenBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("مرحباً العباسي 👋", color = SMColors.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(dateFormat.format(now), color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            barcodeScanner.startScan()
                                .addOnSuccessListener { res ->
                                    res.rawValue?.let { code -> viewModel.checkBarcodeOnHome(code) }
                                }
                                .addOnFailureListener { e ->
                                    android.widget.Toast.makeText(context, "جاري تهيئة السكنر من خدمات جوجل، انتظر لحظة...", android.widget.Toast.LENGTH_LONG).show()
                                }
                        },
                        modifier = Modifier.size(40.dp).background(SMColors.Primary.copy(0.12f), RoundedCornerShape(14.dp)).border(1.dp, SMColors.Primary.copy(0.3f), RoundedCornerShape(14.dp))
                    ) { Icon(Icons.Filled.QrCodeScanner, null, tint = SMColors.Primary, modifier = Modifier.size(20.dp)) }
                    Box(Modifier.background(SMColors.BgCard, RoundedCornerShape(14.dp)).border(1.dp, SMColors.BgCardBorder, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(timeFormat.format(now), color = SMColors.Primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        item {
            Box(Modifier.fillMaxWidth().clickable { onNavigate("new_sale") }.background(Brush.linearGradient(listOf(SMColors.Primary, SMColors.PrimaryDark, Color(0xFF006B3A))), RoundedCornerShape(22.dp)).padding(20.dp)) {
                Canvas(Modifier.matchParentSize()) { for (i in 0..5) for (j in 0..2) drawCircle(Color.White.copy(0.06f), 20f, Offset(i * 80f + 20f, j * 60f + 10f)) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🛒 ابدأ عملية بيع جديدة", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("نقطة البيع المتكاملة • سريعة وسهلة", color = Color.Black.copy(0.6f), fontSize = 12.sp)
                    }
                    Box(Modifier.size(50.dp).background(Color.Black.copy(0.15f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }

        item { Text("الوصول السريع والأقسام", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProductVisualCard(recentProducts, Modifier.weight(1f)) { product ->
                    try { viewModel.checkBarcodeOnHome(product.barcode) } catch(e: Exception) {}
                    android.widget.Toast.makeText(context, "تمت إضافة ${product.name} للفاتورة", android.widget.Toast.LENGTH_SHORT).show()
                }
                CategoryVisualCard(ProductCategory.values().toList(), Modifier.weight(1f))
            }
        }

        item { Text("مؤشرات النظام", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("مبيعات اليوم", "${"%.1f".format(stats.todaySales)} ر", "${stats.todayTransactions} فاتورة", Icons.Filled.TrendingUp, SMColors.Primary, Modifier.weight(1f))
                KpiCard("التحذيرات", "${stats.lowStockProducts}", "مخجوان منخفض", Icons.Filled.Warning, if (stats.lowStockProducts > 0) SMColors.Warning else SMColors.TextMuted, Modifier.weight(1f), onClick = { onNavigate("inventory") })
            }
        }

        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SMColors.BgCard), border = BorderStroke(1.dp, SMColors.AccentCyan.copy(0.15f))) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(SMColors.AccentCyan.copy(0.12f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.BarChart, null, tint = SMColors.AccentCyan, modifier = Modifier.size(22.dp)) }
                        Column { Text("مبيعات الشهر الحالي", color = SMColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    }
                    Text("${"%.0f".format(stats.monthSales)} ر", color = SMColors.AccentCyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
        }

        item { CashierSalesBreakdown() }
        item { SalesChartCard(weeklySales) }

        if (lowStockProducts.isNotEmpty()) {
            item { LowStockAlert(lowStockProducts.take(3)) { onNavigate("inventory") } }
        }

        if (recentSales.isNotEmpty()) {
            item { Text("آخر العمليات", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            items(recentSales.take(4)) { sale -> RecentSaleRow(sale) { saleToEdit = sale } }
            item { TextButton(onClick = { onNavigate("sales") }, modifier = Modifier.fillMaxWidth()) { Text("عرض السجل ←", color = SMColors.Primary, fontWeight = FontWeight.SemiBold) } }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    saleToEdit?.let { sale ->
        SaleDetailsEditDialog(
            sale = sale,
            onDismiss = { saleToEdit = null },
            onSaveChanges = { updatedSale ->
                viewModel.updateSale(updatedSale)
                saleToEdit = null
            }
        )
    }
}

@Composable
fun CashierSalesBreakdown(viewModel: SalesViewModel = hiltViewModel()) {
    val cashierSales by viewModel.todaySalesByCashier.collectAsState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
        border = BorderStroke(1.dp, SMColors.BgCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(SMColors.Primary.copy(0.12f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.People, null, tint = SMColors.Primary, modifier = Modifier.size(22.dp))
                    }
                    Column { Text("تقسيم مبيعات اليوم حسب الموظفين", color = SMColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                }
            }

            Spacer(modifier = modifier.height(12.dp))
            if (cashierSales.isEmpty()) {
                Text("لا توجد عمليات بيع مسجلة للموظفين اليوم بعد.", color = SMColors.TextMuted, fontSize = 12.sp)
            } else {
                cashierSales.forEach { (cashierName, totalAmount) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, null, tint = SMColors.TextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = cashierName, color = SMColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(text = "${"%.1f".format(totalAmount)} ر", color = SMColors.Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductVisualCard(
    products: List<com.supermarket.app.data.models.Product>,
    modifier: Modifier,
    onProductClick: (com.supermarket.app.data.models.Product) -> Unit
) {
    Card(modifier = modifier.aspectRatio(1f), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SMColors.BgCard), border = BorderStroke(1.dp, SMColors.BgCardBorder)) {
        Column(Modifier.padding(10.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("الوصول السريع", color = SMColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.Inventory2, null, tint = SMColors.AccentPurple, modifier = Modifier.size(15.dp))
            }
            val items = products.take(4)
            Column(Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { MiniCircularProductItem(items.getOrNull(0), onProductClick) }
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { MiniCircularProductItem(items.getOrNull(1), onProductClick) }
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { MiniCircularProductItem(items.getOrNull(2), onProductClick) }
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { MiniCircularProductItem(items.getOrNull(3), onProductClick) }
                }
            }
        }
    }
}

@Composable
fun MiniCircularProductItem(
    product: com.supermarket.app.data.models.Product?,
    onClick: (com.supermarket.app.data.models.Product) -> Unit
) {
    if (product != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().clickable { onClick(product) },
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SMColors.BgDeep)
                    .border(1.dp, SMColors.BgCardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    val context = LocalContext.current
                    val imgData = remember(product.imageUrl) {
                        if (product.imageUrl.startsWith("http")) product.imageUrl
                        else try { android.util.Base64.decode(product.imageUrl, android.util.Base64.DEFAULT) } catch(e: Exception) { null }
                    }
                    SubcomposeAsyncImage(model = coil.request.ImageRequest.Builder(context).data(imgData).crossfade(true).size(100).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text(product.name.take(1).uppercase(), color = SMColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(product.name, color = SMColors.TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    } else {
        Spacer(Modifier.fillMaxSize())
    }
}

@Composable
fun CategoryVisualCard(categories: List<ProductCategory>, modifier: Modifier) {
    Card(modifier = modifier.aspectRatio(1f), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SMColors.BgCard), border = BorderStroke(1.dp, SMColors.BgCardBorder)) {
        Column(Modifier.padding(10.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("الأقسام", color = SMColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.Widgets, null, tint = SMColors.AccentCyan, modifier = Modifier.size(15.dp))
            }
            val items = categories.take(4)
            Column(Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { MiniCircularCategoryItem(items.getOrNull(0)) }
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { MiniCircularCategoryItem(items.getOrNull(1)) }
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { MiniCircularCategoryItem(items.getOrNull(2)) }
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { MiniCircularCategoryItem(items.getOrNull(3)) }
                }
            }
        }
    }
}

@Composable
fun MiniCircularCategoryItem(cat: ProductCategory?) {
    if (cat != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SMColors.Primary.copy(0.12f))
                    .border(1.dp, SMColors.Primary.copy(0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(cat.emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.height(2.dp))
            Text(cat.name, color = SMColors.TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    } else {
        Spacer(Modifier.fillMaxSize())
    }
}

@Composable
fun KpiCard(label: String, value: String, sub: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: (() -> Unit)? = null) {
    Card(modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SMColors.BgCard), border = BorderStroke(1.dp, color.copy(0.2f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(38.dp).background(color.copy(0.15f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
                Box(Modifier.size(8.dp).background(color, CircleShape))
            }
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Column {
                Text(label, color = SMColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(sub, color = SMColors.TextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun SalesChartCard(data: List<Pair<String, Double>>) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SMColors.BgCard), border = BorderStroke(1.dp, SMColors.BgCardBorder)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("مبيعات الأسبوع", color = SMColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${data.sumOf { it.second }.let { "%.0f".format(it) }} ر", color = SMColors.Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            if (data.isNotEmpty()) {
                val maxVal = data.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
                Row(Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    data.forEach { (label, value) ->
                        val frac = (value / maxVal).toFloat().coerceAtLeast(0.02f)
                        Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            if (value > 0) Text("${"%.0f".format(value)}", color = SMColors.Primary, fontSize = 8.sp)
                            Spacer(Modifier.height(2.dp))
                            Box(Modifier.fillMaxWidth(0.55f).fillMaxHeight(frac).background(Brush.verticalGradient(listOf(SMColors.Primary, SMColors.PrimaryDark)), RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)))
                            Spacer(Modifier.height(4.dp))
                            Text(label, color = SMColors.TextMuted, fontSize = 9.sp)
                        }
                    }
                }
            } else { Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { Text("لا توجد بيانات", color = SMColors.TextMuted) } }
        }
    }
}

@Composable
fun LowStockAlert(products: List<com.supermarket.app.data.models.Product>, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SMColors.Warning.copy(0.08f)), border = BorderStroke(1.dp, SMColors.Warning.copy(0.3f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Warning, null, tint = SMColors.Warning, modifier = Modifier.size(20.dp))
                Text("تنبيه: مخزون منخفض", color = SMColors.Warning, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            products.forEach { product ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(product.name, color = SMColors.TextPrimary, fontSize = 13.sp)
                    Text("${product.quantity} ${product.unit}", color = SMColors.Warning, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("اضغط للتفاصيل ←", color = SMColors.Warning.copy(0.7f), fontSize = 11.sp)
        }
    }
}

@Composable
fun RecentSaleRow(sale: com.supermarket.app.data.models.Sale, onClick: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).background(SMColors.BgCard, RoundedCornerShape(14.dp)).border(1.dp, SMColors.BgCardBorder, RoundedCornerShape(14.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(SMColors.Primary.copy(0.12f), RoundedCornerShape(10.dp)), contentAlignment = Center) { Icon(Icons.Outlined.Receipt, null, tint = SMColors.Primary, modifier = Modifier.size(18.dp)) }
            Column {
                Text(sale.invoiceNumber, color = SMColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("${sale.items.size} منتج • ${timeFormat.format(Date(sale.createdAt))}", color = SMColors.TextMuted, fontSize = 11.sp)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${"%.2f".format(sale.total)} ر", color = SMColors.Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(sale.paymentMethod.nameAr, color = SMColors.TextMuted, fontSize = 10.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailsEditDialog(
    sale: com.supermarket.app.data.models.Sale,
    onDismiss: () -> Unit,
    onSaveChanges: (com.supermarket.app.data.models.Sale) -> Unit,
    newSaleViewModel: NewSaleViewModel = hiltViewModel()
) {
    var editableItems by remember { mutableStateOf(sale.items) }
    var paymentMethod by remember { mutableStateOf(sale.paymentMethod) }
    var printerIpAddress by remember { mutableStateOf("192.168.1.100") }
    var printerMacAddress by remember { mutableStateOf("") }

    val updatedTotal = editableItems.sumOf { it.quantity * it.unitPrice }

    val currentSaleState = remember(editableItems, paymentMethod, updatedTotal) {
        sale.copy(items = editableItems, total = updatedTotal, paymentMethod = paymentMethod)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SMColors.BgCard,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("تفاصيل الفاتورة", color = SMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null, tint = SMColors.TextSecondary) }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("رقم الفاتورة: ${sale.invoiceNumber}", color = SMColors.TextMuted, fontSize = 12.sp)
                }

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(editableItems) { item ->
                        Row(Modifier.fillMaxWidth().background(SMColors.BgSurface, RoundedCornerShape(10.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, color = SMColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${item.unitPrice} ر.ي", color = SMColors.TextSecondary, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(onClick = { if (item.quantity > 1) { editableItems = editableItems.map { if (it.productId == item.productId) it.copy(quantity = it.quantity - 1) else it } } }, modifier = Modifier.size(28.dp).background(SMColors.BgDeep, RoundedCornerShape(6.dp))) {
                                    Icon(Icons.Filled.Remove, null, tint = SMColors.TextPrimary, modifier = Modifier.size(16.dp))
                                }
                                Text("${item.quantity.toInt()}", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                IconButton(onClick = { editableItems = editableItems.map { if (it.productId == item.productId) it.copy(quantity = it.quantity + 1) else it } }, modifier = Modifier.size(28.dp).background(SMColors.Primary.copy(0.2f), RoundedCornerShape(6.dp))) {
                                    Icon(Icons.Filled.Add, null, tint = SMColors.Primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Divider(color = SMColors.BgCardBorder)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الإجمالي الجديد:", color = SMColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("$updatedTotal ر.ي", color = SMColors.Primary, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(2.dp))
                Divider(color = SMColors.BgCardBorder.copy(alpha = 0.4f))

                OutlinedTextField(
                    value = printerIpAddress,
                    onValueChange = { printerIpAddress = it },
                    label = { Text("IP الطابعة (واي فاي)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = SMColors.TextPrimary),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = SMColors.Primary,
                        unfocusedBorderColor = SMColors.BgCardBorder
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { newSaleViewModel.printExistingSale(currentSaleState, "BT", printerMacAddress) },
                        enabled = printerMacAddress.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary.copy(0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SMColors.Primary.copy(0.4f))
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Receipt, null, tint = SMColors.Primary, modifier = Modifier.size(16.dp))
                            Text("بلوتوث", color = SMColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { newSaleViewModel.printExistingSale(currentSaleState, "WIFI", printerIpAddress) },
                        enabled = printerIpAddress.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SMColors.AccentCyan.copy(0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SMColors.AccentCyan.copy(0.4f))
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Receipt, null, tint = SMColors.AccentCyan, modifier = Modifier.size(16.dp))
                            Text("واي فاي", color = SMColors.AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveChanges(currentSaleState) },
                colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)
            ) { Text("حفظ التعديلات", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء", color = SMColors.TextSecondary) } }
    )
}
