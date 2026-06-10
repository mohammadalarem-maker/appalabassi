package com.supermarket.app.ui.inventory
import com.supermarket.app.ui.smOutlinedColors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.local.ProductDao
import com.supermarket.app.data.models.Product
import com.supermarket.app.data.models.ProductCategory
import com.supermarket.app.data.remote.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    private val _searchQuery      = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val _selectedCategory = MutableStateFlow<ProductCategory?>(null)
    val selectedCategory: StateFlow<ProductCategory?> = _selectedCategory

    val products: StateFlow<List<Product>> = combine(
        productDao.getAllProducts(), _searchQuery, _selectedCategory
    ) { prods, q, cat ->
        prods.filter { p ->
            val matchQ   = q.isEmpty() || p.name.contains(q, true) || p.barcode.contains(q, true)
            val matchCat = cat == null || p.category == cat
            matchQ && matchCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchChange(q: String)             { _searchQuery.value = q }
    fun onCategoryFilter(c: ProductCategory?) { _selectedCategory.value = c }
    fun deleteProduct(id: String) {
        viewModelScope.launch {
            productDao.deleteProduct(id)
            firebaseRepository.deleteProduct(id)
        }
    }
}

data class ProductFormState(
    val name: String              = "",
    val barcode: String           = "",
    val brand: String             = "",
    val unit: String              = "قطعة",
    val category: ProductCategory = ProductCategory.FOOD,
    val buyPrice: String          = "",
    val sellPrice: String         = "",
    val quantity: String          = "",
    val minQuantity: String       = "10",
    val location: String          = "",
    val description: String       = "",
    val expiryDate: String        = "",
    val isWeighed: Boolean        = false,
    val imageUrl: String          = ""
)

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    private val _state     = MutableStateFlow(ProductFormState())
    val state: StateFlow<ProductFormState> = _state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _error     = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private var editingId: String? = null

    fun loadProduct(id: String) {
        viewModelScope.launch {
            editingId = id
            val p = productDao.getProductById(id) ?: return@launch
            _state.value = ProductFormState(
                name        = p.name,        barcode     = p.barcode,
                brand       = p.brand,       unit        = p.unit,
                category    = p.category,    buyPrice    = p.buyPrice.toString(),
                sellPrice   = p.sellPrice.toString(),
                quantity    = p.quantity.toString(),
                minQuantity = p.minQuantity.toString(),
                location    = p.location,    description = p.description,
                isWeighed   = p.isWeighed,   imageUrl    = p.imageUrl
            )
        }
    }

    fun update(block: ProductFormState.() -> ProductFormState) { _state.update(block) }

    fun saveProduct(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.name.isBlank())      { _error.value = "يرجى إدخال اسم المنتج"; return }
        if (s.sellPrice.isBlank()) { _error.value = "يرجى إدخال سعر البيع";  return }
        _error.value = null; _isLoading.value = true
        viewModelScope.launch {
            val product = Product(
                id          = editingId ?: UUID.randomUUID().toString(),
                name        = s.name.trim(),        barcode     = s.barcode.trim(),
                brand       = s.brand.trim(),        unit        = s.unit.trim().ifEmpty { "قطعة" },
                category    = s.category,
                buyPrice    = s.buyPrice.toDoubleOrNull() ?: 0.0,
                sellPrice   = s.sellPrice.toDoubleOrNull() ?: 0.0,
                quantity    = s.quantity.toIntOrNull() ?: 0,
                minQuantity = s.minQuantity.toIntOrNull() ?: 10,
                location    = s.location.trim(),     description = s.description.trim(),
                isWeighed   = s.isWeighed,           imageUrl    = s.imageUrl,
                updatedAt   = System.currentTimeMillis()
            )
            productDao.insertProduct(product)
            firebaseRepository.addProduct(product)
            if (product.quantity <= product.minQuantity)
                firebaseRepository.sendLowStockNotification(product)
            _isLoading.value = false
            onSuccess()
        }
    }
}
