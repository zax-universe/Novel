package com.azxuniverse.azxnov

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.azxuniverse.azxnov.ui.navigation.Screen
import com.azxuniverse.azxnov.ui.screens.*
import com.azxuniverse.azxnov.ui.theme.AppTheme
import com.azxuniverse.azxnov.ui.viewmodel.NovelViewModel
import com.azxuniverse.azxnov.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: NovelViewModel = viewModel(factory = ViewModelFactory(context))

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentDestination)) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    listOf(
                        Triple(Screen.Home, Icons.Rounded.Home, "Home"),
                        Triple(Screen.Search, Icons.Rounded.Search, "Search"),
                        Triple(Screen.Library, Icons.Rounded.AutoStories, "Library"),
                        Triple(Screen.About, Icons.Rounded.Info, "About")
                    ).forEach { (screen, icon, label) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(screen::class) } == true,
                            onClick = {
                                navController.navigate(screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Home> { HomeScreen(viewModel, navController) }
            composable<Screen.Search> { SearchScreen(viewModel, navController) }
            composable<Screen.Library> { LibraryScreen(viewModel, navController) }
            composable<Screen.About> { AboutScreen() }
            composable<Screen.Detail> { backStackEntry ->
                val detail: Screen.Detail = backStackEntry.toRoute()
                DetailScreen(detail.url, viewModel, navController)
            }
            composable<Screen.Reader> { backStackEntry ->
                val reader: Screen.Reader = backStackEntry.toRoute()
                ReaderScreen(reader.url, reader.novelUrl, viewModel, navController)
            }
        }
    }
}

private fun shouldShowBottomBar(destination: NavDestination?): Boolean {
    return destination?.hierarchy?.any {
        it.hasRoute(Screen.Home::class) ||
        it.hasRoute(Screen.Search::class) ||
        it.hasRoute(Screen.Library::class) ||
        it.hasRoute(Screen.About::class)
    } == true
}
