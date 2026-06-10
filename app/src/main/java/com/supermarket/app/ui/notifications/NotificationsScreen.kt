package com.supermarket.app.ui.notifications
import com.supermarket.app.ui.smOutlinedColors

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarket.app.data.models.AppNotification
import com.supermarket.app.data.models.NotificationType
import com.supermarket.app.data.remote.FirebaseRepository
import com.supermarket.app.ui.theme.SMColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repo: FirebaseRepository
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    init {
        viewModelScope.launch {
            repo.getNotifications().collect { _notifications.value = it }
        }
    }
    fun clearAll() { _notifications.value = emptyList() }
}

@Composable
fun NotificationsScreen(vm: NotificationsViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()


    val notifications by vm.notifications.collectAsState()
    val dateFormat    = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Column(
        Modifier.fillMaxSize().background(SMColors.BgDeep).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Clear all button
        if (notifications.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { vm.clearAll() }) {
                    Icon(Icons.Filled.DeleteSweep, null, tint = SMColors.Error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("مسح الكل", color = SMColors.Error, fontSize = 13.sp)
                }
            }
        }

        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("لا توجد إشعارات", color = SMColors.TextMuted, fontSize = 16.sp)
                    Text("ستظهر هنا إشعارات المبيعات والمخزون", color = SMColors.TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotifCard(notif, dateFormat)
                }
            }
        }
    }
}

@Composable
fun NotifCard(notif: AppNotification, dateFormat: SimpleDateFormat) {
    val (color, icon, emoji) = when (notif.type) {
        NotificationType.SALE      -> Triple(SMColors.Primary,       Icons.Filled.ShoppingCart,   "🛒")
        NotificationType.LOW_STOCK -> Triple(SMColors.Warning,       Icons.Filled.Warning,         "⚠️")
        NotificationType.EXPIRY    -> Triple(SMColors.AccentOrange,  Icons.Filled.DateRange,       "📅")
        NotificationType.PURCHASE  -> Triple(SMColors.AccentCyan,    Icons.Filled.LocalShipping,   "🚚")
        NotificationType.EXPENSE   -> Triple(SMColors.Error,         Icons.Filled.AccountBalance,  "💸")
        NotificationType.SYSTEM    -> Triple(SMColors.AccentPurple,  Icons.Filled.Info,            "ℹ️")
    }

    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notif.isRead) color.copy(0.08f) else SMColors.BgCard
        ),
        border = BorderStroke(1.dp, if (!notif.isRead) color.copy(0.3f) else SMColors.BgCardBorder)
    ) {                                                                                                 Row(
            Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier.size(42.dp).background(color.copy(0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 20.sp) }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(notif.title, color = SMColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(notif.body, color = SMColors.TextSecondary,
                    fontSize = 12.sp, lineHeight = 18.sp)
                // Extra data (amount, cashier, etc.)
                notif.data["total"]?.let {
                    Text("المبلغ:  ر", color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                notif.data["cashier"]?.let {
                    Text("الكاشير: ", color = SMColors.TextMuted, fontSize = 10.sp)
                }
                Text(dateFormat.format(Date(notif.createdAt)),
                    color = SMColors.TextMuted, fontSize = 10.sp)
            }

            if (!notif.isRead) {
                Box(Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
            }
        }
    }
}
