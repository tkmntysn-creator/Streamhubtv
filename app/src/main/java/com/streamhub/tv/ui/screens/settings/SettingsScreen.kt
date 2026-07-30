package com.streamhub.tv.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamhub.tv.BuildConfig
import com.streamhub.tv.data.local.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenRepositoryConfig: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("Settings", style = MaterialTheme.typography.headlineMedium) })

        SettingsSectionTitle("Appearance")
        ThemeOptionRow("Dark", state.themeMode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
        ThemeOptionRow("Light", state.themeMode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
        ThemeOptionRow("System Default", state.themeMode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }

        Divider()
        SettingsSectionTitle("Channel Source")
        SettingsRow(
            title = "GitHub Repository URL",
            subtitle = state.repoUrl,
            onClick = onOpenRepositoryConfig
        )
        SettingsToggleRow(
            title = "Auto Update Channels",
            subtitle = "Automatically refresh channels.json in the background",
            checked = state.autoUpdate,
            onCheckedChange = viewModel::setAutoUpdate
        )
        if (state.lastSyncAt.isNotBlank()) {
            SettingsInfoRow(title = "Last Synced", value = state.lastSyncAt)
        }

        Divider()
        SettingsSectionTitle("Storage")
        SettingsRow(
            title = "Clear Cache",
            subtitle = "Remove cached channel list and thumbnails",
            onClick = viewModel::clearCache
        )

        Divider()
        SettingsSectionTitle("Language")
        LanguageOptionRow("English", "en", state.language) { viewModel.setLanguage("en") }
        LanguageOptionRow("العربية", "ar", state.language) { viewModel.setLanguage("ar") }
        LanguageOptionRow("Français", "fr", state.language) { viewModel.setLanguage("fr") }

        Divider()
        SettingsSectionTitle("About")
        SettingsInfoRow(title = "App Version", value = BuildConfig.VERSION_NAME)
        SettingsInfoRow(title = "About", value = "StreamHub TV - Premium Live TV Streaming")
        SettingsInfoRow(title = "Privacy", value = "Channel data is loaded from your configured GitHub source. Favorites and history stay on-device.")
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun LanguageOptionRow(label: String, code: String, current: String, onSelect: () -> Unit) {
    ThemeOptionRow(label = label, selected = current == code, onSelect = onSelect)
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsInfoRow(title: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
