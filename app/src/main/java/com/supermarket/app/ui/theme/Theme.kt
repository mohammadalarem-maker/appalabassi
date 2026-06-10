package com.supermarket.app.ui.theme
import com.supermarket.app.ui.smOutlinedColors

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================
// SUPERMARKET COLOR PALETTE
// ألوان عصرية داكنة خضراء/زرقاء للسوبرماركت
// ============================
object SMColors {
    // Backgrounds
    val BgDeep        = Color(0xFF0A1628)   // أعمق خلفية - نيلي عميق
    val BgSurface     = Color(0xFF0F1F3D)   // سطح - نيلي
    val BgCard        = Color(0xFF152847)   // كارد - أزرق داكن
    val BgCardAlt     = Color(0xFF1A3258)   // كارد بديل
    val BgCardBorder  = Color(0xFF1E3A60)   // حدود الكارد

    // Primary - أخضر سوبرماركت
    val Primary       = Color(0xFF00D26A)   // أخضر حيوي - اللون الرئيسي
    val PrimaryDark   = Color(0xFF00A855)   // أخضر داكن
    val PrimaryGlow   = Color(0xFF00FF7F)   // أخضر متوهج

    // Accent
    val AccentOrange  = Color(0xFFFF6B2B)   // برتقالي - للعروض
    val AccentYellow  = Color(0xFFFFD60A)   // أصفر - للتحذيرات
    val AccentPurple  = Color(0xFF9B59B6)   // بنفسجي
    val AccentCyan    = Color(0xFF00E5FF)   // سماوي

    // Text
    val TextPrimary   = Color(0xFFEEF2FF)   // نص رئيسي
    val TextSecondary = Color(0xFF94A3B8)   // نص ثانوي
    val TextMuted     = Color(0xFF4A6080)   // نص خافت

    // Status
    val Success       = Color(0xFF00D26A)
    val Warning       = Color(0xFFFFD60A)
    val Error         = Color(0xFFFF4757)
    val Info          = Color(0xFF00E5FF)

    // Category colors
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

    // Sidebar
    val SidebarBg     = Color(0xFF0D1E35)
    val SidebarActive = Color(0xFF152847)

    // Login
    val LoginBg1      = Color(0xFF060D1A)
    val LoginBg2      = Color(0xFF0A1628)
    val LoginCard     = Color(0xCC0F1F3D)
}

private val DarkColorScheme = darkColorScheme(
    primary            = SMColors.Primary,
    onPrimary          = Color.Black,
    primaryContainer   = SMColors.PrimaryDark,
    onPrimaryContainer = SMColors.TextPrimary,
    secondary          = SMColors.AccentCyan,
    onSecondary        = Color.Black,
    background         = SMColors.BgDeep,
    onBackground       = SMColors.TextPrimary,
    surface            = SMColors.BgSurface,
    onSurface          = SMColors.TextPrimary,
    surfaceVariant     = SMColors.BgCard,
    onSurfaceVariant   = SMColors.TextSecondary,
    error              = SMColors.Error,
    onError            = Color.White,
    outline            = SMColors.BgCardBorder
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
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = SMTypography,
        content     = content
    )
}
