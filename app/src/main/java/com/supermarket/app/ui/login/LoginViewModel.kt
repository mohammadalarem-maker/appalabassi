package com.supermarket.app.ui.login
import com.supermarket.app.ui.smOutlinedColors

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
                // ① Local admin check - no Firebase needed
                val local = repo.loginAdmin(username.trim(), password)
                if (local.isSuccess) {
                    val user = local.getOrNull()!!
                    _currentUser.value = user
                    prefs.saveUser(user)
                    onResult(true, user.role == UserRole.ADMIN, null)
                    return@launch
                }
                // ② Firebase
                val fb = repo.loginWithEmailPassword("${username.trim()}@supermarket.app", password)
                if (fb.isSuccess) {
                    val user = fb.getOrNull()!!
                    _currentUser.value = user
                    prefs.saveUser(user)
                    onResult(true, user.role == UserRole.ADMIN, null)
                } else {
                    onResult(false, false, "اسم المستخدم أو كلمة المرور غير صحيحة")
                }
            } catch (e: Exception) {
                onResult(false, false, "حدث خطأ: ${e.message}")
            }
        }
    }

    fun isLoggedIn(): Boolean = prefs.isLoggedIn()
}
