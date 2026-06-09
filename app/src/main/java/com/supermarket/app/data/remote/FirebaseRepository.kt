package com.supermarket.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.supermarket.app.data.models.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private fun usersCol()   = db.collection("users")
    private fun productsCol()= db.collection("products")
    private fun salesCol()   = db.collection("sales")
    private fun custsCol()   = db.collection("customers")
    private fun expCol()     = db.collection("expenses")
    private fun notifsCol()  = db.collection("notifications")

    // ============================
    // AUTH
    // ============================
    suspend fun loginAdmin(username: String, password: String): Result<User> {
        return if (username == "Mohali" && password == "1234567")
            Result.success(User("admin_local","Mohali","Mohammedalsarem6@gmail.com", UserRole.ADMIN, true))
        else Result.failure(Exception("Invalid"))
    }

    suspend fun loginWithEmailPassword(email: String, password: String): Result<User> {
        return try {
            val r   = auth.signInWithEmailAndPassword(email, password).await()
            val uid = r.user?.uid ?: throw Exception("Failed")
            val doc = usersCol().document(uid).get().await()
            val u   = doc.toObject(User::class.java) ?: throw Exception("No user data")
            Result.success(u)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun registerUser(user: User, password: String): Result<User> {
        return try {
            val r   = auth.createUserWithEmailAndPassword(user.email, password).await()
            val uid = r.user?.uid ?: throw Exception("Failed")
            val newUser = user.copy(uid = uid)
            usersCol().document(uid).set(newUser).await()
            Result.success(newUser)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun changePassword(newPass: String): Result<Unit> {
        return try {
            auth.currentUser?.updatePassword(newPass)?.await() ?: throw Exception("Not logged in")
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun logout() = auth.signOut()

    // ============================
    // USERS
    // ============================
    fun getUsers(): Flow<List<User>> = callbackFlow {
        val l = usersCol().addSnapshotListener { s, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(s?.toObjects(User::class.java) ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun deactivateUser(uid: String): Result<Unit> {
        return try { usersCol().document(uid).update("isActive", false).await(); Result.success(Unit) }
        catch (e: Exception) { Result.failure(e) }
    }

    // ============================
    // PRODUCTS
    // ============================
    suspend fun addProduct(product: Product): Result<String> {
        return try { productsCol().document(product.id).set(product).await(); Result.success(product.id) }
        catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteProduct(id: String): Result<Unit> {
        return try { productsCol().document(id).update("isActive", false).await(); Result.success(Unit) }
        catch (e: Exception) { Result.failure(e) }
    }

    // ============================
    // SALES
    // ============================
    suspend fun addSale(sale: Sale): Result<String> {
        return try {
            salesCol().document(sale.id).set(sale).await()
            sendSaleNotification(sale)
            sale.items.forEach { item ->
                productsCol().document(item.productId)
                    .update("quantity", com.google.firebase.firestore.FieldValue.increment(-item.quantity.toLong()))
                    .await()
            }
            Result.success(sale.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ============================
    // CUSTOMERS
    // ============================
    suspend fun addCustomer(customer: Customer): Result<String> {
        return try { custsCol().document(customer.id).set(customer).await(); Result.success(customer.id) }
        catch (e: Exception) { Result.failure(e) }
    }

    // ============================
    // EXPENSES
    // ============================
    suspend fun addExpense(expense: Expense): Result<String> {
        return try { expCol().document(expense.id).set(expense).await(); Result.success(expense.id) }
        catch (e: Exception) { Result.failure(e) }
    }

    // ============================
    // NOTIFICATIONS
    // ============================
    private suspend fun sendSaleNotification(sale: Sale) {
        try {
            val n = AppNotification(
                id    = notifsCol().document().id,
                title = "🛒 عملية بيع جديدة",
                body  = "تم بيع ${sale.items.size} منتج بمبلغ ${"%.2f".format(sale.total)} ر",
                type  = NotificationType.SALE,
                data  = mapOf("saleId" to sale.id, "total" to sale.total.toString(),
                    "cashier" to sale.cashierName, "itemsCount" to sale.items.size.toString())
            )
            notifsCol().document(n.id).set(n).await()
        } catch (_: Exception) {}
    }

    suspend fun sendLowStockNotification(product: Product) {
        try {
            val n = AppNotification(
                id    = notifsCol().document().id,
                title = "⚠️ مخزون منخفض",
                body  = "\"${product.name}\" وصل إلى ${product.quantity} ${product.unit} فقط",
                type  = NotificationType.LOW_STOCK,
                data  = mapOf("productId" to product.id, "productName" to product.name,
                    "quantity" to product.quantity.toString())
            )
            notifsCol().document(n.id).set(n).await()
        } catch (_: Exception) {}
    }

    fun getNotifications(): Flow<List<AppNotification>> = callbackFlow {
        val l = notifsCol().orderBy("createdAt", Query.Direction.DESCENDING).limit(100)
            .addSnapshotListener { s, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(s?.toObjects(AppNotification::class.java) ?: emptyList())
            }
        awaitClose { l.remove() }
    }
}
