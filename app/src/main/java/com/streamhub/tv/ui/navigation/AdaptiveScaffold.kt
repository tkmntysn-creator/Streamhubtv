package com.streamhub.tv.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

private val FloatingNavShape = RoundedCornerShape(42.dp)
private val SelectedTabShape = RoundedCornerShape(34.dp)
private val FloatingNavContainer = Color(0xFF303033)
private val FloatingNavSelected = Color(0xFF404044)
private val FloatingNavBorder = Color.White.copy(alpha = 0.12f)
private val FloatingNavUnselectedContent = Color(0xFFC8C5CC)

/**
 * Chooses the right navigation chrome for the current form factor:
 *  - Phone (compact width)          -> Floating, Netflix-inspired bottom navigation.
 *  - Tablet (medium/expanded width) -> Side Navigation Rail.
 *  - Android TV / TV Box            -> Side Navigation Rail (focusable, D-pad friendly).
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
    // avoids re-creating the player subtree when the device orientation changes.
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
            bottomBar = {
                FloatingBottomNavigation(
                    isSelected = ::isSelected,
                    onSelect = ::onSelect
                )
            }
        ) { paddingValues ->
            content(Modifier.fillMaxSize().padding(paddingValues))
        }
    }
}

/** A compact floating phone navigation bar modelled after the Netflix mobile chrome. */
@Composable
private fun FloatingBottomNavigation(
    isSelected: (Destination) -> Boolean,
    onSelect: (Destination) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .border(1.dp, FloatingNavBorder, FloatingNavShape),
            shape = FloatingNavShape,
            color = FloatingNavContainer,
            contentColor = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                topLevelDestinations.forEach { item ->
                    val selected = isSelected(item.destination)
                    val contentColor = if (selected) Color.White else FloatingNavUnselectedContent

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp)
                            .clip(SelectedTabShape)
                            .background(if (selected) FloatingNavSelected else Color.Transparent)
                            .clickable(
                                role = Role.Tab,
                                onClick = { onSelect(item.destination) }
                            )
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(28.dp),
                            tint = contentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
