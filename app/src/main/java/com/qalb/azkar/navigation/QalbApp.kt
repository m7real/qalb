package com.qalb.azkar.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.qalb.azkar.R
import com.qalb.azkar.feature.library.CollectionDetailScreen
import com.qalb.azkar.feature.library.CollectionsScreen
import com.qalb.azkar.feature.library.DhikrDetailScreen
import com.qalb.azkar.feature.library.DhikrEditorScreen
import com.qalb.azkar.feature.library.LibraryScreen
import com.qalb.azkar.feature.settings.SettingsScreen
import com.qalb.azkar.feature.tasbih.TasbihScreen
import com.qalb.azkar.feature.today.TodayScreen

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val iconRes: Int,
)

/** Top-level destinations. Order matters for the bottom bar visual order. */
private val topLevelDestinations = listOf(
    TopLevelDestination("today", R.string.nav_today, R.drawable.ic_nav_today),
    TopLevelDestination("library", R.string.nav_library, R.drawable.ic_nav_library),
    TopLevelDestination("tasbih", R.string.nav_tasbih, R.drawable.ic_nav_tasbih),
    TopLevelDestination("settings", R.string.nav_settings, R.drawable.ic_nav_settings),
)

private const val ROUTE_TODAY = "today"
private const val ROUTE_LIBRARY = "library"
private const val ROUTE_TASBIH = "tasbih"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_COLLECTIONS = "collections"
private const val ROUTE_COLLECTION = "collection"
private const val ROUTE_DETAIL = "detail"
private const val ROUTE_EDITOR = "editor"

/** Route prefixes that belong to the Library tab for bottom-bar selection. */
private val libraryStackPrefixes =
    listOf(ROUTE_LIBRARY, ROUTE_COLLECTIONS, ROUTE_COLLECTION, ROUTE_DETAIL, ROUTE_EDITOR)

private fun selectedTopLevelRoute(currentRoute: String?): String? {
    if (currentRoute == null) return null
    topLevelDestinations.firstOrNull { it.route == currentRoute }?.let { return it.route }
    if (libraryStackPrefixes.any { currentRoute.startsWith(it) }) return ROUTE_LIBRARY
    return null
}

private fun tabNavOptions(navController: NavController): NavOptions =
    navOptions {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

@Composable
fun QalbApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val selectedRoute = selectedTopLevelRoute(backStackEntry?.destination?.route)

                topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route, tabNavOptions(navController))
                        },
                        icon = {
                            // Decorative: the visible text label carries the accessible name.
                            Icon(
                                painter = painterResource(destination.iconRes),
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_TODAY,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ROUTE_TODAY) { TodayScreen() }
            composable(ROUTE_LIBRARY) {
                LibraryScreen(
                    onOpenDetail = { id -> navController.navigate("$ROUTE_DETAIL/$id") },
                    onAddNew = { navController.navigate(ROUTE_EDITOR) },
                    onOpenCollections = { navController.navigate(ROUTE_COLLECTIONS) },
                )
            }
            composable(ROUTE_COLLECTIONS) {
                CollectionsScreen(
                    onOpenCollection = { id -> navController.navigate("$ROUTE_COLLECTION/$id") },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "$ROUTE_COLLECTION/{collectionId}",
                arguments = listOf(navArgument("collectionId") { type = NavType.StringType }),
            ) { entry ->
                CollectionDetailScreen(
                    collectionId = entry.arguments?.getString("collectionId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenDhikr = { id -> navController.navigate("$ROUTE_DETAIL/$id") },
                )
            }
            composable(
                route = "$ROUTE_DETAIL/{dhikrId}",
                arguments = listOf(navArgument("dhikrId") { type = NavType.StringType }),
            ) { entry ->
                DhikrDetailScreen(
                    dhikrId = entry.arguments?.getString("dhikrId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate("$ROUTE_EDITOR/$id") },
                )
            }
            composable(ROUTE_EDITOR) {
                DhikrEditorScreen(
                    existingDhikrId = null,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "$ROUTE_EDITOR/{dhikrId}",
                arguments = listOf(navArgument("dhikrId") { type = NavType.StringType }),
            ) { entry ->
                DhikrEditorScreen(
                    existingDhikrId = entry.arguments?.getString("dhikrId"),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ROUTE_TASBIH) { TasbihScreen() }
            composable(ROUTE_SETTINGS) { SettingsScreen() }
        }
    }
}
