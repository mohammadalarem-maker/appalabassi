package com.supermarket.app.data.local

import androidx.room.*
import com.google.gson.Gson
import com.supermarket.app.data.models.*
import kotlinx.coroutines.flow.Flow

// ============================
// TYPE CONVERTERS
// ============================
class Converters {
    private val gson = Gson()
    @TypeConverter fun saleItemsToJson(v: List<SaleItem>): String = gson.toJson(v)
    @TypeConverter fun jsonToSaleItems(v: String): List<SaleItem> = gson.fromJson(v, Array<SaleItem>::class.java).toList()
    @TypeConverter fun purchaseItemsToJson(v: List<PurchaseItem>): String = gson.toJson(v)
    @TypeConverter fun jsonToPurchaseItems(v: String): List<PurchaseItem> = gson.fromJson(v, Array<PurchaseItem>::class.java).toList()
    @TypeConverter fun catToStr(v: ProductCategory): String = v.name
    @TypeConverter fun strToCat(v: String): ProductCategory = ProductCategory.valueOf(v)
    @TypeConverter fun saleStatusToStr(v: SaleStatus): String = v.name
    @TypeConverter fun strToSaleStatus(v: String): SaleStatus = SaleStatus.valueOf(v)
    @TypeConverter fun paymentToStr(v: PaymentMethod): String = v.name
    @TypeConverter fun strToPayment(v: String): PaymentMethod = PaymentMethod.valueOf(v)
    @TypeConverter fun discountTypeToStr(v: DiscountType): String = v.name
    @TypeConverter fun strToDiscountType(v: String): DiscountType = DiscountType.valueOf(v)
    @TypeConverter fun purchaseStatusToStr(v: PurchaseStatus): String = v.name
    @TypeConverter fun strToPurchaseStatus(v: String): PurchaseStatus = PurchaseStatus.valueOf(v)
    @TypeConverter fun expenseCatToStr(v: ExpenseCategory): String = v.name
    @TypeConverter fun strToExpenseCat(v: String): ExpenseCategory = ExpenseCategory.valueOf(v)
    @TypeConverter fun membershipToStr(v: MembershipLevel): String = v.name
    @TypeConverter fun strToMembership(v: String): MembershipLevel = MembershipLevel.valueOf(v)
}

// ============================
// DAOs
// ============================
@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?
    @Query("SELECT * FROM products WHERE quantity <= minQuantity AND isActive = 1")
    fun getLowStockProducts(): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE name LIKE '%' || :q || '%' OR barcode LIKE '%' || :q || '%'")
    fun searchProducts(q: String): Flow<List<Product>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    @Update
    suspend fun updateProduct(product: Product)
    @Query("UPDATE products SET quantity = quantity - :qty WHERE id = :id")
    suspend fun decreaseStock(id: String, qty: Int)
    @Query("UPDATE products SET quantity = quantity + :qty WHERE id = :id")
    suspend fun increaseStock(id: String, qty: Int)
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)
    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun getTotalProductsCount(): Int
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<Sale>>
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): Sale?
    @Query("SELECT SUM(total) FROM sales WHERE createdAt >= :start AND createdAt <= :end AND status = 'COMPLETED'")
    suspend fun getTotalSalesAmount(start: Long, end: Long): Double?
    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :start AND createdAt <= :end AND status = 'COMPLETED'")
    suspend fun getSalesCount(start: Long, end: Long): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)
    @Update
    suspend fun updateSale(sale: Sale)
    @Query("SELECT * FROM sales WHERE isSynced = 0")
    suspend fun getUnsyncedSales(): List<Sale>
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :q || '%' OR phone LIKE '%' || :q || '%'")
    fun searchCustomers(q: String): Flow<List<Customer>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)
    @Update
    suspend fun updateCustomer(customer: Customer)
    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: String)
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY createdAt DESC")
    fun getAllPurchases(): Flow<List<Purchase>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)
    @Update
    suspend fun updatePurchase(purchase: Purchase)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<Expense>>
    @Query("SELECT SUM(amount) FROM expenses WHERE createdAt >= :start AND createdAt <= :end")
    suspend fun getTotalExpenses(start: Long, end: Long): Double?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    @Update
    suspend fun updateExpense(expense: Expense)
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)
}

// ============================
// DATABASE
// ============================
@Database(
    entities = [Product::class, Sale::class, Customer::class, Purchase::class, Expense::class],
    version  = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SuperMarketDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun expenseDao(): ExpenseDao
    companion object { const val DATABASE_NAME = "supermarket_db" }
}
