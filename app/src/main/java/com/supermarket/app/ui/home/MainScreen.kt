package com.supermarket.app.ui.home
import com.supermarket.app.ui.smOutlinedColors

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarket.app.ui.theme.SMColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ============================
// NAVIGATION ITEMS
// ============================
data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val color: Color,
    val badge: String? = null,
    val section: String = ""
)

val navItems = listOf(
    NavItem("لوحة التحكم",    Icons.Filled.Dashboard,        "home",          SMColors.Primary,      section = "الرئيسية"),
    NavItem("نقطة البيع",     Icons.Filled.ShoppingCart,     "new_sale",      SMColors.AccentOrange, section = "الرئيسية"),

    NavItem("المخزون",        Icons.Filled.Inventory2,       "inventory",     SMColors.AccentCyan,   section = "إدارة المنتجات"),
    NavItem("إضافة منتج",    Icons.Filled.AddBox,           "add_product",   SMColors.Primary,      section = "إدارة المنتجات"),
    NavItem("المنتجات المنتهية",Icons.Filled.Warning,        "expiring",      SMColors.Warning,      section = "إدارة المنتجات"),

    NavItem("سجل المبيعات",   Icons.Filled.Receipt,          "sales",         SMColors.Primary,      section = "المبيعات"),
    NavItem("العملاء",        Icons.Filled.People,           "customers",     SMColors.AccentPurple, section = "المبيعات"),

    NavItem("المشتريات",      Icons.Filled.LocalShipping,    "purchases",     SMColors.AccentOrange, section = "المالية"),
    NavItem("المصروفات",      Icons.Filled.AccountBalance,   "expenses",      SMColors.Error,        section = "المالية"),
    NavItem("التقارير",       Icons.Filled.BarChart,         "reports",       SMColors.AccentCyan,   section = "المالية"),

    NavItem("المستخدمون",     Icons.Filled.ManageAccounts,   "users",         SMColors.AccentPurple, section = "الإدارة"),
    NavItem("الإشعارات",      Icons.Filled.Notifications,    "notifications", SMColors.Warning,      section = "الإدارة"),
    NavItem("الإعدادات",      Icons.Filled.Settings,         "settings",      SMColors.TextSecondary,section = "الإدارة"),
)

// ============================
// MAIN SCREEN WITH DRAWER
// ============================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val stats by viewModel.stats.collectAsState()
    // val lowStockCount by viewModel.lowStockCount.collectAsState()
    // val unreadNotifications by viewModel.unreadNotifications.collectAsState()

    // Update badges
    val itemsWithBadge = navItems.map { item ->
        when (item.route) {
            "notifications" -> item.copy(badge = if (unreadNotifications > 0) "$unreadNotifications" else null)
            "expiring"      -> item.copy(badge = if (stats.expiringProducts > 0) "${stats.expiringProducts}" else null)
            else -> item
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                items = itemsWithBadge,
                currentRoute = currentRoute,
                currentUser = currentUser,
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = SMColors.BgDeep,
            topBar = {
                TopAppBar(
                    title = {
                        // Current page title
                        val item = itemsWithBadge.find { it.route == currentRoute }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            item?.let {
                                Box(
                                    Modifier.size(8.dp, 24.dp)
                                        .background(it.color, RoundedCornerShape(4.dp))
                                )
                                Text(it.label, color = SMColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            } ?: Text("سوبرماركت", color = SMColors.TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, null, tint = SMColors.TextPrimary)
                        }
                    },
                    actions = {
                        // Notifications
                        BadgedBox(badge = {
                            if (unreadNotifications > 0)
                                Badge(containerColor = SMColors.Error) { Text("$unreadNotifications", fontSize = 9.sp) }
                        }) {
                            IconButton(onClick = { onNavigate("notifications") }) {
                                Icon(Icons.Outlined.Notifications, null, tint = SMColors.TextSecondary)
                            }
                        }
                        // Quick Sale
                        IconButton(onClick = { onNavigate("new_sale") }) {
                            Icon(Icons.Filled.ShoppingCart, null, tint = SMColors.Primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SMColors.BgSurface,
                        scrolledContainerColor = SMColors.BgSurface
                    )
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                content()
            }
        }
    }
}

// ============================
// DRAWER CONTENT
// ============================
@Composable
fun AppDrawer(
    items: List<NavItem>,
    currentRoute: String,
    currentUser: com.supermarket.app.data.models.User?,
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val sections = items.groupBy { it.section }

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = SMColors.SidebarBg,
        drawerContentColor = SMColors.TextPrimary
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Header
            DrawerHeader(currentUser)

            Spacer(Modifier.height(8.dp))

            // Navigation sections
            sections.forEach { (sectionName, sectionItems) ->
                if (sectionName.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        sectionName,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        color = SMColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
                sectionItems.forEach { item ->
                    DrawerNavItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onItemClick(item.route) }
                    )
                }
                if (sectionName != sections.keys.last()) {
                    Divider(
                        Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        color = SMColors.BgCardBorder
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))

            // Logout button
            DrawerLogoutButton(onLogout)

            // Footer
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("تطوير: كلود", fontSize = 10.sp, color = SMColors.TextMuted)
                Text("بواسطة محمد الصارم", fontSize = 10.sp, color = SMColors.Primary.copy(0.7f))
            }
        }
    }
}

@Composable
fun DrawerHeader(user: com.supermarket.app.data.models.User?) {
    // Background gradient header
    Box(
        Modifier.fillMaxWidth().background(
            Brush.verticalGradient(listOf(SMColors.BgCard, SMColors.SidebarBg))
        ).padding(20.dp)
    ) {
        // Decorative circle
        Box(
            Modifier.size(120.dp).offset(x = 180.dp, y = (-30).dp)
                .background(SMColors.Primary.copy(0.05f), CircleShape)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // App logo row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier.size(42.dp).background(
                        Brush.linearGradient(listOf(SMColors.Primary, SMColors.PrimaryDark)),
                        RoundedCornerShape(14.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ShoppingCart, null, tint = Color.Black, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text("سوبرماركت", color = SMColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text("نظام إدارة متكامل", color = SMColors.TextMuted, fontSize = 11.sp)
                }
            }

            Divider(color = SMColors.BgCardBorder)

            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar
                Box(
                    Modifier.size(42.dp).background(
                        SMColors.Primary.copy(0.15f), CircleShape
                    ).border(2.dp, SMColors.Primary.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user?.username?.firstOrNull()?.uppercase() ?: "م",
                        color = SMColors.Primary, fontWeight = FontWeight.Black, fontSize = 18.sp
                    )
                }
                Column {
                    Text(user?.username ?: "مستخدم", color = SMColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(7.dp).background(SMColors.Primary, CircleShape))
                        Text(user?.role?.nameAr ?: "مدير", color = SMColors.Primary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerNavItem(item: NavItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        if (isSelected) item.color.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200), label = "bg"
    )
    val textColor by animateColorAsState(
        if (isSelected) item.color else SMColors.TextSecondary,
        animationSpec = tween(200), label = "text"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .background(bgColor, RoundedCornerShape(14.dp))
            .then(if (isSelected) Modifier.border(1.dp, item.color.copy(0.25f), RoundedCornerShape(14.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon with colored background when selected
        Box(
            Modifier.size(34.dp).background(
                if (isSelected) item.color.copy(0.2f) else SMColors.BgCard,
                RoundedCornerShape(10.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = if (isSelected) item.color else SMColors.TextMuted,
                modifier = Modifier.size(19.dp))
        }

        Text(item.label, color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp, modifier = Modifier.weight(1f))

        // Badge
        item.badge?.let { badge ->
            Box(
                Modifier.background(item.color, RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(badge, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Active indicator
        if (isSelected) {
            Box(Modifier.size(4.dp, 20.dp).background(item.color, RoundedCornerShape(2.dp)))
        }
    }
}

@Composable
fun DrawerLogoutButton(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(SMColors.Error.copy(0.08f), RoundedCornerShape(14.dp))
            .border(1.dp, SMColors.Error.copy(0.2f), RoundedCornerShape(14.dp))
            .clickable(onClick = onLogout)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(34.dp).background(SMColors.Error.copy(0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Logout, null, tint = SMColors.Error, modifier = Modifier.size(19.dp))
        }
        Text("تسجيل الخروج", color = SMColors.Error,
            fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
    Spacer(Modifier.height(8.dp))
}
