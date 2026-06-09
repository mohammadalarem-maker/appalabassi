// ============================
// REPORTS SCREEN
// ============================
package com.supermarket.app.ui.reports

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarket.app.ui.theme.SMColors

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val stats       by viewModel.stats.collectAsState()
    val weeklySales by viewModel.weeklySales.collectAsState()
    var period      by remember { mutableStateOf(1) }
    val periods = listOf("اليوم", "الأسبوع", "الشهر", "السنة")

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
