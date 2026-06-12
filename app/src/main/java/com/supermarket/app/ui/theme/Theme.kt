package com.supermarket.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// متغير حالة عام للتحكم بالوضع الليلي من أي مكان في التطبيق
var isAppDarkMode by mutableStateOf(true)

// ============================
// SUPERMARKET COLOR PALETTE
// تتغير الألوان هنا تلقائياً بناءً على وضع الثيم
// ============================
object SMColors {
    // الخلفيات
    val BgDeep        get() = if (isAppDarkMode) Color(0xFF0A1628) else Color(0xFFF1F5F9)   // نيلي عميق / رمادي فاتح مريح
    val BgSurface     get() = if (isAppDarkMode) Color(0xFF0F1F3D) else Color(0xFFFFFFFF)   // أزرق نيلي / أبيض ناصع
    val BgCard        get() = if (isAppDarkMode) Color(0xFF152847) else Color(0xFFFFFFFF)   // كارد داكن / كارد أبيض
    val BgCardAlt     get() = if (isAppDarkMode) Color(0xFF1A3258) else Color(0xFFF8FAFC)   
    val BgCardBorder  get() = if (isAppDarkMode) Color(0xFF1E3A60) else Color(0xFFE2E8F0)   

    // ألوان الهوية البصرية (ثابتة للحفاظ على العلامة التجارية)
    val Primary       = Color(0xFF00D26A)   // أخضر حيوي
    val PrimaryDark   = Color(0xFF00A855)   // أخضر داكن
    val PrimaryGlow   = Color(0xFF00FF7F)   

    // الألوان التنبيهية والجمالية
    val AccentOrange  = Color(0xFFFF6B2B)   // برتقالي للعروض
    val AccentYellow  = Color(0xFFFFD60A)   // أصفر للتحذيرات
    val AccentPurple  = Color(0xFF9B59B6)   // بنفسجي
    val AccentCyan    = Color(0xFF00E5FF)   // سماوي

    // النصوص
    val TextPrimary   get() = if (isAppDarkMode) Color(0xFFEEF2FF) else Color(0xFF0F172A)   // نص فاتح / نص داكن أسود
    val TextSecondary get() = if (isAppDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)   
    val TextMuted     get() = if (isAppDarkMode) Color(0xFF4A6080) else Color(0xFF94A3B8)   

    // الحالات
    val Success       = Color(0xFF00D26A)
    val Warning       = Color(0xFFFFD60A)
    val Error         = Color(0xFFFF4757)
    val Info          = Color(0xFF00E5FF)

    // ألوان فئات المنتجات
    val CatFood       = Color(0xFF4CAF50)
    val CatVeg        = Color(0xFF8BC34A)
    val CatDairy      = Color(0xFF03A9F4)
    val CatMeat       = Color(0xFFE91E63)
    val CatBakery     = Color(0xFFFF9800)
    val CatBev        = Color(0xFF9C27B0)
    val CatClean      = Color(0xFF00BCD4)
    val CatPersonal   = Color(0xFFFF5722)
    val CatFrozen     = Color(0xFF607D8B)
    val CatSnacks     = Color(0xFFFFC107)

    // القائمة الجانبية
    val SidebarBg     get() = if (isAppDarkMode) Color(0xFF0D1E35) else Color(0xFF1E293B)
    val SidebarActive get() = if (isAppDarkMode) Color(0xFF152847) else Color(0xFFF1F5F9)

    // شاشة تسجيل الدخول
    val LoginBg1      get() = if (isAppDarkMode) Color(0xFF060D1A) else Color(0xFFF8FAFC)
    val LoginBg2      get() = if (isAppDarkMode) Color(0xFF0A1628) else Color(0xFFF1F5F9)
    val LoginCard     get() = if (isAppDarkMode) Color(0xCC0F1F3D) else Color(0xCCE2E8F0)
}

private val DarkColorScheme = darkColorScheme(
    primary            = SMColors.Primary,
    onPrimary          = Color.Black,
    primaryContainer   = SMColors.PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary          = SMColors.AccentCyan,
    background         = Color(0xFF0A1628),
    surface            = Color(0xFF0F1F3D)
)

private val LightColorScheme = lightColorScheme(
    primary            = SMColors.PrimaryDark,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFE6F9F0),
    onPrimaryContainer = SMColors.PrimaryDark,
    secondary          = SMColors.AccentCyan,
    background         = Color(0xFFF1F5F9),
    surface            = Color(0xFFFFFFFF)
)

val SMTypography = Typography(
    displayLarge  = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Black,  letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    headlineMedium= TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleLarge    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleMedium   = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyLarge     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall     = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium   = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall    = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
)

@Composable
fun SuperMarketTheme(content: @Composable () -> Unit) {
    val colors = if (isAppDarkMode) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography  = SMTypography,
        content     = content
    )
}
