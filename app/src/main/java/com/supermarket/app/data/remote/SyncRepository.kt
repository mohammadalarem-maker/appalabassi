package com.supermarket.app.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.supermarket.app.data.local.*
import com.supermarket.app.data.models.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val customerDao: CustomerDao,
    private val purchaseDao: PurchaseDao,
    private val expenseDao: ExpenseDao
) {
    private val TAG = "SyncRepo"
    suspend fun syncAll() { syncProducts(); syncSales(); syncCustomers(); syncPurchases(); syncExpenses() }

    private suspend fun syncProducts() {
        try { val l = db.collection("products").get().await().toObjects(Product::class.java); productDao.insertProducts(l) } catch(e:Exception){ Log.e(TAG,"error products",e) }
    }
    private suspend fun syncSales() {
        try { val l = db.collection("sales").get().await().toObjects(Sale::class.java); l.forEach{saleDao.insertSale(it)} } catch(e:Exception){ Log.e(TAG,"error sales",e) }
    }
    private suspend fun syncCustomers() {
        try { val l = db.collection("customers").get().await().toObjects(Customer::class.java); l.forEach{customerDao.insertCustomer(it)} } catch(e:Exception){ Log.e(TAG,"error customers",e) }
    }
    private suspend fun syncPurchases() {
        try { val l = db.collection("purchases").get().await().toObjects(Purchase::class.java); l.forEach{purchaseDao.insertPurchase(it)} } catch(e:Exception){ Log.e(TAG,"error purchases",e) }
    }
    private suspend fun syncExpenses() {
        try { val l = db.collection("expenses").get().await().toObjects(Expense::class.java); l.forEach{expenseDao.insertExpense(it)} } catch(e:Exception){ Log.e(TAG,"error expenses",e) }
    }
}
