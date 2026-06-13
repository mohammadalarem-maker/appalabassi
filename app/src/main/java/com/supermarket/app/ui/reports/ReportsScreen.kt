@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.supermarket.app.ui.reports
import com.supermarket.app.ui.smOutlinedColors

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarket.app.ui.theme.SMColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val stats       by viewModel.stats.collectAsState()
    val weeklySales by viewModel.weeklySales.collectAsState()
    val sales       by viewModel.salesList.collectAsState()
    var period      by remember { mutableStateOf(1) }
    val periods = listOf("اليوم", "الأسبوع", "الشهر", "السنة")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize().background(SMColors.BgDeep),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(periods.indices.toList()) { i ->
                    FilterChip(
                        selected = period == i,
                        onClick  = { period = i; viewModel.loadPeriod(i) },
                        label    = { Text(periods[i]) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SMColors.Primary.copy(0.2f),
                            selectedLabelColor     = SMColors.Primary
                        )
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { ReportKpi("المبيعات",   "${"%.2f".format(stats.sales)} ر",    SMColors.Primary,       Icons.Filled.TrendingUp) }
                item { ReportKpi("المصروفات",  "${"%.2f".format(stats.expenses)} ر", SMColors.Error,         Icons.Filled.AccountBalance) }
                item { ReportKpi("إجمالي الربح","${"%.2f".format(stats.grossProfit)} ر", SMColors.AccentCyan, Icons.Filled.AttachMoney) }
                item { ReportKpi("صافي الربح", "${"%.2f".format(stats.netProfit)} ر", if (stats.netProfit >= 0) SMColors.Primary else SMColors.Error, Icons.Filled.PieChart) }
            }
        }

        // ===== أزرار التصدير =====
        item {
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
                border = BorderStroke(1.dp, SMColors.BgCardBorder)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Download, null, tint = SMColors.Primary, modifier = Modifier.size(18.dp))
                        Text("تصدير البيانات", color = SMColors.Primary,
                            fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // زر Excel
                        Button(
                            onClick = {
                                scope.launch {
                                    isExporting = true
                                    try {
                                        val file = exportToExcel(context, sales, stats)
                                        shareFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                        Toast.makeText(context, "✓ تم تصدير Excel", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                    isExporting = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF217346)),
                            enabled = !isExporting
                        ) {
                            Icon(Icons.Filled.TableChart, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Excel", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // زر PDF
                        Button(
                            onClick = {
                                scope.launch {
                                    isExporting = true
                                    try {
                                        val file = exportToPdf(context, sales, stats)
                                        shareFile(context, file, "application/pdf")
                                        Toast.makeText(context, "✓ تم تصدير PDF", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                    isExporting = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                            enabled = !isExporting
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.PictureAsPdf, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("PDF", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
                border = BorderStroke(1.dp, SMColors.BgCardBorder)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("مبيعات الأسبوع", color = SMColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    if (weeklySales.isNotEmpty()) {
                        val maxVal = weeklySales.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
                        Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                            weeklySales.forEach { (label, value) ->
                                val frac = (value / maxVal).toFloat().coerceAtLeast(0.02f)
                                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                    if (value > 0) Text("${"%.0f".format(value)}", color = SMColors.Primary, fontSize = 8.sp)
                                    Spacer(Modifier.height(2.dp))
                                    Box(Modifier.fillMaxWidth(0.55f).fillMaxHeight(frac).background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(listOf(SMColors.Primary, SMColors.PrimaryDark)),
                                        RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                                    ))
                                    Spacer(Modifier.height(4.dp))
                                    Text(label, color = SMColors.TextMuted, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
                border = BorderStroke(1.dp, SMColors.BgCardBorder)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الملخص المالي", color = SMColors.Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    FinRow("إجمالي المبيعات",   "${"%.2f".format(stats.sales)} ر",       SMColors.Primary)
                    FinRow("تكلفة البضاعة",      "${"%.2f".format(stats.cogs)} ر",        SMColors.AccentYellow)
                    FinRow("إجمالي الربح",       "${"%.2f".format(stats.grossProfit)} ر", SMColors.AccentCyan)
                    Divider(color = SMColors.BgCardBorder)
                    FinRow("المصروفات",           "${"%.2f".format(stats.expenses)} ر",   SMColors.Error)
                    FinRow("صافي الربح",         "${"%.2f".format(stats.netProfit)} ر",
                        if (stats.netProfit >= 0) SMColors.Primary else SMColors.Error, bold = true)
                }
            }
        }
    }
}

suspend fun exportToExcel(context: Context, sales: List<com.supermarket.app.data.models.Sale>, stats: ReportStats): File = withContext(Dispatchers.IO) {
    val workbook = XSSFWorkbook()
    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    // ورقة المبيعات
    val sheet = workbook.createSheet("المبيعات")
    val header = sheet.createRow(0)
    listOf("رقم الفاتورة", "التاريخ", "الكاشير", "الزبون", "الإجمالي", "طريقة الدفع").forEachIndexed { i, title ->
        header.createCell(i).setCellValue(title)
    }
    sales.forEachIndexed { index, sale ->
        val row = sheet.createRow(index + 1)
        row.createCell(0).setCellValue(sale.invoiceNumber)
        row.createCell(1).setCellValue(fmt.format(Date(sale.createdAt)))
        row.createCell(2).setCellValue(sale.cashierName)
        row.createCell(3).setCellValue(sale.customerName)
        row.createCell(4).setCellValue(sale.total)
        row.createCell(5).setCellValue(sale.paymentMethod.nameAr)
    }

    // ورقة الملخص
    val summarySheet = workbook.createSheet("الملخص المالي")
    listOf(
        "إجمالي المبيعات" to stats.sales,
        "تكلفة البضاعة" to stats.cogs,
        "إجمالي الربح" to stats.grossProfit,
        "المصروفات" to stats.expenses,
        "صافي الربح" to stats.netProfit
    ).forEachIndexed { i, (label, value) ->
        val row = summarySheet.createRow(i)
        row.createCell(0).setCellValue(label)
        row.createCell(1).setCellValue(value)
    }

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        "تقرير_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.xlsx")
    FileOutputStream(file).use { workbook.write(it) }
    workbook.close()
    file
}

suspend fun exportToPdf(context: Context, sales: List<com.supermarket.app.data.models.Sale>, stats: ReportStats): File = withContext(Dispatchers.IO) {
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        "تقرير_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pdf")

    val writer = PdfWriter(file)
    val pdf = PdfDocument(writer)
    val document = Document(pdf)
    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    document.add(Paragraph("تقرير المبيعات - سوبرماركت")
        .setFontSize(18f).setBold().setTextAlignment(TextAlignment.CENTER))
    document.add(Paragraph("تاريخ التقرير: ${fmt.format(Date())}")
        .setFontSize(10f).setTextAlignment(TextAlignment.CENTER))
    document.add(Paragraph(" "))

    // الملخص المالي
    document.add(Paragraph("الملخص المالي").setFontSize(14f).setBold())
    val summaryTable = Table(floatArrayOf(200f, 150f))
    listOf(
        "إجمالي المبيعات" to "${"%.2f".format(stats.sales)} ر",
        "تكلفة البضاعة" to "${"%.2f".format(stats.cogs)} ر",
        "إجمالي الربح" to "${"%.2f".format(stats.grossProfit)} ر",
        "المصروفات" to "${"%.2f".format(stats.expenses)} ر",
        "صافي الربح" to "${"%.2f".format(stats.netProfit)} ر"
    ).forEach { (label, value) ->
        summaryTable.addCell(label)
        summaryTable.addCell(value)
    }
    document.add(summaryTable)
    document.add(Paragraph(" "))

    // جدول المبيعات
    document.add(Paragraph("تفاصيل المبيعات").setFontSize(14f).setBold())
    val table = Table(floatArrayOf(120f, 100f, 80f, 80f, 60f))
    listOf("الفاتورة", "التاريخ", "الكاشير", "الزبون", "الإجمالي").forEach {
        table.addHeaderCell(it)
    }
    sales.forEach { sale ->
        table.addCell(sale.invoiceNumber)
        table.addCell(fmt.format(Date(sale.createdAt)))
        table.addCell(sale.cashierName)
        table.addCell(sale.customerName)
        table.addCell("${"%.2f".format(sale.total)} ر")
    }
    document.add(table)
    document.close()
    file
}

fun shareFile(context: Context, file: File, mimeType: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة التقرير"))
}

@Composable fun ReportKpi(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(Modifier.width(155.dp), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.1f)),
        border = BorderStroke(1.dp, color.copy(0.25f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text(label, color = SMColors.TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable fun FinRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SMColors.TextSecondary, fontSize = 13.sp)
        Text(value, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, fontSize = if (bold) 15.sp else 13.sp)
    }
}
