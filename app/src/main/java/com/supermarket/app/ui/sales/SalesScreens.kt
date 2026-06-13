package com.supermarket.app.ui.sales

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.data.models.Product
import com.supermarket.app.data.models.PaymentMethod
import com.supermarket.app.data.models.SaleItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    viewModel: NewSaleViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val products by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val totalAmount by viewModel.total.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    var showPaySheet by remember { mutableStateOf(false) }
    val isCartNotEmpty = cartItems.isNotEmpty()
    val context = LocalContext.current
    val userRole by viewModel.userRole.collectAsState()
    val canAddProduct = !userRole.equals("CASHIER", ignoreCase = true)

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.onSearch(result.contents)
        } else {
            Toast.makeText(context, "تم إلغاء المسح", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("نقطة البيع السريعة", color = SMColors.TextPrimary,
                        fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                actions = {
                    if (canAddProduct) {
                        TextButton(
                            onClick = onNavigateToAddCategory,
                            colors = ButtonDefaults.textButtonColors(contentColor = SMColors.Primary)
                        ) {
                            Icon(Icons.Filled.Category, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("إضافة صنف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onNavigateToAddProduct) {
                            Icon(Icons.Filled.AddCircle, "إضافة منتج",
                                tint = SMColors.Primary, modifier = Modifier.size(28.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SMColors.BgSurface)
            )
        },
        bottomBar = {
            if (isCartNotEmpty) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SMColors.BgSurface,
                    border = BorderStroke(1.dp, SMColors.BgCardBorder)
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp).navigationBarsPadding()) {
                        // ملخص سريع للسلة
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${cartItems.size} صنف", color = SMColors.TextSecondary, fontSize = 13.sp)
                            Text("الإجمالي: $totalAmount ر.ي",
                                color = SMColors.Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showPaySheet = true },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)
                        ) {
                            Icon(Icons.Filled.ShoppingCart, null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("إتمام الدفع (${cartItems.size})",
                                color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.weight(1f))
                            Text("$totalAmount ر.ي",
                                color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }
                }
            }
        },
        containerColor = SMColors.BgDeep
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp)
        ) {
            // شريط البحث والمسح
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearch(it) },
                placeholder = { Text("ابحث باسم المنتج أو الباركود...",
                    color = SMColors.TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = SMColors.TextSecondary) },
                trailingIcon = {
                    Row {
                        // زر مسح البحث
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearch("") }) {
                                Icon(Icons.Filled.Close, null, tint = SMColors.TextMuted)
                            }
                        }
                        // زر مسح الباركود
                        IconButton(onClick = {
                            val options = ScanOptions()
                                .setOrientationLocked(false)
                                .setBeepEnabled(true)
                                .setPrompt("وجّه الكاميرا نحو الباركود")
                            barcodeLauncher.launch(options)
                        }) {
                            Icon(Icons.Filled.QrCodeScanner, "مسح الباركود", tint = SMColors.Primary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = com.supermarket.app.ui.smOutlinedColors(),
                singleLine = true
            )

            // إحصائية سريعة
            if (isCartNotEmpty) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SMColors.Primary.copy(0.1f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${cartItems.size}", color = SMColors.Primary,
                                fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text("صنف", color = SMColors.TextMuted, fontSize = 11.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(2f),
                        colors = CardDefaults.cardColors(containerColor = SMColors.Primary.copy(0.1f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$totalAmount ر.ي", color = SMColors.Primary,
                                fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text("الإجمالي", color = SMColors.TextMuted, fontSize = 11.sp)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.clearCart() },
                        modifier = Modifier.background(SMColors.Error.copy(0.1f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Filled.DeleteSweep, null, tint = SMColors.Error)
                    }
                }
            }

            // شبكة المنتجات
            if (products.isEmpty() && searchQuery.isNotEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.SearchOff, null,
                            tint = SMColors.TextMuted, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("لا توجد نتائج لـ \"$searchQuery\"",
                            color = SMColors.TextMuted, fontSize = 14.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products) { product ->
                        val cartItem = cartItems.find { it.productId == product.id }
                        POSProductCard(
                            product = product,
                            cartQty = cartItem?.quantity?.toInt() ?: 0,
                            onClick = {
                                viewModel.addToCart(product)
                                Toast.makeText(context, "✓ ${product.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // شاشة الدفع
    if (showPaySheet) {
        var paidAmountStr by remember { mutableStateOf("") }
        var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
        var customerName by remember { mutableStateOf("زبون نقدي") }

        ModalBottomSheet(
            onDismissRequest = { showPaySheet = false },
            containerColor = SMColors.BgCard,
            dragHandle = { BottomSheetDefaults.DragHandle(color = SMColors.BgCardBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 34.dp)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("مراجعة الفاتورة", color = SMColors.TextPrimary,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // قائمة الأصناف
                Card(
                    colors = CardDefaults.cardColors(containerColor = SMColors.BgSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SMColors.BgCardBorder)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        cartItems.forEachIndexed { index, saleItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(saleItem.productName, color = SMColors.TextPrimary,
                                        fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis)
                                    Text("${saleItem.unitPrice} ر.ي × ${saleItem.quantity.toInt()}",
                                        color = SMColors.TextMuted, fontSize = 11.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.decreaseQty(saleItem.productId) },
                                        modifier = Modifier.size(28.dp)
                                            .background(SMColors.BgDeep, RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Filled.Remove, null,
                                            tint = SMColors.TextPrimary, modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        text = saleItem.quantity.toInt().toString(),
                                        color = SMColors.Primary, fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(30.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { viewModel.increaseQty(saleItem.productId) },
                                        modifier = Modifier.size(28.dp)
                                            .background(SMColors.BgDeep, RoundedCornerShape(6.dp))
                                    ) {
                                        Icon(Icons.Filled.Add, null,
                                            tint = SMColors.TextPrimary, modifier = Modifier.size(14.dp))
                                    }
                                    Text("${saleItem.total} ر.ي",
                                        color = SMColors.Primary, fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.End)
                                    IconButton(
                                        onClick = {
                                            viewModel.removeFromCart(saleItem.productId)
                                            if (cartItems.size <= 1) showPaySheet = false
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, null,
                                            tint = SMColors.Error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            if (index < cartItems.lastIndex) {
                                Divider(color = SMColors.BgCardBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                // الإجمالي
                Card(
                    colors = CardDefaults.cardColors(containerColor = SMColors.Primary.copy(0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الإجمالي المطلوب", color = SMColors.TextSecondary,
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("$totalAmount ر.ي", color = SMColors.Primary,
                            fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }

                // اسم الزبون
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("اسم الزبون (اختياري)") },
                    leadingIcon = { Icon(Icons.Filled.Person, null, tint = SMColors.TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = com.supermarket.app.ui.smOutlinedColors(),
                    singleLine = true
                )

                // طريقة الدفع
                Text("طريقة الدفع", color = SMColors.TextSecondary,
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        PaymentMethod.CASH to "نقداً",
                        PaymentMethod.CARD to "شبكة",
                        PaymentMethod.TRANSFER to "تحويل"
                    ).forEach { (method, label) ->
                        ElevatedButton(
                            onClick = { selectedMethod = method },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (selectedMethod == method) SMColors.Primary else SMColors.BgSurface
                            )
                        ) {
                            Text(label, color = Color.Black,
                                fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // المبلغ المدفوع
                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = { paidAmountStr = it },
                    label = { Text("المبلغ المدفوع") },
                    leadingIcon = { Icon(Icons.Filled.Payments, null, tint = SMColors.TextSecondary) },
                    placeholder = { Text("$totalAmount", color = SMColors.TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = com.supermarket.app.ui.smOutlinedColors(),
                    singleLine = true
                )

                val totalDouble = totalAmount.toString().toDoubleOrNull() ?: 0.0
                val paid = paidAmountStr.toDoubleOrNull() ?: totalDouble
                val change = (paid - totalDouble).coerceAtLeast(0.0)

                if (change > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SMColors.AccentOrange.copy(0.1f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SMColors.AccentOrange.copy(0.3f))
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الفكة المتبقية", color = SMColors.TextSecondary, fontSize = 14.sp)
                            Text("${"%.2f".format(change)} ر.ي",
                                color = SMColors.AccentOrange, fontSize = 18.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // زر تأكيد البيع
                Button(
                    onClick = {
                        viewModel.completeSale(
                            customerName = customerName.ifBlank { "زبون نقدي" },
                            paidAmount = paid,
                            paymentMethod = selectedMethod,
                            onSuccess = { showPaySheet = false }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                    } else {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("تأكيد البيع وطباعة الفاتورة",
                            color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }

                // زر إلغاء
                OutlinedButton(
                    onClick = { viewModel.clearCart(); showPaySheet = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SMColors.Error),
                    border = BorderStroke(1.dp, SMColors.Error.copy(0.5f))
                ) {
                    Icon(Icons.Filled.DeleteSweep, null, tint = SMColors.Error)
                    Spacer(Modifier.width(6.dp))
                    Text("إلغاء وتفريغ السلة",
                        color = SMColors.Error, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun POSProductCard(
    product: Product,
    cartQty: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (cartQty > 0) SMColors.Primary.copy(0.08f) else SMColors.BgCard
        ),
        border = BorderStroke(
            if (cartQty > 0) 1.5.dp else 1.dp,
            if (cartQty > 0) SMColors.Primary.copy(0.5f) else SMColors.BgCardBorder
        )
    ) {
        Box {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(85.dp)
                        .background(SMColors.BgSurface)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.imageUrl.isNotEmpty()) {
                        val imageRequestData = remember(product.imageUrl) {
                            if (product.imageUrl.startsWith("http")) product.imageUrl
                            else {
                                try { android.util.Base64.decode(product.imageUrl, android.util.Base64.DEFAULT) }
                                catch (e: Exception) { null }
                            }
                        }
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageRequestData).crossfade(true).size(150, 150).build(),
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = { Icon(Icons.Outlined.Image, null, tint = SMColors.TextMuted) }
                        )
                    } else {
                        Icon(Icons.Outlined.Image, null, tint = SMColors.TextMuted.copy(0.4f))
                    }
                }

                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(product.name, color = SMColors.TextPrimary, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text("${product.sellPrice} ر.ي", color = SMColors.Primary,
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Text("مخزون: ${product.quantity}",
                        color = if (product.quantity <= 5) SMColors.Error else SMColors.TextMuted,
                        fontSize = 9.sp)
                }
            }

            // شارة الكمية في السلة
            if (cartQty > 0) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        .size(22.dp).background(SMColors.Primary, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$cartQty", color = Color.Black,
                        fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
