package com.supermarket.app.ui.inventory
import com.supermarket.app.ui.smOutlinedColors
import com.supermarket.app.ui.smOutlinedColors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarket.app.data.models.ProductCategory
import com.supermarket.app.ui.theme.SMColors
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    val tempFile by remember { mutableStateOf(File(context.cacheDir, "temp_product_capture.jpg")) }
    val tempUri by remember { mutableStateOf(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)) }
import com.supermarket.app.ui.smOutlinedColors
    var bitmapPreview by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(state.imageUrl) {
        if (state.imageUrl.isNotEmpty() && bitmapPreview == null) {
            try {
                val decodedBytes = Base64.decode(state.imageUrl, Base64.DEFAULT)
                bitmapPreview = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val compressedBitmap = scaleAndCompressImage(context, tempUri, 600)
            if (compressedBitmap != null) {
                bitmapPreview = compressedBitmap
                val outputStream = ByteArrayOutputStream()
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                viewModel.update { copy(imageUrl = base64Image) }
            }
        }
    }

    LaunchedEffect(productId) { productId?.let { viewModel.loadProduct(it) } }

    Column(
        Modifier.fillMaxSize().background(SMColors.BgDeep).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (error != null) {
            Card(colors = CardDefaults.cardColors(containerColor = SMColors.Error.copy(0.1f)), border = BorderStroke(1.dp, SMColors.Error), shape = RoundedCornerShape(14.dp)) {
                Text(error!!, color = SMColors.Error, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }

        SMSectionCard("صورة المنتج", Icons.Filled.PhotoCamera) {
            Box(Modifier.fillMaxWidth().height(180.dp).background(SMColors.BgSurface, RoundedCornerShape(14.dp)).border(1.dp, SMColors.BgCardBorder, RoundedCornerShape(14.dp)).clickable { cameraLauncher.launch(tempUri) }, contentAlignment = Alignment.Center) {
                if (bitmapPreview != null) {
                    Image(bitmap = bitmapPreview!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CloudUpload, null, tint = SMColors.Primary, modifier = Modifier.size(40.dp))
                        Text("اضغط هنا لالتقاط صورة بالكاميرا", color = SMColors.TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        SMSectionCard("معلومات المنتج", Icons.Filled.Label) {
            SMField("اسم المنتج *", state.name, Icons.Filled.Label) { viewModel.update { copy(name = it) } }
            SMField("الباركود", state.barcode, Icons.Filled.QrCode) { viewModel.update { copy(barcode = it) } }
            SMField("الماركة", state.brand, Icons.Filled.Business) { viewModel.update { copy(brand = it) } }
            SMField("الوحدة", state.unit, Icons.Filled.Scale) { viewModel.update { copy(unit = it) } }
        }

        SMSectionCard("الفئة", Icons.Filled.Category) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded, { expanded = it }) {
                OutlinedTextField(
                    value = "${state.category.emoji} ${state.category.nameAr}", onValueChange = {}, readOnly = true,
                    label = { Text("فئة المنتج") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(14.dp), colors = smOutlinedColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(SMColors.BgCard)) {
                    ProductCategory.values().forEach { cat ->
                        DropdownMenuItem(text = { Text("${cat.emoji} ${cat.nameAr}", color = SMColors.TextPrimary) }, onClick = { viewModel.update { copy(category = cat) }; expanded = false })
                    }
                }
            }
        }

        SMSectionCard("الأسعار والمخزون", Icons.Filled.AttachMoney) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SMField("سعر الشراء *", state.buyPrice, Icons.Filled.AttachMoney, KeyboardType.Decimal, Modifier.weight(1f)) { viewModel.update { copy(buyPrice = it) } }
                SMField("سعر البيع *", state.sellPrice, Icons.Filled.Sell, KeyboardType.Decimal, Modifier.weight(1f)) { viewModel.update { copy(sellPrice = it) } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SMField("الكمية", state.quantity, Icons.Filled.Inventory, KeyboardType.Number, Modifier.weight(1f)) { viewModel.update { copy(quantity = it) } }
                SMField("حد التنبيه", state.minQuantity, Icons.Filled.NotificationImportant, KeyboardType.Number, Modifier.weight(1f)) { viewModel.update { copy(minQuantity = it) } }
            }
        }

        Button(onClick = { viewModel.saveProduct { onSaved() } }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary), enabled = !isLoading) {
            if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
            else {
                Icon(Icons.Filled.Save, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(if (productId != null) "حفظ التعديلات" else "إضافة المنتج", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

fun scaleAndCompressImage(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
    var inputStream: InputStream? = null
    try {
        inputStream = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        var sampleSize = 1
        while ((options.outWidth / sampleSize > maxDimension) || (options.outHeight / sampleSize > maxDimension)) { sampleSize *= 2 }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        inputStream = context.contentResolver.openInputStream(uri)
        val srcBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        inputStream?.close()

        return srcBitmap
    } catch (e: Exception) { return null }
}

@Composable
fun SMSectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SMColors.BgCard), border = BorderStroke(1.dp, SMColors.BgCardBorder)) {
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
fun SMField(label: String, value: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier.fillMaxWidth(), onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, leadingIcon = { Icon(icon, null, tint = SMColors.TextSecondary, modifier = Modifier.size(20.dp)) }, modifier = modifier, shape = RoundedCornerShape(14.dp), colors = smOutlinedColors(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType))
}
