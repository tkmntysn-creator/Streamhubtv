package com.streamhub.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.streamhub.tv.data.local.ThemeMode
import com.streamhub.tv.data.repository.SettingsRepository
import com.streamhub.tv.ui.navigation.AdaptiveNavScaffold
import com.streamhub.tv.ui.navigation.StreamHubNavHost
import com.streamhub.tv.ui.theme.StreamHubTVTheme
import com.streamhub.tv.util.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val isTv = DeviceUtils.isTelevision(this)
            val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.DARK)

            StreamHubTVTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                Box(modifier = Modifier) {
                    AdaptiveNavScaffold(
                        navController = navController,
                        widthSizeClass = windowSizeClass.widthSizeClass,
                        isTv = isTv
                    ) { modifier ->
                        StreamHubNavHost(navController = navController, modifier = modifier)
                    }
                }
            }
        }
    }
}
