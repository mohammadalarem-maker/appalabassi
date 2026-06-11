package com.supermarket.app.ui.sales

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.data.models.Product
import com.supermarket.app.data.models.PaymentMethod

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

    var showPaySheet by remember { mutableStateOf(false) }
    
    // فحص ما إذا كانت السلة تحتوي على منتجات بناءً على القيمة الإجمالية
    val isCartNotEmpty = (totalAmount.toString().toDoubleOrNull() ?: 0.0) > 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نقطة البيع السريعة", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    TextButton(onClick = onNavigateToAddCategory, colors = ButtonDefaults.textButtonColors(contentColor = SMColors.Primary)) {
                        Icon(Icons.Filled.Category, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("إضافة صنف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onNavigateToAddProduct) {
                        Icon(Icons.Filled.AddCircle, "إضافة منتج", tint = SMColors.Primary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SMColors.BgSurface)
            )
        },
        // إضافة شريط سفلي ثابت يظهر تفاعلياً عند وجود منتجات في السلة لإتمام الدفع للمجموعة كاملة
        bottomBar = {
            if (isCartNotEmpty) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SMColors.BgSurface,
                    border = BorderStroke(1.dp, SMColors.BgCardBorder)
                ) {
                    Button(
                        onClick = { showPaySheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding() // رفع الزر عن شريط الإيماءات ونظام الهاتف السفلي 
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ShoppingCart, null, tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("عرض السلة وإتمام الدفع", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("${totalAmount} ر.ي", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        containerColor = SMColors.BgDeep
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            // شريط البحث
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearch(it) },
                placeholder = { Text("ابحث باسم المنتج أو الباركود...", color = SMColors.TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = SMColors.TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = com.supermarket.app.ui.smOutlinedColors()
            )

            // شبكة عرض المنتجات
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { product ->
                    POSProductCard(
                        product = product,
                        onClick = {
                            viewModel.addToCart(product) // الإضافة للسلة مباشرة لتتمكن من اختيار عناصر أخرى
                        }
                    )
                }
            }
        }
    }

    // شيت إجراءات الدفع للمجموعة كاملة
    if (showPaySheet) {
        var paidAmountStr by remember { mutableStateOf("") }
        var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }

        ModalBottomSheet(
            onDismissRequest = { showPaySheet = false },
            containerColor = SMColors.BgCard,
            dragHandle = { BottomSheetDefaults.DragHandle(color = SMColors.BgCardBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 34.dp) // زيادة الحشوة السفلية لرفع كرت تأكيد الدفع عن الخط السفلي للنظام
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("إجراءات الدفع الفوري", color = SMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Text(text = "فاتورة مبيعات متعددة الأصناف", color = SMColors.Primary, fontSize = 16.sp, fontWeight = FontWeight.Black)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الإجمالي المطلوب:", color = SMColors.TextSecondary, fontSize = 14.sp)
                    Text("${totalAmount} ر.ي", color = SMColors.Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Divider(color = SMColors.BgCardBorder)

                // خيارات الدفع (نقداً / شبكة)
                Text("طريقة الدفع:", color = SMColors.TextSecondary, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatedButton(
                        onClick = { selectedMethod = PaymentMethod.CASH },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = if (selectedMethod == PaymentMethod.CASH) SMColors.Primary else SMColors.BgSurface)
                    ) {
                        Icon(Icons.Filled.Payments, null, tint = Color.Black)
                        Spacer(Modifier.width(6.dp))
                        Text("نقداً", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    ElevatedButton(
                        onClick = { selectedMethod = PaymentMethod.CARD },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = if (selectedMethod == PaymentMethod.CARD) SMColors.Primary else SMColors.BgSurface)
                    ) {
                        Icon(Icons.Filled.CreditCard, null, tint = Color.Black)
                        Spacer(Modifier.width(6.dp))
                        Text("شبكة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // حقل إدخال المبلغ المدفوع
                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = { paidAmountStr = it },
                    label = { Text("المبلغ المدفوع من الزبون") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = com.supermarket.app.ui.smOutlinedColors()
                )

                val totalAmountDouble = totalAmount.toString().toDoubleOrNull() ?: 0.0
                val paidAmount = paidAmountStr.toDoubleOrNull() ?: totalAmountDouble
                val change = (paidAmount - totalAmountDouble).coerceAtLeast(0.0)

                if (change > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المبلغ المتبقي (الفكة):", color = SMColors.TextSecondary, fontSize = 14.sp)
                        Text("${change} ر.ي", color = SMColors.Error, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // زر التأكيد النهائي وحفظ الفاتورة
                Button(
                    onClick = {
                        viewModel.completeSale(
                            customerName = "زبون نقدي سريع",
                            paidAmount = paidAmount,
                            paymentMethod = selectedMethod,
                            onSuccess = { showPaySheet = false }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                    else Text("تأكيد البيع وطباعة الفاتورة", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun POSProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
        border = BorderStroke(1.dp, SMColors.BgCardBorder)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.fillMaxWidth().height(85.dp).background(SMColors.BgSurface).clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
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
                            .data(imageRequestData)
                            .crossfade(true)
                            .size(150, 150)
                            .build(),
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
                Text(text = product.name, color = SMColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(text = "${product.sellPrice} ر.ي", color = SMColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "المخزن: ${product.quantity}", color = if ((product.quantity.toString().toDoubleOrNull() ?: 0.0) <= 5.0) SMColors.Error else SMColors.TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
