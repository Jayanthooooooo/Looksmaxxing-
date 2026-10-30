package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import com.example.data.remote.configureAppCheck
import com.example.ui.nav.AppNavGraph
import com.example.ui.theme.LumoraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check initialization with launch intent before any Firebase or UI operation
        try {
            configureAppCheck(this, intent)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "App Check init failed", e)
        }

        enableEdgeToEdge()

        val app = application as LumoraApp
        val preferences = app.container.preferencesDataStore

        setContent {
            val themeMode by preferences.themeModeFlow.collectAsState(initial = "SYSTEM")
            val blockScreenshots by preferences.blockScreenshotsFlow.collectAsState(initial = false)

            // Respect blockScreenshots setting
            if (blockScreenshots) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            val configuration = LocalConfiguration.current
            val isExpanded = configuration.screenWidthDp >= 600

            LumoraTheme(darkTheme = isDark) {
                Surface {
                    AppNavGraph(isExpanded = isExpanded)
                }
            }
        }
    }
}
