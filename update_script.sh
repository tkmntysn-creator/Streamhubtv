cat > app/build.gradle.kts << 'FILEEOF'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.streamhub.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.streamhub.tv"
        minSdk = 21          // Supports phones, tablets, Android TV & TV boxes
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Default GitHub raw JSON URL - can be changed at runtime from Settings > Repository
        buildConfigField(
            "String",
            "DEFAULT_CHANNELS_URL",
            "\"https://raw.githubusercontent.com/tkmntysn-creator/Channels/main/channels.json\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Adaptive layout (tablets / foldables / TV)
    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0")

    // TV support is handled via standard Compose (adaptive Nav Rail) + leanback manifest flags,
    // so the androidx.tv Compose libraries are not required here.
    implementation("androidx.leanback:leanback:1.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Networking - Retrofit + Kotlinx Serialization
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore (Settings)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil (Image loading)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Media3 ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.4.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.0")
    implementation("androidx.media3:media3-ui:1.4.0")
    implementation("androidx.media3:media3-session:1.4.0")
    implementation("androidx.media3:media3-common:1.4.0")
    implementation("androidx.media3:media3-datasource-okhttp:1.4.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Accompanist (system UI controller for immersive fullscreen player)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
FILEEOF

mkdir -p app/src/main/java/com/streamhub/tv/ui/navigation
cat > app/src/main/java/com/streamhub/tv/ui/navigation/Destinations.kt << 'FILEEOF'
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
FILEEOF

cat > app/src/main/java/com/streamhub/tv/ui/navigation/NavGraph.kt << 'FILEEOF'
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
FILEEOF

mkdir -p app/src/main/java/com/streamhub/tv/ui/components
cat > app/src/main/java/com/streamhub/tv/ui/components/ChannelCard.kt << 'FILEEOF'
package com.streamhub.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.ui.theme.AppRadii

/**
 * A rounded, glassmorphism-styled card used across Home / Live TV / Search / Favorites
 * to represent a single channel. Tapping opens the player; tapping the heart toggles
 * favorite status. Set [showPlayOverlay] for Netflix-style "Continue Watching" rows,
 * which draws a centered play button over the thumbnail.
 */
@Composable
fun ChannelCard(
    channel: Channel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    showPlayOverlay: Boolean = false
) {
    Column(
        modifier = modifier
            .width(150.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(AppRadii.Card)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, Color.White.copy(alpha = 0.08f), AppRadii.Card)
        ) {
            AsyncImage(
                model = channel.logo,
                contentDescription = channel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Glass gradient overlay for legibility of the live badge / favorite button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                        )
                    )
            )
            if (showPlayOverlay) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(AppRadii.Chip)
                    .background(Color.Red.copy(alpha = 0.85f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LiveTv, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Text(" LIVE", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Toggle favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else Color.White.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = channel.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 2.dp)
        )
        Text(
            text = channel.country.ifBlank { channel.category },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
FILEEOF

cat > app/src/main/java/com/streamhub/tv/ui/components/SharedComponents.kt << 'FILEEOF'
package com.streamhub.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.ui.theme.GradientHero

/** A titled horizontally-scrolling row of channel cards, e.g. for a category, "My List",
 *  or "Continue Watching". Set [showPlayOverlay] to draw a centered play icon on every
 *  thumbnail (Netflix-style "Continue Watching" look). */
@Composable
fun CategoryRow(
    title: String,
    channels: List<Channel>,
    favoriteIds: Set<String>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onSeeAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showPlayOverlay: Boolean = false
) {
    if (channels.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (onSeeAll != null) {
                TextButton(onClick = onSeeAll) { Text("See all") }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(channels) { channel ->
                ChannelCard(
                    channel = channel,
                    isFavorite = favoriteIds.contains(channel.id),
                    onClick = { onChannelClick(channel) },
                    onToggleFavorite = { onToggleFavorite(channel) },
                    showPlayOverlay = showPlayOverlay
                )
            }
        }
    }
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FullScreenError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Something went wrong", style = MaterialTheme.typography.titleLarge)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

/** Decorative hero gradient background used behind the Home screen header. */
@Composable
fun HeroGradientBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(GradientHero))
    ) {
        content()
    }
}
FILEEOF

mkdir -p app/src/main/java/com/streamhub/tv/ui/screens/home
cat > app/src/main/java/com/streamhub/tv/ui/screens/home/HomeScreen.kt << 'FILEEOF'
package com.streamhub.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.ChannelCategory
import com.streamhub.tv.ui.components.CategoryRow
import com.streamhub.tv.ui.components.FullScreenError
import com.streamhub.tv.ui.components.FullScreenLoading

/**
 * Netflix-inspired home screen:
 *  - Top bar: wordmark logo + notifications bell only (no download icon)
 *  - Horizontal category chips (Sports, News, Movies, ...)
 *  - Hero banner for a featured channel with "Watch Live" + "My List" actions
 *  - "Continue Watching" row (with a centered play icon per thumbnail)
 *  - "My List" row (favorites)
 *  - One row per category
 */
@Composable
fun HomeScreen(
    onChannelClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HomeTopBar()

        when {
            state.isLoading && state.categorized.isEmpty() -> FullScreenLoading()
            state.errorMessage != null && state.categorized.isEmpty() -> FullScreenError(
                message = state.errorMessage ?: "Unknown error",
                onRetry = { viewModel.loadChannels(forceRefresh = true) }
            )
            else -> HomeContent(
                state = state,
                onChannelClick = { onChannelClick(it.id) },
                onToggleFavorite = viewModel::toggleFavorite,
                onCategoryClick = onCategoryClick
            )
        }
    }
}

/** Minimal top bar: wordmark on the left, a single notifications bell on the right.
 *  Deliberately has NO download icon, per the current design direction. */
@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.LiveTv,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Text(
                "StreamHub",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        IconButton(onClick = { /* No notifications yet - reserved for future use */ }) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onCategoryClick: (String) -> Unit
) {
    val heroChannel = state.featured.firstOrNull()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            CategoryChipsRow(onCategoryClick = onCategoryClick)
        }

        heroChannel?.let { hero ->
            item {
                HeroBanner(
                    channel = hero,
                    isFavorite = state.favoriteIds.contains(hero.id),
                    onWatch = { onChannelClick(hero) },
                    onToggleFavorite = { onToggleFavorite(hero) }
                )
            }
        }

        if (state.recentlyWatched.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "Continue Watching",
                    channels = state.recentlyWatched,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    showPlayOverlay = true,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }

        if (state.favorites.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "My List",
                    channels = state.favorites,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }

        items(ChannelCategory.orderedCategories) { category ->
            val channels = state.categorized[category].orEmpty()
            if (channels.isNotEmpty()) {
                CategoryRow(
                    title = category.displayName,
                    channels = channels,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    onSeeAll = { onCategoryClick(category.displayName) },
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }

        if (state.recommended.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "Recommended For You",
                    channels = state.recommended,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(top = 20.dp, bottom = 32.dp)
                )
            }
        }
    }
}

/** Horizontally scrolling category filter chips, right under the top bar. */
@Composable
private fun CategoryChipsRow(onCategoryClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ChannelCategory.orderedCategories) { category ->
            FilterChip(
                selected = false,
                onClick = { onCategoryClick(category.displayName) },
                label = { Text(category.displayName) }
            )
        }
    }
}

/** Big featured banner at the top of Home, styled after Netflix's hero card:
 *  full-width backdrop image, title, category/country tags, and two actions. */
@Composable
private fun HeroBanner(
    channel: Channel,
    isFavorite: Boolean,
    onWatch: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        AsyncImage(
            model = channel.logo,
            contentDescription = channel.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                channel.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Text(
                listOfNotNull(channel.category.ifBlank { null }, channel.country.ifBlank { null })
                    .joinToString("  •  "),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onWatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("Watch Live", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null
                    )
                    Text(
                        if (isFavorite) "In My List" else "My List",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
FILEEOF

mkdir -p app/src/main/java/com/streamhub/tv/ui/screens/settings
cat > app/src/main/java/com/streamhub/tv/ui/screens/settings/SettingsScreen.kt << 'FILEEOF'
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
        SettingsSectionTitle("Channels")
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
        SettingsRow(
            title = "Reset Activation",
            subtitle = "Require the activation code again next time the app opens",
            onClick = viewModel::resetActivation
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
FILEEOF
