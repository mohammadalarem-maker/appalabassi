package com.supermarket.app.ui.inventory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarket.app.data.models.ProductCategory
import com.supermarket.app.ui.theme.SMColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    val state     by viewModel.state.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()

    LaunchedEffect(productId) { productId?.let { viewModel.loadProduct(it) } }

    Column(
        Modifier.fillMaxSize().background(SMColors.BgDeep)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Error banner
        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SMColors.Error.copy(0.1f)),
                border = BorderStroke(1.dp, SMColors.Error),
                shape  = RoundedCornerShape(14.dp)
            ) { Text(error!!, color = SMColors.Error, modifier = Modifier.padding(12.dp), fontSize = 13.sp) }
        }

        // Basic info card
        SMSectionCard("معلومات المنتج", Icons.Filled.Label) {
            SMField("اسم المنتج *", state.name, Icons.Filled.Label) { viewModel.update { copy(name = it) } }
            SMField("الباركود", state.barcode, Icons.Filled.QrCode) { viewModel.update { copy(barcode = it) } }
            SMField("الماركة", state.brand, Icons.Filled.Business) { viewModel.update { copy(brand = it) } }
            SMField("الوحدة (قطعة/كيلو/لتر)", state.unit, Icons.Filled.Scale) { viewModel.update { copy(unit = it) } }

            // Weighed switch
            Row(
                Modifier.fillMaxWidth().background(SMColors.BgSurface, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("منتج يُوزن؟", color = SMColors.TextPrimary, fontSize = 14.sp)
                    Text("للمنتجات المباعة بالكيلو", color = SMColors.TextMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = state.isWeighed,
                    onCheckedChange = { viewModel.update { copy(isWeighed = it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = SMColors.Primary)
                )
            }
        }

        // Category
        SMSectionCard("الفئة", Icons.Filled.Category) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded, { expanded = it }) {
                OutlinedTextField(
                    value = "${state.category.emoji} ${state.category.nameAr}",
                    onValueChange = {}, readOnly = true,
                    label = { Text("فئة المنتج") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp), colors = smOutlinedColors()
                )
                ExposedDropdownMenu(expanded, { expanded = false },
                    modifier = Modifier.background(SMColors.BgCard)) {
                    ProductCategory.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.emoji} ${cat.nameAr}", color = SMColors.TextPrimary) },
                            onClick = { viewModel.update { copy(category = cat) }; expanded = false }
                        )
                    }
                }
            }
        }

        // Pricing
        SMSectionCard("الأسعار والمخزون", Icons.Filled.AttachMoney) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SMField("سعر الشراء *", state.buyPrice, Icons.Filled.AttachMoney, KeyboardType.Decimal, Modifier.weight(1f)) {
                    viewModel.update { copy(buyPrice = it) }
                }
                SMField("سعر البيع *", state.sellPrice, Icons.Filled.Sell, KeyboardType.Decimal, Modifier.weight(1f)) {
                    viewModel.update { copy(sellPrice = it) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SMField("الكمية", state.quantity, Icons.Filled.Inventory, KeyboardType.Number, Modifier.weight(1f)) {
                    viewModel.update { copy(quantity = it) }
                }
                SMField("حد التنبيه", state.minQuantity, Icons.Filled.NotificationImportant, KeyboardType.Number, Modifier.weight(1f)) {
                    viewModel.update { copy(minQuantity = it) }
                }
            }

            // Profit indicator
            val buy  = state.buyPrice.toDoubleOrNull() ?: 0.0
            val sell = state.sellPrice.toDoubleOrNull() ?: 0.0
            if (buy > 0 && sell > 0) {
                val profit = sell - buy
                val pct    = if (buy > 0) (profit / buy) * 100 else 0.0
                val c      = if (profit > 0) SMColors.Primary else SMColors.Error
                Row(
                    Modifier.fillMaxWidth().background(c.copy(0.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, c.copy(0.3f), RoundedCornerShape(12.dp)).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("هامش الربح", color = SMColors.TextSecondary, fontSize = 13.sp)
                    Text("${"%.2f".format(profit)} ر (${"%.1f".format(pct)}%)",
                        color = c, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Extra info
        SMSectionCard("معلومات إضافية", Icons.Filled.Info) {
            SMField("موقع الرف", state.location, Icons.Filled.LocationOn) { viewModel.update { copy(location = it) } }
            OutlinedTextField(
                value = state.description, onValueChange = { viewModel.update { copy(description = it) } },
                label = { Text("الوصف / الملاحظات") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp), colors = smOutlinedColors(),
                minLines = 3, maxLines = 5
            )
        }

        // Save Button
        Button(
            onClick = { viewModel.saveProduct { onSaved() } },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Save, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (productId != null) "حفظ التعديلات" else "إضافة المنتج",
                    color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun SMSectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
        border = BorderStroke(1.dp, SMColors.BgCardBorder)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = SMColors.Primary, modifier = Modifier.size(18.dp))
                Text(title, color = SMColors.Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            content()
        }
    }
}

@Composable
fun SMField(
    label: String, value: String, icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = SMColors.TextSecondary, modifier = Modifier.size(20.dp)) },
        modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = smOutlinedColors(), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
