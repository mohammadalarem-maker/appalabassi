package com.supermarket.app.ui.reports
import com.supermarket.app.ui.smOutlinedColors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.local.ExpenseDao
import com.supermarket.app.data.local.SaleDao
import com.supermarket.app.data.models.Sale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ReportStats(
    val sales: Double = 0.0, val purchases: Double = 0.0,
    val expenses: Double = 0.0, val cogs: Double = 0.0,
    val grossProfit: Double = 0.0, val netProfit: Double = 0.0
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val saleDao: SaleDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _stats = MutableStateFlow(ReportStats())
    val stats: StateFlow<ReportStats> = _stats

    private val _weeklySales = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val weeklySales: StateFlow<List<Pair<String, Double>>> = _weeklySales

    private val _salesList = MutableStateFlow<List<Sale>>(emptyList())
    val salesList: StateFlow<List<Sale>> = _salesList

    private var currentStart = 0L
    private var currentEnd = 0L

    init { loadPeriod(1) }

    fun loadPeriod(period: Int) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            currentEnd = cal.timeInMillis
            currentStart = when (period) {
                0 -> { cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.timeInMillis }
                1 -> { cal.add(Calendar.DAY_OF_YEAR, -7); cal.timeInMillis }
                2 -> { cal.set(Calendar.DAY_OF_MONTH, 1); cal.timeInMillis }
                else -> { cal.set(Calendar.DAY_OF_YEAR, 1); cal.timeInMillis }
            }
            val sales    = saleDao.getTotalSalesAmount(currentStart, currentEnd) ?: 0.0
            val expenses = expenseDao.getTotalExpenses(currentStart, currentEnd) ?: 0.0
            val cogs     = sales * 0.65
            val gross    = sales - cogs
            _stats.value = ReportStats(sales, 0.0, expenses, cogs, gross, gross - expenses)
            _salesList.value = saleDao.getSalesBetween(currentStart, currentEnd)
            loadWeekly()
        }
    }

    private fun loadWeekly() {
        viewModelScope.launch {
            val fmt    = SimpleDateFormat("EEE", Locale("ar"))
            val result = mutableListOf<Pair<String, Double>>()
            for (i in 6 downTo 0) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -i)
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0)
                val s = c.timeInMillis
                c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59)
                val e = c.timeInMillis
                result.add(Pair(fmt.format(Date(s)), saleDao.getTotalSalesAmount(s, e) ?: 0.0))
            }
            _weeklySales.value = result
        }
    }
}
