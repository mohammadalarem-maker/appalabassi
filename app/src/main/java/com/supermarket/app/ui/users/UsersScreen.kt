package com.supermarket.app.ui.users

import com.supermarket.app.ui.smOutlinedColors
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.models.User
import com.supermarket.app.data.models.UserRole
import com.supermarket.app.data.remote.FirebaseRepository
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    val currentUser = MutableStateFlow<User?>(null)

    init {
        currentUser.value = prefsManager.getUser()
        viewModelScope.launch {
            firebaseRepository.getUsers().collect { _users.value = it }
        }
    }

    fun addUser(username: String, email: String, password: String, role: UserRole, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            val cleanUsername = username.trim()
            val result = firebaseRepository.registerUser(
                User(username = cleanUsername, email = cleanEmail, role = role),
                password
            )
            if (result.isSuccess) {
                onResult(true, "تمت إضافة المستخدم بنجاح")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "فشل غير معروف"
                onResult(false, errorMsg)
            }
        }
    }

    fun deleteUser(uid: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = firebaseRepository.deleteUser(uid)
            onResult(result.isSuccess)
        }
    }

    fun deactivate(uid: String) {
        viewModelScope.launch { firebaseRepository.deactivateUser(uid) }
    }

    fun changePassword(newPass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val r = firebaseRepository.changePassword(newPass)
            onResult(r.isSuccess)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(viewModel: UsersViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState()
    val me by viewModel.currentUser.collectAsState()
    val isAdmin = me?.role == UserRole.ADMIN
    var showAdd by remember { mutableStateOf(false) }
    var showChangePw by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    Column(Modifier.fillMaxSize().background(SMColors.BgDeep).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        me?.let { user ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SMColors.Primary.copy(0.12f)), border = BorderStroke(1.dp, SMColors.Primary.copy(0.35f))) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(SMColors.Primary.copy(0.2f), CircleShape).border(2.dp, SMColors.Primary.copy(0.5f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(user.username.firstOrNull()?.uppercase() ?: "م", color = SMColors.Primary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(user.username, color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(user.role.nameAr, color = SMColors.Primary, fontSize = 12.sp)
                        Text("الحساب الحالي", color = SMColors.TextMuted, fontSize = 11.sp)
                    }
                    IconButton({ showChangePw = true }) { Icon(Icons.Filled.Lock, null, tint = SMColors.AccentCyan) }
                    if (isAdmin) IconButton({ showAdd = true }) { Icon(Icons.Filled.PersonAdd, null, tint = SMColors.Primary) }
                }
            }
        }

        Text("المستخدمون (${users.size})", color = SMColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users, key = { it.uid }) { user ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (user.isActive) SMColors.BgCard else SMColors.BgSurface), border = BorderStroke(1.dp, SMColors.BgCardBorder)) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        val roleColor = when(user.role) { UserRole.ADMIN -> SMColors.Primary; UserRole.MANAGER -> SMColors.AccentCyan; UserRole.CASHIER -> SMColors.AccentYellow; else -> SMColors.TextMuted }
                        Box(Modifier.size(42.dp).background(roleColor.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(user.username.firstOrNull()?.uppercase() ?: "م", color = roleColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(user.username, color = if (user.isActive) SMColors.TextPrimary else SMColors.TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(user.email, color = SMColors.TextMuted, fontSize = 11.sp)
                            Box(Modifier.background(roleColor.copy(0.12f), RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(user.role.nameAr, color = roleColor, fontSize = 10.sp)
                            }
                        }
                        if (isAdmin && user.role != UserRole.ADMIN) {
                            IconButton({ viewModel.deactivate(user.uid) }, Modifier.size(32.dp)) {
                                Icon(if (user.isActive) Icons.Filled.Block else Icons.Filled.CheckCircle, null, tint = if (user.isActive) SMColors.Warning else SMColors.Primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton({ userToDelete = user }, Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Delete, null, tint = SMColors.Error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            containerColor = SMColors.BgCard,
            title = { Text("حذف المستخدم", color = SMColors.Error, fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف \"${user.username}\"؟", color = SMColors.TextPrimary) },
            confirmButton = {
                Button(onClick = { viewModel.deleteUser(user.uid) { userToDelete = null } },
                    colors = ButtonDefaults.buttonColors(containerColor = SMColors.Error)
                ) { Text("حذف", color = Color.White) }
            },
            dismissButton = { TextButton({ userToDelete = null }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }

    if (showAdd) {
        var uname by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(UserRole.CASHIER) }
        var exp by remember { mutableStateOf(false) }
        var addError by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showAdd = false },
            containerColor = SMColors.BgCard,
            title = { Text("إضافة مستخدم جديد", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (addError.isNotEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = SMColors.Error.copy(0.1f)),
                            border = BorderStroke(1.dp, SMColors.Error.copy(0.3f))) {
                            Text(addError, color = SMColors.Error, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                        }
                    }
                    OutlinedTextField(
                        value = uname,
                        onValueChange = { uname = it.trim() },
                        label = { Text("اسم المستخدم (بدون مسافات أو رموز)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = smOutlinedColors(),
                        enabled = !isSubmitting,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = smOutlinedColors(),
                        enabled = !isSubmitting,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("كلمة المرور (6 أحرف على الأقل)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = smOutlinedColors(),
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isSubmitting,
                        singleLine = true
                    )
                    ExposedDropdownMenuBox(exp, { exp = it }) {
                        OutlinedTextField(role.nameAr, {}, readOnly = true,
                            label = { Text("الصلاحية") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exp) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = smOutlinedColors(),
                            enabled = !isSubmitting)
                        ExposedDropdownMenu(exp, { exp = false }, modifier = Modifier.background(SMColors.BgCard)) {
                            UserRole.values().filter { it != UserRole.ADMIN }.forEach { r ->
                                DropdownMenuItem(text = { Text(r.nameAr, color = SMColors.TextPrimary) }, onClick = { role = r; exp = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanEmail = email.trim().lowercase()
                        val cleanUsername = uname.trim()
                        if (cleanUsername.isBlank() || cleanEmail.isBlank() || pass.isBlank()) {
                            addError = "الرجاء تعبئة جميع الحقول"
                            return@Button
                        }
                        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                            addError = "البريد الإلكتروني غير صحيح"
                            return@Button
                        }
                        if (pass.length < 6) {
                            addError = "كلمة المرور يجب أن تكون 6 أحرف على الأقل"
                            return@Button
                        }
                        isSubmitting = true
                        addError = ""
                        viewModel.addUser(cleanUsername, cleanEmail, pass, role) { success, message ->
                            isSubmitting = false
                            if (success) showAdd = false else addError = message
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("إضافة", color = Color.Black)
                }
            },
            dismissButton = { TextButton({ if (!isSubmitting) showAdd = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }

    if (showChangePw) {
        var newPass by remember { mutableStateOf("") }
        var confPass by remember { mutableStateOf("") }
        var err by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePw = false },
            containerColor = SMColors.BgCard,
            title = { Text("تغيير كلمة المرور", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (err.isNotEmpty()) Text(err, color = SMColors.Error, fontSize = 12.sp)
                    OutlinedTextField(newPass, { newPass = it }, label = { Text("كلمة المرور الجديدة") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors(), visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(confPass, { confPass = it }, label = { Text("تأكيد كلمة المرور") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors(), visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                Button({
                    if (newPass != confPass) { err = "كلمتا المرور غير متطابقتان"; return@Button }
                    if (newPass.length < 6) { err = "6 أحرف على الأقل"; return@Button }
                    viewModel.changePassword(newPass) { showChangePw = false }
                }, colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)) { Text("تغيير", color = Color.Black) }
            },
            dismissButton = { TextButton({ showChangePw = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }
}
