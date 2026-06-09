package com.supermarket.app.ui.expenses
import androidx.lifecycle.kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
import androidx.lifecycle.viewModel.kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)

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
import com.supermarket.app.data.local.ExpenseDao
import com.supermarket.app.data.models.Expense
import com.supermarket.app.data.models.ExpenseCategory
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
class ExpensesViewModel @Inject constructor(
    private val dao: ExpenseDao,
    private val repo: FirebaseRepository,
    private val prefs: PrefsManager
) : androidx.lifecycle.ViewModel() {
    val expenses: StateFlow<List<Expense>> = dao.getAllExpenses()
        .stateIn(androidx.lifecycle.viewModel.kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main), SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalMonth = MutableStateFlow(0.0)
    val totalMonth: StateFlow<Double> = _totalMonth

    init { loadMonthTotal() }

    private fun loadMonthTotal() {
        androidx.lifecycle.viewModel.kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val cal = Calendar.getInstance()
            val end = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, 1)
            _totalMonth.value = dao.getTotalExpenses(cal.timeInMillis, end) ?: 0.0
        }
    }

    fun add(title: String, amount: Double, category: ExpenseCategory, desc: String) {
        androidx.lifecycle.viewModel.kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val e = Expense(UUID.randomUUID().toString(), title, amount, category, desc, prefs.getUser()?.uid ?: "")
            dao.insertExpense(e)
            repo.addExpense(e)
            loadMonthTotal()
        }
    }

    fun delete(id: String) {
        androidx.lifecycle.viewModel.kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch { dao.deleteExpense(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(vm: ExpensesViewModel = hiltViewModel()) {
    val expenses    by vm.expenses.collectAsState()
    val totalMonth  by vm.totalMonth.collectAsState()
    val dateFormat  = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    var showAdd     by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(SMColors.BgDeep).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Monthly total card
        Card(
            Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SMColors.Error.copy(0.08f)),
            border = BorderStroke(1.dp, SMColors.Error.copy(0.25f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("مصروفات الشهر", color = SMColors.TextSecondary, fontSize = 12.sp)
                Text("${"%.2f".format(totalMonth)} ر", color = SMColors.Error,
                    fontWeight = FontWeight.Black, fontSize = 24.sp)
            }
        }

        Text("سجل المصروفات", color = SMColors.TextSecondary,
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (expenses.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💸", fontSize = 48.sp)
                            Spacer(Modifier.height(10.dp))
                            Text("لا توجد مصروفات", color = SMColors.TextMuted)
                        }
                    }
                }
            }
            items(expenses, key = { it.id }) { expense ->
                Card(
                    Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
                    border = BorderStroke(1.dp, SMColors.BgCardBorder)
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                Modifier.size(42.dp).background(SMColors.Error.copy(0.12f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text("💸", fontSize = 20.sp) }
                            Column {
                                Text(expense.title, color = SMColors.TextPrimary,
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(expense.category.nameAr, color = SMColors.TextSecondary, fontSize = 11.sp)
                                Text(dateFormat.format(Date(expense.createdAt)), color = SMColors.TextMuted, fontSize = 10.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${"%.2f".format(expense.amount)} ر",
                                color = SMColors.Error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton({ vm.delete(expense.id) }, Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Delete, null, tint = SMColors.Error.copy(0.5f),
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { showAdd = true },
            containerColor = SMColors.Error,
            modifier = Modifier.padding(24.dp)
        ) { Icon(Icons.Filled.Add, null, tint = Color.White) }
    }

    if (showAdd) {
        var title    by remember { mutableStateOf("") }
        var amount   by remember { mutableStateOf("") }
        var desc     by remember { mutableStateOf("") }
        var cat      by remember { mutableStateOf(ExpenseCategory.OTHER) }
        var expanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            containerColor = SMColors.BgCard,
            title = { Text("إضافة مصروف", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(title, { title = it }, label = { Text("العنوان *") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                    OutlinedTextField(amount, { amount = it }, label = { Text("المبلغ *") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    ExposedDropdownMenuBox(expanded, { expanded = it }) {
                        OutlinedTextField(cat.nameAr, {}, readOnly = true, label = { Text("الفئة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                        ExposedDropdownMenu(expanded, { expanded = false },
                            modifier = Modifier.background(SMColors.BgCard)) {
                            ExpenseCategory.values().forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.nameAr, color = SMColors.TextPrimary) },
                                    onClick = { cat = c; expanded = false })
                            }
                        }
                    }
                    OutlinedTextField(desc, { desc = it }, label = { Text("الوصف") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = smOutlinedColors(), maxLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val a = amount.toDoubleOrNull() ?: return@Button
                        if (title.isNotEmpty()) { vm.add(title, a, cat, desc); showAdd = false }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SMColors.Error)
                ) { Text("إضافة", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton({ showAdd = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }
}
