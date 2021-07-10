package com.fishhawk.driftinglibraryandroid.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.explore.ExploreScreen
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryEditScreen
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryScreen
import com.fishhawk.driftinglibraryandroid.ui.globalsearch.GlobalSearchScreen
import com.fishhawk.driftinglibraryandroid.ui.history.HistoryScreen
import com.fishhawk.driftinglibraryandroid.ui.library.LibraryScreen
import com.fishhawk.driftinglibraryandroid.ui.more.MoreScreen
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderScreen
import com.fishhawk.driftinglibraryandroid.ui.search.SearchScreen
import com.fishhawk.driftinglibraryandroid.ui.server.ServerScreen
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.ui.BottomNavigation
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val labelResId: Int, val icon: ImageVector) {
    object Library : Screen("library", R.string.label_library, Icons.Filled.CollectionsBookmark)
    object History : Screen("history", R.string.label_history, Icons.Filled.History)
    object Explore : Screen("explore", R.string.label_explore, Icons.Filled.Explore)
    object More : Screen("more", R.string.label_more, Icons.Filled.MoreHoriz)

    companion object {
        val items = listOf(Library, History, Explore, More)
    }
}

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainContent() }
    }
}

@Composable
private fun MainContent() {
    val theme = GlobalPreference.theme.asFlow().collectAsState(GlobalPreference.theme.get())

    val navController = rememberNavController()
    ApplicationTheme(theme.value) {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        SideEffect {
            systemUiController.setSystemBarsColor(Color.Transparent, useDarkIcons)
            systemUiController.setNavigationBarColor(Color.Transparent, useDarkIcons)
        }

        ProvideWindowInsets {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { innerPadding ->
                val startScreen = when (GlobalPreference.startScreen.get()) {
                    GlobalPreference.StartScreen.LIBRARY -> Screen.Library
                    GlobalPreference.StartScreen.HISTORY -> Screen.History
                    GlobalPreference.StartScreen.EXPLORE -> Screen.Explore
                }
                NavHost(
                    navController = navController,
                    startDestination = startScreen.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Library.route) { LibraryScreen(navController) }
                    composable(Screen.History.route) { HistoryScreen(navController) }
                    composable(Screen.Explore.route) { ExploreScreen(navController) }
                    composable(Screen.More.route) { MoreScreen(navController) }

                    composable("library-search") { LibraryScreen(navController) }

                    composable("global-search") { GlobalSearchScreen(navController) }
                    composable("provider/{providerId}") { ProviderScreen(navController) }
                    composable("search/{providerId}") { SearchScreen(navController) }

                    composable("server") { ServerScreen(navController) }

                    navigation(startDestination = "detail", route = "gallery/{mangaId}") {
                        composable("detail") { GalleryScreen(navController) }
                        composable("edit") { GalleryEditScreen(navController) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    if (Screen.items.none { it.route == currentDestination?.route }) return

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Screen.items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = screen.route) },
                label = { Text(stringResource(screen.labelResId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
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
