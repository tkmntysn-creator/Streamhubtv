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
import androidx.compose.runtime.getValue
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

    // The Player screen owns the entire screen (video should never share space with
    // app chrome), so we skip the Scaffold/Rail wrapper completely for it. This also
    // fixes a rotation bug: previously, rotating the phone while the player was open
    // could flip `useRail` (since width/height swap on rotation), tearing down and
    // re-creating the whole content subtree - which reset the player's local
    // `isFullscreen` state back to false right after the user turned it on.
    val isPlayerRoute = currentDestination?.route == Destination.Player.route
    if (isPlayerRoute) {
        content(Modifier.fillMaxSize())
        return
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
