package com.supermarket.app.ui.settings
import com.supermarket.app.ui.smOutlinedColors

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.supermarket.app.data.remote.FirebaseRepository
import com.supermarket.app.ui.components.SMField
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PrefsManager,
    private val repo: FirebaseRepository
) : androidx.lifecycle.ViewModel() {
    private val _storeName     = MutableStateFlow(prefs.getStoreName())
    val storeName: StateFlow<String> = _storeName
    private val _currency      = MutableStateFlow(prefs.getCurrency())
    val currency: StateFlow<String> = _currency
    private val _taxRate       = MutableStateFlow(prefs.getTaxRate())
    val taxRate: StateFlow<Double> = _taxRate
    private val _lowStock      = MutableStateFlow(prefs.getLowStockThreshold())
    val lowStockThreshold: StateFlow<Int> = _lowStock
    private val _notif         = MutableStateFlow(prefs.isNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notif

    fun setStoreName(v: String)    { _storeName.value = v; prefs.setStoreName(v) }
    fun setCurrency(v: String)     { _currency.value = v; prefs.setCurrency(v) }
    fun setTaxRate(v: Double)      { _taxRate.value = v; prefs.setTaxRate(v) }
    fun setLowStock(v: Int)        { _lowStock.value = v; prefs.setLowStockThreshold(v) }
    fun setNotif(v: Boolean)       { _notif.value = v; prefs.setNotificationsEnabled(v) }
    fun logout()                   { repo.logout(); prefs.logout() }
}

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val storeName  by vm.storeName.collectAsState()
    val currency   by vm.currency.collectAsState()
    val taxRate    by vm.taxRate.collectAsState()
    val lowStock   by vm.lowStockThreshold.collectAsState()
    val notif      by vm.notificationsEnabled.collectAsState()
    var showLogout by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(SMColors.BgDeep)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SMSettingsCard("إعدادات المحل", Icons.Filled.Store) {
            SMField("اسم المحل", storeName, Icons.Filled.Store) { vm.setStoreName(it) }
            SMField("العملة", currency, Icons.Filled.AttachMoney) { vm.setCurrency(it) }
            SMField("نسبة الضريبة (%)", taxRate.toString(), Icons.Filled.Percent, KeyboardType.Decimal) { vm.setTaxRate(it.toDoubleOrNull() ?: 0.0) }
        }

        SMSettingsCard("إعدادات الإشعارات", Icons.Filled.Notifications) {
            Row(
                Modifier.fillMaxWidth().background(SMColors.BgSurface, RoundedCornerShape(14.dp)).padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("تفعيل الإشعارات", color = SMColors.TextPrimary, fontSize = 14.sp)
                    Text("إشعارات المبيعات ونفاد المخزون", color = SMColors.TextMuted, fontSize = 11.sp)
                }
                Switch(notif, { vm.setNotif(it) }, colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black, checkedTrackColor = SMColors.Primary))
            }
            SMField("حد تنبيه المخزون (قطعة)", lowStock.toString(), Icons.Filled.NotificationImportant, KeyboardType.Number) {
                vm.setLowStock(it.toIntOrNull() ?: 10)
            }
        }

        SMSettingsCard("حول التطبيق", Icons.Filled.Info) {
            InfoLine("اسم التطبيق", "سوبرماركت")
            InfoLine("الإصدار", "1.0.0")
            InfoLine("المطور", "كلود")
            InfoLine("بواسطة", "محمد الصارم")
            InfoLine("البريد الإلكتروني", "Mohammedalsarem6@gmail.com")
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = { showLogout = true },
            Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SMColors.Error.copy(0.12f)),
            border = BorderStroke(1.dp, SMColors.Error.copy(0.4f))
        ) {
            Icon(Icons.Filled.Logout, null, tint = SMColors.Error)
            Spacer(Modifier.width(8.dp))
            Text("تسجيل الخروج", color = SMColors.Error, fontWeight = FontWeight.SemiBold)
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("تطوير: كلود • بواسطة محمد الصارم", color = SMColors.TextMuted, fontSize = 11.sp)
            Text("جميع الحقوق محفوظة © 2024", color = SMColors.TextMuted, fontSize = 10.sp)
        }
        Spacer(Modifier.height(20.dp))
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            containerColor = SMColors.BgCard,
            title = { Text("تسجيل الخروج", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text  = { Text("هل أنت متأكد من تسجيل الخروج؟", color = SMColors.TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { vm.logout(); onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = SMColors.Error)
                ) { Text("خروج", color = Color.White) }
            },
            dismissButton = { TextButton({ showLogout = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }
}

@Composable
fun SMSettingsCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
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
fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SMColors.TextSecondary, fontSize = 13.sp)
        Text(value, color = SMColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
