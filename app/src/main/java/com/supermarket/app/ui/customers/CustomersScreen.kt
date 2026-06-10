// ============================
// CUSTOMERS SCREEN
// ============================
package com.supermarket.app.ui.customers
import com.supermarket.app.ui.smOutlinedColors

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.local.CustomerDao
import com.supermarket.app.data.models.Customer
import com.supermarket.app.data.models.MembershipLevel
import com.supermarket.app.data.remote.FirebaseRepository
import com.supermarket.app.ui.theme.SMColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val dao: CustomerDao,
    private val repo: FirebaseRepository
) : ViewModel() {
    private val _q = MutableStateFlow("")
    val customers: StateFlow<List<Customer>> = _q.debounce(300).flatMapLatest { q ->
        if (q.isEmpty()) dao.getAllCustomers() else dao.searchCustomers(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun search(q: String) { _q.value = q }
    
    fun add(name: String, phone: String, email: String) {
        viewModelScope.launch {
            val c = Customer(UUID.randomUUID().toString(), name, phone, email)
            dao.insertCustomer(c); repo.addCustomer(c)
        }
    }
}

@Composable
fun CustomersScreen(vm: CustomersViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()

    val customers by vm.customers.collectAsState()
    var q         by remember { mutableStateOf("") }
    var showAdd   by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(SMColors.BgDeep).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(q, { q = it; vm.search(it) }, placeholder = { Text("بحث بالاسم أو الهاتف...") },
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = SMColors.TextSecondary) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = smOutlinedColors(), singleLine = true)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(customers, key = { it.id }) { c ->
                val lvlColor = Color(c.membershipLevel.color)
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
                    border = BorderStroke(1.dp, SMColors.BgCardBorder)
                ) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(lvlColor.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(c.name.firstOrNull()?.uppercase() ?: "ع", color = lvlColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = SMColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            if (c.phone.isNotEmpty()) Text(c.phone, color = SMColors.TextSecondary, fontSize = 12.sp)
                            Text("${c.purchaseCount} مشتريات • ${"%.2f".format(c.totalPurchases)} ر", color = SMColors.Primary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("⭐ ${c.loyaltyPoints}", color = SMColors.AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(c.membershipLevel.nameAr, color = lvlColor, fontSize = 10.sp)
                        }
                    }
                }
            }
            if (customers.isEmpty()) item {
                Box(Modifier.fillParentMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥", fontSize = 48.sp); Spacer(Modifier.height(10.dp))
                        Text("لا يوجد عملاء", color = SMColors.TextMuted)                                           }
                }
            }
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = { showAdd = true }, containerColor = SMColors.Primary,
            modifier = Modifier.padding(24.dp)) { Icon(Icons.Filled.PersonAdd, null, tint = Color.Black) }
    }
    if (showAdd) {
        var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showAdd = false }, containerColor = SMColors.BgCard,
            title = { Text("إضافة عميل", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("الاسم *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                    OutlinedTextField(phone, { phone = it }, label = { Text("الهاتف") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                    OutlinedTextField(email, { email = it }, label = { Text("البريد الإلكتروني") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())                                                                                                         }
            },
            confirmButton = { Button({ if (name.isNotEmpty()) { vm.add(name, phone, email); showAdd = false } }, colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)) { Text("إضافة", color = Color.Black) } },
            dismissButton = { TextButton({ showAdd = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }
}
