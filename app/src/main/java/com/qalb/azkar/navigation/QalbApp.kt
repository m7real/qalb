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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.qalb.azkar.R
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
                val currentRoute = backStackEntry?.destination?.route

                topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
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
            startDestination = "today",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("today") { TodayScreen() }
            composable("library") { LibraryScreen() }
            composable("tasbih") { TasbihScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
