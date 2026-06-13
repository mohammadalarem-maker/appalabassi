package com.supermarket.app.ui
import com.supermarket.app.ui.smOutlinedColors

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.supermarket.app.ui.navigation.AppNavigation
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.ui.theme.SuperMarketTheme
import com.supermarket.app.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* تم منح أو رفض الإذن */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // إنشاء قنوات الإشعارات
        NotificationHelper.createChannels(this)

        // طلب إذن الإشعارات لـ Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            SuperMarketTheme {
                val systemUi = rememberSystemUiController()
                SideEffect {
                    systemUi.setSystemBarsColor(color = SMColors.BgDeep, darkIcons = false)
                }
                Surface(modifier = Modifier.fillMaxSize(), color = SMColors.BgDeep) {
                    AppNavigation(navController = rememberNavController())
                }
            }
        }
    }
}
