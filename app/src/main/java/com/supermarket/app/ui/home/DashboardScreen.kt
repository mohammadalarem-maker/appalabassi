package com.supermarket.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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

    // حوار تنبيهي ذكي يظهر عندما لا يتم العثور على الصنف الممسوح بالباركود
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
                ) { 
                    Text("تسجيل الآن", color = Color.Black, fontWeight = FontWeight.Bold) 
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissNotFoundDialog() }) { 
                    Text("إلغاء", color = SMColors.TextMuted) 
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SMColors.BgDeep),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // [أولاً] الترحيب + التاريخ + البحث الذكي بالباركود
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("مرحباً 👋", color = SMColors.TextSecondary, fontSize = 13.sp)
                    Text(dateFormat.format(now), color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            barcodeScanner.startScan()
                                .addOnSuccessListener { res ->
                                    res.rawValue?.let { code -> viewModel.checkBarcodeOnHome(code) }
                                }
                                .addOnFailureListener { e -> e.printStackTrace() }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(SMColors.Primary.copy(0.12f), RoundedCornerShape(14.dp))
                            .border(1.dp, SMColors.Primary.copy(0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "بحث بالباركود", tint = SMColors.Primary, modifier = Modifier.size(20.dp))
                    }

                    // الساعة الرقمية
                    Box(
                        Modifier.background(SMColors.BgCard, RoundedCornerShape(14.dp))
                            .border(1.dp, SMColors.BgCardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(timeFormat.format(now), color = SMColors.Primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // [ثانياً] بنر بدء عملية بيع جديدة
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("new_sale") }
                    .background(
                        Brush.linearGradient(listOf(SMColors.Primary, SMColors.PrimaryDark, Color(0xFF006B3A))),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp)
            ) {
                Canvas(Modifier.matchParentSize()) {
                    for (i in 0..5) for (j in 0..2) {
                        drawCircle(Color.White.copy(0.06f), 20f, Offset(i * 80f + 20f, j * 60f + 10f))
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🛒 ابدأ عملية بيع جديدة", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("نقطة البيع المتكاملة • سريعة وسهلة", color = Color.Black.copy(0.6f), fontSize = 12.sp)
                    }
                    Box(
                        Modifier.size(50.dp).background(Color.Black.copy(0.15f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }

        // عنوان لوحة التحكم السريعة
        item {
            Text("المؤشرات الرئيسية", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }

        // [ثالثاً] تقسيم المربعات الأربعة المحدث حسب طلبك ورسمتك (المنتجات، الأقسام، مبيعات اليوم، التحذيرات)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // مربع 1 (أعلى يمين): المنتجات النشطة
                KpiCard(
                    "المنتجات النشطة",
                    "${stats.totalProducts}",
                    "صنف متاح بالنظام",
                    Icons.Filled.Inventory2, SMColors.AccentPurple, Modifier.weight(1f),
                    onClick = { onNavigate("inventory") }
                )
                // مربع 2 (أعلى يسار): الأقسام
                KpiCard(
                    "الأقسام",
                    "${ProductCategory.values().size}",
                    "تصفح تصنيفات الفئات",
                    Icons.Filled.Widgets, SMColors.AccentCyan, Modifier.weight(1f),
                    onClick = { onNavigate("inventory") }
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // مربع 3 (أسفل يمين): مبيعات اليوم
                KpiCard(
                    "مبيعات اليوم",
                    "${"%.1f".format(stats.todaySales)} ر",
                    "${stats.todayTransactions} فاتورة",
                    Icons.Filled.TrendingUp, SMColors.Primary, Modifier.weight(1f)
                )
                // مربع 4 (أسفل يسار): التحذيرات
                KpiCard(
                    "التحذيرات",
                    "${stats.lowStockProducts}",
                    "مخزون منخفض",
                    Icons.Filled.Warning,
                    if (stats.lowStockProducts > 0) SMColors.Warning else SMColors.TextMuted,
                    Modifier.weight(1f),
                    onClick = { onNavigate("inventory") }
                )
            }
        }

        // [رابعاً] إعادة تنسيق أسفل الشاشة المختار والأفضل احترافياً
        item {
            Text("التقارير المالية والبيانية", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }

        // بنر عريض ومميز لمبيعات الشهر لضمان التوازن البصري وعدم فقدان المؤشر
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
                border = BorderStroke(1.dp, SMColors.AccentCyan.copy(0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).background(SMColors.AccentCyan.copy(0.12f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.BarChart, null, tint = SMColors.AccentCyan, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("إجمالي مبيعات الشهر الحالي", color = SMColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("التقرير التراكمي المحدث", color = SMColors.TextMuted, fontSize = 11.sp)
                        }
                    }
                    Text("${"%.0f".format(stats.monthSales)} ر", color = SMColors.AccentCyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
        }

        // المخطط البياني الأسبوعي للمبيعات
        item {
            SalesChartCard(weeklySales)
        }

        // قسم استوديو المنتجات النشطة مؤخراً (تحميل ذكي مرئي)
        item {
            Text(
                text = "المنتجات النشطة مؤخراً",
                color = SMColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
        item {
            if (recentProducts.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(recentProducts.take(6)) { product ->
                        StudioProductCard(product = product) {
                            onNavigate("inventory")
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(SMColors.BgCard, RoundedCornerShape(18.dp))
                        .border(1.dp, SMColors.BgCardBorder, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد منتجات نشطة حالياً", color = SMColors.TextMuted, fontSize = 12.sp)
                }
            }
        }

        // الأقسام السريعة (شيبس دائرية للتصفح السريع)
        item {
            Text("الأقسام", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ProductCategory.values().toList()) { cat ->
                    CategoryChip(cat) { onNavigate("inventory") }
                }
            }
        }

        // كرت تفاصيل المخزون المنخفض الفعلي (إذا وجد)
        if (lowStockProducts.isNotEmpty()) {
            item {
                LowStockAlert(lowStockProducts.take(3)) { onNavigate("inventory") }
            }
        }

        // قائمة آخر الفواتير والمبيعات في تذييل الصفحة
        if (recentSales.isNotEmpty()) {
            item {
                Text("آخر العمليات والبيع المباشر", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            }
            items(recentSales.take(4)) { sale ->
                RecentSaleRow(sale)
            }
            item {
                TextButton(
                    onClick = { onNavigate("sales") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("عرض سجل المبيعات الكامل ←", color = SMColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun StudioProductCard(
    product: com.supermarket.app.data.models.Product,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(105.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(105.dp)
                .background(SMColors.BgCard, RoundedCornerShape(18.dp))
                .border(1.dp, SMColors.BgCardBorder, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (product.imageUrl.isNotEmpty()) {
                val imageRequestData = remember(product.imageUrl) {
                    if (product.imageUrl.startsWith("http")) {
                        product.imageUrl
                    } else {
                        try {
                            android.util.Base64.decode(product.imageUrl, android.util.Base64.DEFAULT)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                SubcomposeAsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(imageRequestData)
                        .crossfade(true)
                        .size(200, 200)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(Modifier.fillMaxSize().background(SMColors.TextMuted.copy(0.1f)))
                    },
                    error = {
                        Icon(Icons.Outlined.Image, null, tint = SMColors.TextMuted)
                    }
                )
            } else {
                val initials = if (product.name.length >= 2) product.name.take(2) else product.name
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SMColors.Primary.copy(0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.uppercase(java.util.Locale.getDefault()),
                        color = SMColors.Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = product.name,
                color = SMColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = "${product.quantity} ${product.unit}",
                color = SMColors.TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun KpiCard(
    label: String, value: String, sub: String,
    icon: ImageVector, color: Color, modifier: Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
        border = BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    Modifier.size(38.dp).background(color.copy(0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
                Box(Modifier.size(8.dp).background(color, CircleShape))
            }
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Column {
                Text(label, color = SMColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(sub, color = SMColors.TextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun SalesChartCard(data: List<Pair<String, Double>>) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
        border = BorderStroke(1.dp, SMColors.BgCardBorder)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("مبيعات الأسبوع", color = SMColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${data.sumOf { it.second }.let { "%.0f".format(it) }} ر", color = SMColors.Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            if (data.isNotEmpty()) {
                val maxVal = data.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
                Row(
                    Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEach { (label, value) ->
                        val frac = (value / maxVal).toFloat().coerceAtLeast(0.02f)
                        Column(
                            Modifier.weight(1f).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (value > 0) Text("${"%.0f".format(value)}", color = SMColors.Primary, fontSize = 8.sp)
                            Spacer(Modifier.height(2.dp))
                            Box(
                                Modifier.fillMaxWidth(0.55f).fillMaxHeight(frac)
                                    .background(
                                        Brush.verticalGradient(listOf(SMColors.Primary, SMColors.PrimaryDark)),
                                        RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(label, color = SMColors.TextMuted, fontSize = 9.sp)
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد بيانات بعد", color = SMColors.TextMuted)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(cat: ProductCategory, onClick: () -> Unit) {
    val catColor = Color(cat.color)
    Column(
        Modifier
            .width(80.dp)
            .background(catColor.copy(0.1f), RoundedCornerShape(16.dp))
            .border(1.dp, catColor.copy(0.25f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(cat.emoji, fontSize = 24.sp)
        Text(cat.nameAr.take(8), color = SMColors.TextSecondary, fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2, lineHeight = 12.sp)
    }
}

@Composable
fun LowStockAlert(products: List<com.supermarket.app.data.models.Product>, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.Warning.copy(0.08f)),
        border = BorderStroke(1.dp, SMColors.Warning.copy(0.3f))
    ) {
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
fun RecentSaleRow(sale: com.supermarket.app.data.models.Sale) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Row(
        Modifier.fillMaxWidth()
            .background(SMColors.BgCard, RoundedCornerShape(14.dp))
            .border(1.dp, SMColors.BgCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).background(SMColors.Primary.copy(0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.Receipt, null, tint = SMColors.Primary, modifier = Modifier.size(18.dp)) }
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
