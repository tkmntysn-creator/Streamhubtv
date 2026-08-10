package com.streamhub.tv.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.streamhub.tv.ui.screens.favorites.FavoritesScreen
import com.streamhub.tv.ui.screens.home.HomeScreen
import com.streamhub.tv.ui.screens.livetv.CategoryDetailScreen
import com.streamhub.tv.ui.screens.livetv.LiveTvScreen
import com.streamhub.tv.ui.screens.player.PlayerScreen
import com.streamhub.tv.ui.screens.repoconfig.RepositoryConfigScreen
import com.streamhub.tv.ui.screens.search.SearchScreen
import com.streamhub.tv.ui.screens.settings.SettingsScreen

@Composable
fun StreamHubNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(Destination.Home.route) {
            HomeScreen(
                onChannelClick = { channelId ->
                    navController.navigate(Destination.Player.createRoute(channelId))
                },
                onCategoryClick = { category ->
                    navController.navigate(Destination.CategoryDetail.createRoute(category))
                }
            )
        }
        composable(Destination.LiveTv.route) {
            LiveTvScreen(
                onChannelClick = { channelId ->
                    navController.navigate(Destination.Player.createRoute(channelId))
                }
            )
        }
        composable(Destination.Favorites.route) {
            FavoritesScreen(
                onChannelClick = { channelId ->
                    navController.navigate(Destination.Player.createRoute(channelId))
                }
            )
        }
        composable(Destination.Search.route) {
            SearchScreen(
                onChannelClick = { channelId ->
                    navController.navigate(Destination.Player.createRoute(channelId))
                }
            )
        }
        composable(Destination.Settings.route) {
            SettingsScreen(
                onOpenRepositoryConfig = { navController.navigate(Destination.RepositoryConfig.route) }
            )
        }
        composable(Destination.RepositoryConfig.route) {
            RepositoryConfigScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Destination.Player.route,
            arguments = listOf(navArgument("channelId") { defaultValue = "" })
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            PlayerScreen(channelId = channelId, onBack = { navController.popBackStack() })
        }
        composable(
            route = Destination.CategoryDetail.route,
            arguments = listOf(navArgument("categoryName") { defaultValue = "" })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryDetailScreen(
                categoryName = categoryName,
                onChannelClick = { channelId ->
                    navController.navigate(Destination.Player.createRoute(channelId))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
