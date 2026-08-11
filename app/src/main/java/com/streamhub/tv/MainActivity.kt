package com.streamhub.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.streamhub.tv.data.local.ThemeMode
import com.streamhub.tv.data.repository.ActivationRepository
import com.streamhub.tv.data.repository.SettingsRepository
import com.streamhub.tv.ui.navigation.AdaptiveNavScaffold
import com.streamhub.tv.ui.navigation.StreamHubNavHost
import com.streamhub.tv.ui.screens.activation.ActivationScreen
import com.streamhub.tv.ui.theme.StreamHubTVTheme
import com.streamhub.tv.util.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var activationRepository: ActivationRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val isTv = DeviceUtils.isTelevision(this)
            val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.DARK)
            // null = still checking local storage, true/false = known activation state.
            val isActivatedPersisted by activationRepository.isActivated.collectAsState(initial = null)
            // Once the user successfully activates in this session, flip this immediately
            // so the app moves on without waiting for the DataStore Flow to re-emit.
            var justActivated by remember { mutableStateOf(false) }

            StreamHubTVTheme(themeMode = themeMode) {
                val isActivated = justActivated || isActivatedPersisted == true

                if (isActivatedPersisted == null) {
                    // Brief moment while reading DataStore - render nothing to avoid a flash
                    // of the activation screen for already-activated users.
                    Box(modifier = Modifier)
                } else if (!isActivated) {
                    ActivationScreen(onActivated = { justActivated = true })
                } else {
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
}
