package com.streamhub.tv.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamhub.tv.BuildConfig
import com.streamhub.tv.R
import com.streamhub.tv.data.local.ThemeMode

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(top = 20.dp, bottom = 24.dp)
        )

        SettingsSection(stringResource(R.string.section_appearance)) {
            SettingsCard {
                SettingsIconRow(
                    icon = Icons.Filled.Brightness4,
                    title = stringResource(R.string.dark_appearance),
                    subtitle = stringResource(R.string.dark_appearance_subtitle),
                    selected = state.themeMode == ThemeMode.DARK
                ) { viewModel.setThemeMode(ThemeMode.DARK) }
                SettingsDivider()
                SettingsIconRow(
                    icon = Icons.Filled.Brightness7,
                    title = stringResource(R.string.light_appearance),
                    subtitle = stringResource(R.string.light_appearance_subtitle),
                    selected = state.themeMode == ThemeMode.LIGHT
                ) { viewModel.setThemeMode(ThemeMode.LIGHT) }
                SettingsDivider()
                SettingsIconRow(
                    icon = Icons.Filled.BrightnessAuto,
                    title = stringResource(R.string.system_appearance),
                    subtitle = stringResource(R.string.system_appearance_subtitle),
                    selected = state.themeMode == ThemeMode.SYSTEM
                ) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
            }
        }

        SettingsSection(stringResource(R.string.section_channels)) {
            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Filled.Refresh,
                    title = stringResource(R.string.automatic_updates),
                    subtitle = stringResource(R.string.automatic_updates_subtitle),
                    checked = state.autoUpdate,
                    onCheckedChange = viewModel::setAutoUpdate
                )
                if (state.lastSyncAt.isNotBlank()) {
                    SettingsDivider()
                    SettingsInfoRow(
                        icon = Icons.Filled.Shield,
                        title = stringResource(R.string.last_catalogue_sync),
                        value = state.lastSyncAt
                    )
                }
            }
        }

        SettingsSection(stringResource(R.string.section_storage)) {
            SettingsCard {
                SettingsActionRow(
                    icon = Icons.Filled.Delete,
                    title = stringResource(R.string.clear_local_cache),
                    subtitle = stringResource(R.string.clear_local_cache_subtitle),
                    onClick = viewModel::clearCache
                )
            }
        }

        SettingsSection(stringResource(R.string.section_language)) {
            SettingsCard {
                LanguageChoice(stringResource(R.string.english), "en", state.language, Icons.Filled.Language) { viewModel.setLanguage("en") }
                SettingsDivider()
                LanguageChoice(stringResource(R.string.arabic), "ar", state.language, Icons.Filled.Language) { viewModel.setLanguage("ar") }
                SettingsDivider()
                LanguageChoice(stringResource(R.string.french), "fr", state.language, Icons.Filled.Language) { viewModel.setLanguage("fr") }
            }
        }

        SettingsSection(stringResource(R.string.section_about)) {
            SettingsCard {
                SettingsInfoRow(Icons.Filled.Info, stringResource(R.string.version), BuildConfig.VERSION_NAME)
                SettingsDivider()
                SettingsInfoRow(Icons.Filled.Shield, stringResource(R.string.privacy), stringResource(R.string.privacy_value))
            }
        }
        Text(
            text = "StreamHub TV · Midnight Broadcast",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 22.dp, bottom = 32.dp)
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
    ) { content() }
}

@Composable
private fun SettingsDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}

@Composable
private fun SettingsIconRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IosSelectionDot(selected)
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon, MaterialTheme.colorScheme.secondary)
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun SettingsActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon, MaterialTheme.colorScheme.tertiary)
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon, MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LanguageChoice(label: String, code: String, current: String, icon: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon, MaterialTheme.colorScheme.secondary)
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(start = 12.dp))
        IosSelectionDot(selected = current == code)
    }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color) {
    Row(
        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.18f)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp)) }
}

@Composable
private fun IosSelectionDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, androidx.compose.foundation.shape.CircleShape)
            .padding(4.dp)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, androidx.compose.foundation.shape.CircleShape)
    )
}
