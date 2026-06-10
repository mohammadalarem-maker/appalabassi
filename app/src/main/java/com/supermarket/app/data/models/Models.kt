package com.supermarket.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

// ============================
// USER MODEL
// ============================
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CASHIER,
    @get:PropertyName("active")
    @set:PropertyName("active")
    @PropertyName("active")
    var isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = 0L,
    val profileImage: String = ""
)

enum class UserRole(val displayName: String, val nameAr: String) {
    ADMIN("Admin", "مدير"),
    MANAGER("Manager", "مشرف"),
    CASHIER("Cashier", "كاشير"),
    VIEWER("Viewer", "مشاهد فقط")
}

// ============================
// PRODUCT MODEL - سوبرماركت
// ============================
@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val nameAr: String = "",
    val barcode: String = "",
    val category: ProductCategory = ProductCategory.FOOD,
    val brand: String = "",
    val unit: String = "قطعة",        // قطعة / كيلو / لتر / علبة
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val quantity: Int = 0,
    val minQuantity: Int = 10,
    val description: String = "",
    val imageUrl: String = "",
    @get:PropertyName("active")
    @set:PropertyName("active")
    @PropertyName("active")
    var isActive: Boolean = true,
    val expiryDate: Long = 0L,        // تاريخ انتهاء الصلاحية
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val location: String = "",         // رف / قسم
    val taxRate: Double = 0.0,
    @get:PropertyName("weighed")
    @set:PropertyName("weighed")
    @PropertyName("weighed")
    var isWeighed: Boolean = false     // منتج يُوزن
)

enum class ProductCategory(val nameAr: String, val emoji: String, val color: Long) {
    FOOD(        "مواد غذائية",       "🥫", 0xFF4CAF50),
    VEGETABLES(  "خضروات وفواكه",    "🥦", 0xFF8BC34A),
    DAIRY(       "ألبان وأجبان",     "🥛", 0xFF03A9F4),
    MEAT(        "لحوم ودواجن",      "🥩", 0xFFE91E63),
    BAKERY(      "مخبوزات",          "🍞", 0xFFFF9800),
    BEVERAGES(   "مشروبات",          "🧃", 0xFF9C27B0),
    CLEANING(    "منظفات",           "🧹", 0xFF00BCD4),
    PERSONAL(    "عناية شخصية",      "🧴", 0xFFFF5722),
    FROZEN(      "مجمدات",           "❄️", 0xFF607D8B),
    SNACKS(      "وجبات خفيفة",     "🍪", 0xFFFFC107),
    BABY(        "منتجات أطفال",     "👶", 0xFFE040FB),
    OTHER(       "أخرى",             "📦", 0xFF9E9E9E)
}

// ============================
// SALE MODEL
// ============================
@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey val id: String = "",
    val invoiceNumber: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<SaleItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val discountType: DiscountType = DiscountType.AMOUNT,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val paidAmount: Double = 0.0,
    val changeAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String = "",
    val cashierId: String = "",
    val cashierName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: SaleStatus = SaleStatus.COMPLETED,
    val isSynced: Boolean = false,
    val shift: String = ""            // وردية صباحية / مسائية
)

data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val barcode: String = "",
    val quantity: Double = 1.0,       // Double لدعم الوزن
    val unit: String = "قطعة",
    val unitPrice: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0
)

enum class DiscountType { AMOUNT, PERCENTAGE }
enum class SaleStatus { COMPLETED, CANCELLED, RETURNED, PENDING }
enum class PaymentMethod(val nameAr: String, val icon: String) {
    CASH(       "نقدي",     "💵"),
    CARD(       "بطاقة",    "💳"),
    TRANSFER(   "تحويل",   "📲"),
    VOUCHER(    "قسيمة",   "🎟️")
}

// ============================
// CUSTOMER MODEL
// ============================
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val totalPurchases: Double = 0.0,
    val purchaseCount: Int = 0,
    val lastPurchase: Long = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val loyaltyPoints: Int = 0,
    val membershipLevel: MembershipLevel = MembershipLevel.REGULAR
)

enum class MembershipLevel(val nameAr: String, val color: Long) {
    REGULAR(  "عادي",   0xFF9E9E9E),
    SILVER(   "فضي",    0xFF9E9E9E),
    GOLD(     "ذهبي",   0xFFFFD700),
    PLATINUM( "بلاتيني",0xFF00BCD4)
}

// ============================
// PURCHASE / SUPPLY MODEL
// ============================
@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey val id: String = "",
    val supplierName: String = "",
    val supplierPhone: String = "",
    val items: List<PurchaseItem> = emptyList(),
    val totalCost: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val invoiceNumber: String = "",
    val notes: String = "",
    val createdById: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: PurchaseStatus = PurchaseStatus.COMPLETED,
    val isSynced: Boolean = false
)

data class PurchaseItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Double = 1.0,
    val unit: String = "قطعة",
    val unitCost: Double = 0.0,
    val total: Double = 0.0
)

enum class PurchaseStatus { COMPLETED, PENDING, PARTIAL }

// ============================
// EXPENSE MODEL
// ============================
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val description: String = "",
    val createdById: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class ExpenseCategory(val nameAr: String) {
    RENT("إيجار"),
    UTILITIES("مرافق وكهرباء"),
    SALARY("رواتب"),
    MAINTENANCE("صيانة"),
    MARKETING("تسويق وإعلان"),
    TRANSPORT("نقل وشحن"),
    PACKAGING("تغليف"),
    OTHER("أخرى")
}

// ============================
// NOTIFICATION MODEL
// ============================
data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.SALE,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType { SALE, LOW_STOCK, EXPIRY, PURCHASE, EXPENSE, SYSTEM }

// ============================
// DASHBOARD STATS
// ============================
data class DashboardStats(
    val todaySales: Double = 0.0,
    val todayTransactions: Int = 0,
    val weekSales: Double = 0.0,
    val monthSales: Double = 0.0,
    val totalProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val expiringProducts: Int = 0,
    val totalCustomers: Int = 0,
    val netProfit: Double = 0.0
)
