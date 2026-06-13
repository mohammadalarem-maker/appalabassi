package com.supermarket.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.models.User
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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun login(
        usernameOrEmail: String,
        password: String,
        onResult: (success: Boolean, isAdmin: Boolean, error: String?) -> Unit
    ) {
        // تنظيف المدخلات من المسافات الزائدة
        val cleanInput = usernameOrEmail.trim()
        val cleanPassword = password.trim()

        if (cleanInput.isBlank() || cleanPassword.isBlank()) {
            onResult(false, false, "يرجى إدخال اسم المستخدم أو البريد الإلكتروني مع كلمة المرور")
            return
        }

        viewModelScope.launch {
            try {
                // ① الفحص المحلي السريع للأدمن (Mohali / 1234567)
                val local = repo.loginAdmin(cleanInput, cleanPassword)
                if (local.isSuccess) {
                    val user = local.getOrNull()!!
                    _currentUser.value = user
                    prefs.saveUser(user)
                    onResult(true, user.role == UserRole.ADMIN, null)
                    return@launch
                }

                // ② التوجيه الذكي: إذا كان المدخل يحتوي على @ فهو إيميل (للأدمن)، وإلا فهو اسم مستخدم (للموظفين)
                val fb = if (cleanInput.contains("@")) {
                    repo.loginWithEmailPassword(cleanInput, cleanPassword)
                } else {
                    repo.loginWithUsername(cleanInput, cleanPassword)
                }

                if (fb.isSuccess) {
                    val user = fb.getOrNull()!!
                    _currentUser.value = user
                    prefs.saveUser(user)
                    onResult(true, user.role == UserRole.ADMIN, null)
                } else {
                    onResult(false, false, "بيانات الدخول غير صحيحة، تأكد من الاسم وكلمة المرور")
                }
            } catch (e: Exception) {
                onResult(false, false, "حدث خطأ أثناء الاتصال: ${e.message}")
            }
        }
    }

    fun isLoggedIn(): Boolean = prefs.isLoggedIn()
}
