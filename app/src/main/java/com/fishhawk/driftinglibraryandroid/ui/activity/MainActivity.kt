package com.fishhawk.driftinglibraryandroid.ui.activity

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.datastore.StartScreen
import com.fishhawk.driftinglibraryandroid.data.datastore.getBlocking
import com.fishhawk.driftinglibraryandroid.ui.explore.ExploreScreen
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryEditScreen
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryScreen
import com.fishhawk.driftinglibraryandroid.ui.globalsearch.GlobalSearchScreen
import com.fishhawk.driftinglibraryandroid.ui.history.HistoryScreen
import com.fishhawk.driftinglibraryandroid.ui.library.LibraryScreen
import com.fishhawk.driftinglibraryandroid.ui.more.*
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderScreen
import com.fishhawk.driftinglibraryandroid.ui.search.SearchScreen
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.ui.BottomNavigation
import com.google.accompanist.insets.ui.Scaffold
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
        setContent { MainApp() }
    }
}

@Composable
private fun MainApp() {
    ApplicationTheme {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            MainNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    val startDestination = rememberSaveable {
        when (PR.startScreen.getBlocking()) {
            StartScreen.Library -> Screen.Library
            StartScreen.History -> Screen.History
            StartScreen.Explore -> Screen.Explore
        }.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Library.route) { LibraryScreen(navController) }
        composable(Screen.History.route) { HistoryScreen(navController) }
        composable(Screen.Explore.route) { ExploreScreen(navController) }
        composable(Screen.More.route) { MoreScreen(navController) }

        composable("library-search") { LibraryScreen(navController) }

        composable("global-search") { GlobalSearchScreen(navController) }
        composable("provider/{providerId}") { ProviderScreen(navController) }
        composable("search/{providerId}") { SearchScreen(navController) }

        composable("setting-general") { SettingGeneralScreen(navController) }
        composable("setting-reader") { SettingReaderScreen(navController) }
        composable("setting-advanced") { SettingAdvancedScreen(navController) }
        composable("about") { AboutScreen(navController) }

        navigation(startDestination = "detail", route = "gallery/{mangaId}") {
            composable("detail") { GalleryScreen(navController) }
            composable("edit") { GalleryEditScreen(navController) }
        }

        composable("test") { }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    AnimatedVisibility(
        visible = (Screen.items.any { it.route == currentDestination?.route }),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
            Screen.items.forEach { screen ->
                BottomNavigationItem(
                    icon = { Icon(screen.icon, contentDescription = screen.route) },
                    label = { Text(stringResource(screen.labelResId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                    onClick = {
                        if (screen.route == navController.currentDestination?.route) return@BottomNavigationItem
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
}

fun <T : Parcelable> NavHostController.setArgument(key: String) {
    previousBackStackEntry?.arguments?.let { prev ->
        currentBackStackEntry?.arguments?.putParcelable(
            key, prev.getParcelable<T>(key)
        )
    }
}

fun NavHostController.setString(key: String) {
    previousBackStackEntry?.arguments?.let { prev ->
        currentBackStackEntry?.arguments?.putString(
            key, prev.getString(key)
        )
    }
}
