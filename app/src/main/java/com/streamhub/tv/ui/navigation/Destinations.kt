package com.streamhub.tv.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Every navigable screen in the app. */
sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object LiveTv : Destination("live_tv")
    data object Favorites : Destination("favorites")
    data object Search : Destination("search")
    data object Settings : Destination("settings")
    data object RepositoryConfig : Destination("repository_config")

    data object Player : Destination("player/{channelId}") {
        fun createRoute(channelId: String) = "player/$channelId"
    }

    data object CategoryDetail : Destination("category/{categoryName}") {
        fun createRoute(categoryName: String) = "category/$categoryName"
    }
}

/** Metadata for the top-level tabs shown in Bottom Navigation / Nav Rail / Drawer. */
data class TopLevelDestination(
    val destination: Destination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// Bottom nav order: Home -> Live TV (Channels, center) -> Search -> Settings.
// Favorites no longer has its own tab - it now lives on the Home screen as a
// "My List" row (Netflix-style), reachable via the heart icon on any channel card.
val topLevelDestinations = listOf(
    TopLevelDestination(Destination.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    TopLevelDestination(Destination.LiveTv, "Channels", Icons.Filled.LiveTv, Icons.Outlined.LiveTv),
    TopLevelDestination(Destination.Search, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    TopLevelDestination(Destination.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)
