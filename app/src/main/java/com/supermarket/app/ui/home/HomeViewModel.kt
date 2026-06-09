package com.supermarket.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.local.ProductDao
import com.supermarket.app.data.local.SaleDao
import com.supermarket.app.data.models.*
import com.supermarket.app.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats

    private val _lowStockCount = MutableStateFlow(0)
    val lowStockCount: StateFlow<Int> = _lowStockCount

    private val _unreadNotifications = MutableStateFlow(0)
    val unreadNotifications: StateFlow<Int> = _unreadNotifications

    val lowStockProducts: StateFlow<List<Product>> = productDao.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSales: StateFlow<List<Sale>> = saleDao.getAllSales()
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _weeklySales = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val weeklySales: StateFlow<List<Pair<String, Double>>> = _weeklySales

    init {
        _currentUser.value = prefsManager.getUser()
        loadStats()
        loadWeeklyData()
        observeLowStock()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val end = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
            val startDay = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startMonth = cal.timeInMillis

            val todaySales  = saleDao.getTotalSalesAmount(startDay, end) ?: 0.0
            val todayTx     = saleDao.getSalesCount(startDay, end)
            val monthSales  = saleDao.getTotalSalesAmount(startMonth, end) ?: 0.0
            val totalProds  = productDao.getTotalProductsCount()

            _stats.value = DashboardStats(
                todaySales        = todaySales,
                todayTransactions = todayTx,
                monthSales        = monthSales,
                totalProducts     = totalProds,
                lowStockProducts  = _lowStockCount.value
            )
        }
    }

    private fun observeLowStock() {
        viewModelScope.launch {
            productDao.getLowStockProducts().collect { products ->
                _lowStockCount.value = products.size
                _stats.update { it.copy(lowStockProducts = products.size) }
            }
        }
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            val dayFormat = SimpleDateFormat("EEE", Locale("ar"))
            val result = mutableListOf<Pair<String, Double>>()
            for (i in 6 downTo 0) {
                val dayCal = Calendar.getInstance()
                dayCal.add(Calendar.DAY_OF_YEAR, -i)
                dayCal.set(Calendar.HOUR_OF_DAY, 0); dayCal.set(Calendar.MINUTE, 0); dayCal.set(Calendar.SECOND, 0)
                val dayStart = dayCal.timeInMillis
                dayCal.set(Calendar.HOUR_OF_DAY, 23); dayCal.set(Calendar.MINUTE, 59)
                val dayEnd = dayCal.timeInMillis
                val amount = saleDao.getTotalSalesAmount(dayStart, dayEnd) ?: 0.0
                result.add(Pair(dayFormat.format(Date(dayStart)), amount))
            }
            _weeklySales.value = result
        }
    }
}
