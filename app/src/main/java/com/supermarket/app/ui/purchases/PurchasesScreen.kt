package com.supermarket.app.ui.purchases
import com.supermarket.app.ui.smOutlinedColors
import com.supermarket.app.ui.smOutlinedColors

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.local.PurchaseDao
import com.supermarket.app.data.models.*
import com.supermarket.app.data.remote.FirebaseRepository
import com.supermarket.app.ui.inventory.smOutlinedColors
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PurchasesViewModel @Inject constructor(
    private val dao: PurchaseDao,
    private val repo: FirebaseRepository,
    private val prefs: PrefsManager
) : ViewModel() {
    val purchases: StateFlow<List<Purchase>> = dao.getAllPurchases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPurchase(supplier: String, cost: Double, items: List<PurchaseItem>, paid: Double) {
        viewModelScope.launch {
            val p = Purchase(
                id = UUID.randomUUID().toString(),
                supplierName = supplier,
                items = items,
                totalCost = cost,
                paidAmount = paid,
                remainingAmount = (cost - paid).coerceAtLeast(0.0),
                createdById = prefs.getUser()?.uid ?: "",
                status = if (paid >= cost) PurchaseStatus.COMPLETED else PurchaseStatus.PARTIAL
            )
            dao.insertPurchase(p)
        }
    }
}

@Composable
fun PurchasesScreen(vm: PurchasesViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val purchases by vm.purchases.collectAsState()
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(SMColors.BgDeep).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val totalOwed = purchases.sumOf { it.remainingAmount }
        if (totalOwed > 0) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SMColors.Error.copy(0.1f)), border = BorderStroke(1.dp, SMColors.Error.copy(0.3f))) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("إجمالي المستحقات", color = SMColors.TextSecondary, fontSize = 12.sp)
                        Text("${"%.2f".format(totalOwed)} ر", color = SMColors.Error, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Icon(Icons.Filled.Warning, null, tint = SMColors.Error, modifier = Modifier.size(32.dp))
                }
            }
        }

        Text("سجل المشتريات (${purchases.size})", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            if (purchases.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚚", fontSize = 48.sp); Spacer(Modifier.height(10.dp)); Text("لا توجد مشتريات", color = SMColors.TextMuted)
                        }
                    }
                }
            }
            items(purchases, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = SMColors.BgCard), border = BorderStroke(1.dp, SMColors.BgCardBorder)) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).background(SMColors.AccentOrange.copy(0.12f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("🚚", fontSize = 20.sp) }
                            Column {
                                Text(p.supplierName.ifEmpty { "مورد غير محدد" }, color = SMColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${p.items.size} صنف", color = SMColors.TextSecondary, fontSize = 11.sp)
                                Text(dateFormat.format(Date(p.createdAt)), color = SMColors.TextMuted, fontSize = 10.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${"%.2f".format(p.totalCost)} ر", color = SMColors.AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (p.remainingAmount > 0) Text("متبقي ${"%.2f".format(p.remainingAmount)} ر", color = SMColors.Error, fontSize = 10.sp) else Text("مدفوع بالكامل ✓", color = SMColors.Primary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = { showAdd = true }, containerColor = SMColors.AccentOrange, modifier = Modifier.padding(24.dp)) { Icon(Icons.Filled.Add, null, tint = Color.White) }
    }

    if (showAdd) {
        var supplier by remember { mutableStateOf("") }; var total by remember { mutableStateOf("") }; var paid by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false }, containerColor = SMColors.BgCard,
            title = { Text("إضافة مشترى", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(supplier, { supplier = it }, label = { Text("اسم المورد") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                    OutlinedTextField(total, { total = it }, label = { Text("إجمالي التكلفة *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(paid, { paid = it }, label = { Text("المبلغ المدفوع") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            },
            confirmButton = {
                Button(onClick = { val t = total.toDoubleOrNull() ?: return@Button; val p = paid.toDoubleOrNull() ?: t; vm.addPurchase(supplier, t, emptyList(), p); showAdd = false }, colors = ButtonDefaults.buttonColors(containerColor = SMColors.AccentOrange)) { Text("إضافة", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton({ showAdd = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }
}