package com.streamhub.tv.ui.screens.repoconfig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamhub.tv.data.repository.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryConfigScreen(
    onBack: () -> Unit,
    viewModel: RepositoryConfigViewModel = hiltViewModel()
) {
    val currentUrl by viewModel.repoUrl.collectAsState()
    var textValue by remember { mutableStateOf("") }

    LaunchedEffect(currentUrl) {
        if (textValue.isBlank()) textValue = currentUrl
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Repository Configuration", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "Enter the raw GitHub URL of your channels.json file. The app will " +
                    "download it on startup and cache it locally so channels stay " +
                    "available offline. Editing this JSON on GitHub - adding, removing, " +
                    "renaming, enabling/disabling, or reordering channels - updates the " +
                    "app automatically the next time it refreshes. No app update needed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("channels.json raw URL") },
                placeholder = { Text("https://raw.githubusercontent.com/user/repo/main/channels.json") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
            Button(
                onClick = { viewModel.saveUrl(textValue) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Save & Refresh")
            }
        }
    }
}
