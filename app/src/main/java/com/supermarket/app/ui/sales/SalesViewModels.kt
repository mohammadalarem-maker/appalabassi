package com.supermarket.app.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.local.ProductDao
import com.supermarket.app.data.local.SaleDao
import com.supermarket.app.data.models.*
import com.supermarket.app.data.remote.FirebaseRepository
import com.supermarket.app.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class NewSaleViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val firebaseRepository: FirebaseRepository,
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _cartItems  = MutableStateFlow<List<SaleItem>>(emptyList())
    val cartItems: StateFlow<List<SaleItem>> = _cartItems

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _discount   = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount

    private val _isLoading  = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val searchResults: StateFlow<List<Product>> = _searchQuery
        .debounce(250)
        .flatMapLatest { q -> productDao.searchProducts(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subtotal: StateFlow<Double> = _cartItems
        .map { it.sumOf { item -> item.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val total: StateFlow<Double> = combine(subtotal, _discount) { sub, disc -> (sub - disc).coerceAtLeast(0.0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    fun onSearch(q: String) { _searchQuery.value = q }
    fun setDiscount(v: Double) { _discount.value = v }

    // دالة إلغاء الدفع وتفريغ السلة بالكامل
    fun clearCart() {
        _cartItems.value = emptyList()
        _discount.value = 0.0
        _searchQuery.value = ""
    }

    fun addToCart(product: Product) {
        val existing = _cartItems.value.find { it.productId == product.id }
        if (existing != null) {
            _cartItems.update { items ->
                items.map { if (it.productId == product.id) it.copy(quantity = it.quantity + 1, total = (it.quantity + 1) * it.unitPrice) else it }
            }
        } else {
            _cartItems.update { it + SaleItem(product.id, product.name, product.barcode, 1.0, product.unit, product.sellPrice, 0.0, product.sellPrice) }
        }
    }

    fun increaseQty(id: String) {
        _cartItems.update { items -> items.map { if (it.productId == id) it.copy(quantity = it.quantity + 1, total = (it.quantity + 1) * it.unitPrice) else it } }
    }

    fun decreaseQty(id: String) {
        _cartItems.update { items ->
            items.mapNotNull {
                if (it.productId == id) {
                    if (it.quantity <= 1) null else it.copy(quantity = it.quantity - 1, total = (it.quantity - 1) * it.unitPrice)
                } else it
            }
        }
    }

    fun removeFromCart(id: String) { _cartItems.update { it.filter { item -> item.productId != id } } }

    fun completeSale(
        customerName: String, 
        paidAmount: Double, 
        paymentMethod: PaymentMethod, 
        printerType: String = "NONE", // "BT" أو "WIFI" أو "NONE"
        printerAddress: String = "",  // Mac Address للبلوتوث أو IP للوايرلس
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val user = prefsManager.getUser()
            val invoiceNum = "SM-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())}"
            val t = total.value
            val sale = Sale(
                id            = UUID.randomUUID().toString(),
                invoiceNumber = invoiceNum,
                customerName  = customerName,
                items         = _cartItems.value,
                subtotal      = subtotal.value,
                discount      = _discount.value,
                total         = t,
                paidAmount    = paidAmount,
                changeAmount  = (paidAmount - t).coerceAtLeast(0.0),
                paymentMethod = paymentMethod,
                cashierId     = user?.uid ?: "",
                cashierName   = user?.username ?: "",
                status        = SaleStatus.COMPLETED
            )

            // 1. الحفظ المحلي وتحديث المخزون
            saleDao.insertSale(sale)
            sale.items.forEach { item -> productDao.decreaseStock(item.productId, item.quantity.toInt()) }
            
            // 2. الرفع السحابي والتحقق من نواقص المخزون
            firebaseRepository.addSale(sale)
            sale.items.forEach { item ->
                val p = productDao.getProductById(item.productId)
                if (p != null && p.quantity <= p.minQuantity) firebaseRepository.sendLowStockNotification(p)
            }

            // 3. طباعة الفاتورة تلقائياً إذا كانت مفعلة
            if (printerType != "NONE" && printerAddress.isNotBlank()) {
                val receiptText = generateReceiptText(sale)
                if (printerType == "BT") {
                    printViaBluetooth(printerAddress, receiptText)
                } else if (printerType == "WIFI") {
                    printViaWifi(printerAddress, 9100, receiptText)
                }
            }

            _isLoading.value = false
            _cartItems.value = emptyList()
            _discount.value  = 0.0
            _searchQuery.value = ""
            onSuccess()
        }
    }

    // صياغة نص الفاتورة الحرارية بالتنسيق القياسي ESC/POS
    private fun generateReceiptText(sale: Sale): String {
        val sb = StringBuilder()
        sb.append("      سوبرماركت الحسام      \n")
        sb.append("--------------------------------\n")
        sb.append("رقم الفاتورة: ${sale.invoiceNumber}\n")
        sb.append("التاريخ: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())}\n")
        sb.append("الكاشير: ${sale.cashierName}\n")
        sb.append("--------------------------------\n")
        sb.append("الصنف          الكمية     الإجمالي\n")
        sb.append("--------------------------------\n")
        sale.items.forEach { item ->
            val namePadded = item.productName.padEnd(14).take(14)
            val qtyPadded = item.quantity.toInt().toString().padEnd(6)
            sb.append("$namePadded $qtyPadded ${item.total} ر.ي\n")
        }
        sb.append("--------------------------------\n")
        sb.append("المجموع: ${sale.subtotal} ر.ي\n")
        sb.append("الخصم: ${sale.discount} ر.ي\n")
        sb.append("الإجمالي النهائي: ${sale.total} ر.ي\n")
        sb.append("--------------------------------\n")
        sb.append("    شكراً لزيارتكم وثقتكم بنا    \n\n\n\n")
        return sb.toString()
    }

    private suspend fun printViaBluetooth(macAddress: String, text: String) = withContext(Dispatchers.IO) {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@withContext
            val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            val outputStream = socket.outputStream
            outputStream.write(text.toByteArray(charset("Cp1256")))
            outputStream.flush()
            outputStream.close()
            socket.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private suspend fun printViaWifi(ip: String, port: Int, text: String) = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 3000)
            val outputStream = socket.getOutputStream()
            outputStream.write(text.toByteArray(charset("Cp1256")))
            outputStream.flush()
            outputStream.close()
            socket.close()
        } catch (e: Exception) { e.printStackTrace() }
    }
}

@HiltViewModel
class SalesViewModel @Inject constructor(private val saleDao: SaleDao) : ViewModel() {
    val sales: StateFlow<List<Sale>> = saleDao.getAllSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySalesByCashier: StateFlow<Map<String, Double>> = sales.map { list ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startDay = cal.timeInMillis
        list.filter { it.createdAt >= startDay && it.status == SaleStatus.COMPLETED }
            .groupBy { it.cashierName.ifEmpty { "غير معروف" } }
            .mapValues { entry -> entry.value.sumOf { it.total } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _totalToday = MutableStateFlow(0.0)
    val totalToday: StateFlow<Double> = _totalToday

    private val _totalMonth = MutableStateFlow(0.0)
    val totalMonth: StateFlow<Double> = _totalMonth

    init {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val end = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
            val startDay = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startMonth = cal.timeInMillis
            _totalToday.value = saleDao.getTotalSalesAmount(startDay, end) ?: 0.0
            _totalMonth.value = saleDao.getTotalSalesAmount(startMonth, end) ?: 0.0
        }
    }
}
