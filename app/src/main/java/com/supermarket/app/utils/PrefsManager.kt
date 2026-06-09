package com.supermarket.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.supermarket.app.data.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sm_prefs", Context.MODE_PRIVATE)
    private val gson = GsonBuilder().serializeNulls().create()

    fun saveUser(user: User) {
        prefs.edit().putString("current_user", gson.toJson(user)).putBoolean("is_logged_in", true).apply()
    }
    fun getUser(): User? {
        val json = prefs.getString("current_user", null) ?: return null
        return try { gson.fromJson(json, User::class.java) } catch (_: Exception) { null }
    }
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)
    fun savePassword(p: String) { prefs.edit().putString("local_pass", p).apply() }
    fun getLocalPassword(): String = prefs.getString("local_pass", "1234567") ?: "1234567"
    fun logout() { prefs.edit().remove("current_user").putBoolean("is_logged_in", false).apply() }
    fun setLowStockThreshold(v: Int) { prefs.edit().putInt("low_stock", v).apply() }
    fun getLowStockThreshold(): Int = prefs.getInt("low_stock", 10)
    fun setNotificationsEnabled(v: Boolean) { prefs.edit().putBoolean("notif", v).apply() }
    fun isNotificationsEnabled(): Boolean = prefs.getBoolean("notif", true)
    fun setStoreName(v: String) { prefs.edit().putString("store_name", v).apply() }
    fun getStoreName(): String = prefs.getString("store_name", "سوبرماركت") ?: "سوبرماركت"
    fun setTaxRate(v: Double) { prefs.edit().putFloat("tax", v.toFloat()).apply() }
    fun getTaxRate(): Double = prefs.getFloat("tax", 0f).toDouble()
    fun setCurrency(v: String) { prefs.edit().putString("currency", v).apply() }
    fun getCurrency(): String = prefs.getString("currency", "ريال") ?: "ريال"
}
