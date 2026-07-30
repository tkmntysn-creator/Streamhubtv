package com.streamhub.tv.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Chooses the right navigation chrome for the current form factor:
 *  - Phone (compact width)         -> Bottom Navigation Bar
 *  - Tablet (medium/expanded width) -> Side Navigation Rail
 *  - Android TV / TV Box            -> Side Navigation Rail (focusable, D-pad friendly)
 */
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

    if (useRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail {
                topLevelDestinations.forEach { item ->
                    val selected = isSelected(item.destination)
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onSelect(item.destination) },
                        icon = {
                            androidx.compose.material3.Icon(
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
            bottomBar = {
                NavigationBar {
                    topLevelDestinations.forEach { item ->
                        val selected = isSelected(item.destination)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onSelect(item.destination) },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            content(Modifier.fillMaxSize().padding(paddingValues))
        }
    }
}
