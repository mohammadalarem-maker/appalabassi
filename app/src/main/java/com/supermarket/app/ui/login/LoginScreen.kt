package com.supermarket.app.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.supermarket.app.ui.theme.SMColors
import kotlinx.coroutines.delay
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    viewModel: LoginViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animAngle by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "angle"
    )
    val pulse by infiniteTransition.animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )
    val cartBounce by infiniteTransition.animateFloat(
        0f, -12f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bounce"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); visible = true }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(SMColors.LoginBg1, SMColors.LoginBg2, Color(0xFF0A1F3A)))
        )
    ) {
        // Animated mesh background
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2; val cy = size.height / 2
            // Green glow top-left
            drawCircle(SMColors.Primary.copy(alpha = 0.06f), 350f, Offset(cx * 0.3f, cy * 0.4f))
            // Blue glow bottom-right
            drawCircle(SMColors.AccentCyan.copy(alpha = 0.04f), 400f, Offset(cx * 1.7f, cy * 1.6f))
            // Rotating ring
            val r = 220f
            drawCircle(
                SMColors.Primary.copy(alpha = 0.04f),
                r, Offset(
                    cx + r * cos(Math.toRadians(animAngle.toDouble())).toFloat(),
                    cy + r * sin(Math.toRadians(animAngle.toDouble())).toFloat()
                )
            )
        }

        // Grid lines overlay
        Canvas(Modifier.fillMaxSize()) {
            val step = 60f
            var x = 0f
            while (x < size.width) {
                drawLine(SMColors.Primary.copy(alpha = 0.03f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(SMColors.Primary.copy(alpha = 0.03f), Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(70.dp))

            // Logo area
            enter = androidx.compose.animation.fadeIn(),
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Animated cart icon in glowing box
                    Box(contentAlignment = Alignment.Center) {
                        // Outer glow ring
                        Box(
                            Modifier.size(110.dp).scale(pulse)
                                .background(SMColors.Primary.copy(0.08f), CircleShape)
                        )
                        // Icon box
                        Box(
                            Modifier
                                .size(90.dp)
                                .offset(y = cartBounce.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(SMColors.Primary, SMColors.PrimaryDark),
                                        start = Offset(0f, 0f), end = Offset(90f, 90f)
                                    ),
                                    RoundedCornerShape(26.dp)
                                )
                                .shadow(24.dp, RoundedCornerShape(26.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ShoppingCart, null,
                                tint = Color.Black, modifier = Modifier.size(46.dp))
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    Text("سوبرماركت", style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black, color = SMColors.TextPrimary,
                        letterSpacing = (-1).sp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(SMColors.Primary, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("نظام إدارة البيع المتكامل",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SMColors.TextSecondary)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(6.dp).background(SMColors.Primary, CircleShape))
                    }

                    Spacer(Modifier.height(6.dp))

                    // Version badge
                    Box(
                        Modifier.background(SMColors.Primary.copy(0.15f), RoundedCornerShape(20.dp))
                            .border(1.dp, SMColors.Primary.copy(0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text("الإصدار 1.0 • 2024", color = SMColors.Primary,
                            fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // Login card
            enter = androidx.compose.animation.fadeIn(),

                Card(
                    Modifier.fillMaxWidth().border(
                        1.dp,
                        Brush.linearGradient(listOf(
                            SMColors.Primary.copy(0.6f), SMColors.AccentCyan.copy(0.3f), Color.Transparent
                        )), RoundedCornerShape(28.dp)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SMColors.LoginCard),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(4.dp, 28.dp).background(SMColors.Primary, RoundedCornerShape(2.dp)))
                            Column {
                                Text("تسجيل الدخول", style = MaterialTheme.typography.headlineMedium,
                                    color = SMColors.TextPrimary, fontWeight = FontWeight.Bold)
                                Text("أدخل بياناتك للمتابعة", fontSize = 12.sp, color = SMColors.TextSecondary)
                            }
                        }

                        // Username
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it; errorMessage = "" },
                            label = { Text("اسم المستخدم") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, null,
                                    tint = if (username.isNotEmpty()) SMColors.Primary else SMColors.TextSecondary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = smFieldColors(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = "" },
                            label = { Text("كلمة المرور") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, null,
                                    tint = if (password.isNotEmpty()) SMColors.Primary else SMColors.TextSecondary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        null, tint = SMColors.TextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = smFieldColors(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                        )

                        // Error
                        AnimatedVisibility(errorMessage.isNotEmpty()) {
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(SMColors.Error.copy(0.1f), RoundedCornerShape(12.dp))
                                    .border(1.dp, SMColors.Error.copy(0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ErrorOutline, null, tint = SMColors.Error, modifier = Modifier.size(18.dp))
                                Text(errorMessage, color = SMColors.Error, fontSize = 13.sp)
                            }
                        }

                        // Login Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                isLoading = true; errorMessage = ""
                                viewModel.login(username, password) { success, isAdmin, err ->
                                    isLoading = false
                                    if (success) onLoginSuccess(isAdmin)
                                    else errorMessage = err ?: "خطأ في تسجيل الدخول"
                                }
                            },
                            Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            enabled = !isLoading
                        ) {
                            Box(
                                Modifier.fillMaxSize().background(
                                    if (!isLoading) Brush.horizontalGradient(listOf(SMColors.Primary, SMColors.PrimaryDark))
                                    else Brush.horizontalGradient(listOf(SMColors.TextMuted, SMColors.TextMuted)),
                                    RoundedCornerShape(18.dp)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                        Text("جاري الدخول...", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Login, null, tint = Color.Black)
                                        Text("دخول", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 17.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(800, 700))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("تطوير: كلود • بواسطة محمد الصارم",
                        fontSize = 11.sp, color = SMColors.TextMuted)
                }
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}

@Composable
fun smFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = SMColors.Primary,
    unfocusedBorderColor = SMColors.BgCardBorder,
    focusedLabelColor    = SMColors.Primary,
    unfocusedLabelColor  = SMColors.TextSecondary,
    cursorColor          = SMColors.Primary,
    focusedTextColor     = SMColors.TextPrimary,
    unfocusedTextColor   = SMColors.TextPrimary,
    focusedContainerColor   = SMColors.BgCard,
    unfocusedContainerColor = SMColors.BgSurface,
    disabledTextColor       = SMColors.TextPrimary.copy(0.5f),
    disabledBorderColor     = SMColors.BgCardBorder,
    disabledContainerColor  = SMColors.BgSurface
)
