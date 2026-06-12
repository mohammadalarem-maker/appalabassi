package com.supermarket.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.models.UserRole
import com.supermarket.app.data.remote.FirebaseRepository
import com.supermarket.app.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: FirebaseRepository,
    private val prefs: PrefsManager
) : ViewModel() {

    private val _currentUser = MutableStateFlow<com.supermarket.app.data.models.User?>(null)
    val currentUser: StateFlow<com.supermarket.app.data.models.User?> = _currentUser

    fun login(
        username: String,
        password: String,
        onResult: (success: Boolean, isAdmin: Boolean, error: String?) -> Unit
    ) {
        if (username.isBlank() || password.isBlank()) {
            onResult(false, false, "يرجى إدخال اسم المستخدم وكلمة المرور")
            return
        }
        viewModelScope.launch {
            try {
                val cleanInput = username.trim()
                val cleanPassword = password.trim()

                // ① فحص الأدمن المحلي أولاً
                val local = repo.loginAdmin(cleanInput, cleanPassword)
                if (local.isSuccess) {
                    val user = local.getOrNull()!!
                    _currentUser.value = user
                    prefs.saveUser(user)
                    onResult(true, user.role == UserRole.ADMIN, null)
                    return@launch
                }

                // ② إذا كان إيميل ادخل مباشرة
                if (cleanInput.contains("@")) {
                    val fb = repo.loginWithEmailPassword(cleanInput.lowercase(), cleanPassword)
                    if (fb.isSuccess) {
                        val user = fb.getOrNull()!!
                        _currentUser.value = user
                        prefs.saveUser(user)
                        onResult(true, user.role == UserRole.ADMIN, null)
                        return@launch
                    }
                }

                // ③ البحث بالـ username في Firestore والدخول بإيميله الحقيقي
                val byUsername = repo.loginWithUsername(cleanInput, cleanPassword)
                if (byUsername.isSuccess) {
                    val user = byUsername.getOrNull()!!
                    _currentUser.value = user
                    prefs.saveUser(user)
                    onResult(true, user.role == UserRole.ADMIN, null)
                } else {
                    onResult(false, false, "بيانات الدخول غير صحيحة، تأكد من الاسم وكلمة المرور")
                }

            } catch (e: Exception) {
                onResult(false, false, "حدث خطأ في الاتصال: ${e.message}")
            }
        }
    }

    fun isLoggedIn(): Boolean = prefs.isLoggedIn()
}
