package com.supermarket.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.supermarket.app.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        return db
    }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SuperMarketDatabase =
        Room.databaseBuilder(ctx, SuperMarketDatabase::class.java, SuperMarketDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration().build()

    @Provides fun provideProductDao(db: SuperMarketDatabase): ProductDao   = db.productDao()
    @Provides fun provideSaleDao(db: SuperMarketDatabase): SaleDao         = db.saleDao()
    @Provides fun provideCustomerDao(db: SuperMarketDatabase): CustomerDao = db.customerDao()
    @Provides fun providePurchaseDao(db: SuperMarketDatabase): PurchaseDao = db.purchaseDao()
    @Provides fun provideExpenseDao(db: SuperMarketDatabase): ExpenseDao   = db.expenseDao()
}
