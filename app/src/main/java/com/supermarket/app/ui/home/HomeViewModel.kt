package com.supermarket.app.ui.home
import com.supermarket.app.ui.smOutlinedColors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.local.ProductDao
import com.supermarket.app.data.local.SaleDao
import com.supermarket.app.data.models.*
import com.supermarket.app.data.remote.SyncRepository
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
    private val prefsManager: PrefsManager,
    private val syncRepository: SyncRepository
) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats
    private val _lowStockCount = MutableStateFlow(0)

    val lowStockProducts: StateFlow<List<Product>> = productDao.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentSales: StateFlow<List<Sale>> = saleDao.getAllSales()
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _weeklySales = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val weeklySales: StateFlow<List<Pair<String, Double>>> = _weeklySales

    init {
        _currentUser.value = prefsManager.getUser()
        viewModelScope.launch {
            syncRepository.syncAll()
            loadStats()
            loadWeeklyData()
        }
        observeLowStock()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val end = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
            val todaySales = saleDao.getTotalSalesAmount(cal.timeInMillis, end) ?: 0.0
            val todayTx    = saleDao.getSalesCount(cal.timeInMillis, end)
            val totalProds = productDao.getTotalProductsCount()
            _stats.value = DashboardStats(
                todaySales = todaySales, todayTransactions = todayTx,
                totalProducts = totalProds, lowStockProducts = _lowStockCount.value
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
            val fmt = SimpleDateFormat("EEE", Locale("ar"))
            val result = mutableListOf<Pair<String, Double>>()
            for (i in 6 downTo 0) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -i)
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0)
                val dayStart = c.timeInMillis
                c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59)
                val amount = saleDao.getTotalSalesAmount(dayStart, c.timeInMillis) ?: 0.0
                result.add(Pair(fmt.format(Date(dayStart)), amount))
            }
            _weeklySales.value = result
        }
    }
}
