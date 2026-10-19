package com.streamhub.tv.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.streamhub.tv.ui.theme.AppRadii

/** Adaptive iOS-inspired navigation: floating Liquid Glass on phones, rail on large screens/TV. */
@Composable
fun AdaptiveNavScaffold(
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    isTv: Boolean,
    content: @Composable (Modifier) -> Unit
) {
    val useRail = isTv || widthSizeClass != WindowWidthSizeClass.Compact
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun isSelected(dest: Destination) =
        currentDestination?.hierarchy?.any { it.route == dest.route } == true

    fun onSelect(dest: Destination) {
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val isPlayerRoute = currentDestination?.route == Destination.Player.route
    if (isPlayerRoute) {
        content(Modifier.fillMaxSize())
        return
    }

    if (useRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                topLevelDestinations.forEach { item ->
                    val selected = isSelected(item.destination)
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onSelect(item.destination) },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
            content(Modifier.fillMaxSize())
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                LiquidGlassBottomBar(
                    onSelect = ::onSelect,
                    isSelected = ::isSelected
                )
            }
        ) { paddingValues ->
            content(Modifier.fillMaxSize().padding(paddingValues))
        }
    }
}

@Composable
private fun LiquidGlassBottomBar(
    onSelect: (Destination) -> Unit,
    isSelected: (Destination) -> Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppRadii.GlassBar)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.08f))
                    ),
                    shape = AppRadii.GlassBar
                )
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            topLevelDestinations.forEach { item ->
                GlassNavigationItem(
                    item = item,
                    selected = isSelected(item.destination),
                    onClick = { onSelect(item.destination) }
                )
            }
        }
    }
}

@Composable
private fun GlassNavigationItem(
    item: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label
            )
        },
        label = { Text(item.label) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
