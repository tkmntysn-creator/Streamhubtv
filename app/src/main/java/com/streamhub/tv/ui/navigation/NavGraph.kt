package com.streamhub.tv.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.AnimatedContentTransitionScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.streamhub.tv.ui.screens.favorites.FavoritesScreen
import com.streamhub.tv.ui.screens.home.HomeScreen
import com.streamhub.tv.ui.screens.livetv.CategoryDetailScreen
import com.streamhub.tv.ui.screens.livetv.LiveTvScreen
import com.streamhub.tv.ui.screens.player.PlayerScreen
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
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(220)) + scaleIn(
                initialScale = 0.985f,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(180)) + scaleOut(
                targetScale = 0.985f,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(220)) + scaleIn(
                initialScale = 0.985f,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(180)) + scaleOut(
                targetScale = 0.985f,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            )
        }
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
            SettingsScreen()
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
