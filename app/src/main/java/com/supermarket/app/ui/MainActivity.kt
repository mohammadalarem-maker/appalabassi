package com.supermarket.app.ui
import com.supermarket.app.ui.smOutlinedColors

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.supermarket.app.ui.navigation.AppNavigation
import com.supermarket.app.ui.theme.SMColors
import com.supermarket.app.ui.theme.SuperMarketTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.SideEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
