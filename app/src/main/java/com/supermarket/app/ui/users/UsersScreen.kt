package com.supermarket.app.ui.users

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
import com.supermarket.app.data.models.User
import com.supermarket.app.data.models.UserRole
import com.supermarket.app.data.remote.FirebaseRepository
import com.supermarket.app.ui.inventory.smOutlinedColors
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ======= VIEWMODEL =======
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val prefsManager: PrefsManager
) : androidx.lifecycle.ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    val currentUser = MutableStateFlow<User?>(null)

    init {
        currentUser.value = prefsManager.getUser()
        androidx.lifecycle.viewModelScope.launch {
            firebaseRepository.getUsers().collect { _users.value = it }
        }
    }

    fun addUser(username: String, email: String, password: String, role: UserRole) {
        androidx.lifecycle.viewModelScope.launch {
            firebaseRepository.registerUser(User(username = username, email = email, role = role), password)
        }
    }
    fun deactivate(uid: String) {
        androidx.lifecycle.viewModelScope.launch { firebaseRepository.deactivateUser(uid) }
    }
    fun changePassword(newPass: String, onResult: (Boolean) -> Unit) {
        androidx.lifecycle.viewModelScope.launch {
            val r = firebaseRepository.changePassword(newPass)
            onResult(r.isSuccess)
        }
    }
}

// ======= SCREEN =======
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(viewModel: UsersViewModel = hiltViewModel()) {
    val viewModelScope = androidx.compose.runtime.rememberCoroutineScope()

    
    val users       by viewModel.users.collectAsState()
    val me          by viewModel.currentUser.collectAsState()
    val isAdmin     = me?.role == UserRole.ADMIN
    var showAdd     by remember { mutableStateOf(false) }
    var showChangePw by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(SMColors.BgDeep).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // My card
        me?.let { user ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SMColors.Primary.copy(0.12f)),
                border = BorderStroke(1.dp, SMColors.Primary.copy(0.35f))
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(SMColors.Primary.copy(0.2f), CircleShape)
                        .border(2.dp, SMColors.Primary.copy(0.5f), CircleShape), contentAlignment = Alignment.Center) {
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
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (user.isActive) SMColors.BgCard else SMColors.BgSurface),
                    border = BorderStroke(1.dp, SMColors.BgCardBorder)
                ) {
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
                                Icon(if (user.isActive) Icons.Filled.Block else Icons.Filled.CheckCircle, null,
                                    tint = if (user.isActive) SMColors.Error else SMColors.Primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add user dialog
    if (showAdd) {
        var uname by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var pass  by remember { mutableStateOf("") }
        var role  by remember { mutableStateOf(UserRole.CASHIER) }
        var exp   by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { showAdd = false }, containerColor = SMColors.BgCard,
            title = { Text("إضافة مستخدم", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(uname, { uname = it }, label = { Text("اسم المستخدم") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                    OutlinedTextField(email, { email = it }, label = { Text("البريد الإلكتروني") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                    OutlinedTextField(pass, { pass = it }, label = { Text("كلمة المرور") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors(), visualTransformation = PasswordVisualTransformation())
                    ExposedDropdownMenuBox(exp, { exp = it }) {
                        OutlinedTextField(role.nameAr, {}, readOnly = true, label = { Text("الصلاحية") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exp) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp), colors = smOutlinedColors())
                        ExposedDropdownMenu(exp, { exp = false }, modifier = Modifier.background(SMColors.BgCard)) {
                            UserRole.values().filter { it != UserRole.ADMIN }.forEach { r ->
                                DropdownMenuItem(text = { Text(r.nameAr, color = SMColors.TextPrimary) }, onClick = { role = r; exp = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button({ viewModel.addUser(uname, email, pass, role); showAdd = false }, colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)) { Text("إضافة", color = Color.Black) }
            },
            dismissButton = { TextButton({ showAdd = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }

    // Change password dialog
    if (showChangePw) {
        var newPass  by remember { mutableStateOf("") }
        var confPass by remember { mutableStateOf("") }
        var err      by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showChangePw = false }, containerColor = SMColors.BgCard,
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
                    if (newPass.length < 6)  { err = "6 أحرف على الأقل"; return@Button }
                    viewModel.changePassword(newPass) { showChangePw = false }
                }, colors = ButtonDefaults.buttonColors(containerColor = SMColors.Primary)) { Text("تغيير", color = Color.Black) }
            },
            dismissButton = { TextButton({ showChangePw = false }) { Text("إلغاء", color = SMColors.TextSecondary) } }
        )
    }
}
