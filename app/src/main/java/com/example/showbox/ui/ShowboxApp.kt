package com.example.showbox.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.showbox.data.Category
import com.example.showbox.ui.screens.EntryListScreen
import com.example.showbox.ui.screens.PlayerScreen
import com.example.showbox.ui.screens.ShiftScreen

/**
 * The five bottom-bar sections. [category] is null for the music player;
 * every other destination is the shared entry screen for that category.
 */
private enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val category: Category?,
) {
    SHIFT("shift", "Schicht", Icons.Filled.DateRange, null),
    MUSIC("music", "Musik", Icons.Filled.PlayArrow, null),
    QUIZ("quiz", "Quiz", Icons.Filled.Star, Category.QUIZ),
    SPEED_DATING("dating", "Dating", Icons.Filled.Favorite, Category.SPEED_DATING),
    ESTIMATION("estimation", "Schätzen", Icons.Filled.Search, Category.ESTIMATION),
    PODCAST("podcast", "Podcast", Icons.Filled.Create, Category.PODCAST),
}

/**
 * Root of the UI. Both view models are created here — outside any nav
 * destination — so they are activity-scoped and shared across sections.
 */
@Composable
fun ShowboxApp() {
    val navController = rememberNavController()
    val library: LibraryViewModel = viewModel()
    val player: PlayerViewModel = viewModel()
    val shift: ShiftViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.SHIFT.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            Destination.entries.forEach { destination ->
                composable(destination.route) {
                    val category = destination.category
                    when {
                        category != null -> EntryListScreen(category = category, library = library)
                        destination == Destination.SHIFT -> ShiftScreen(shift = shift)
                        else -> PlayerScreen(library = library, player = player)
                    }
                }
            }
        }
    }
}
