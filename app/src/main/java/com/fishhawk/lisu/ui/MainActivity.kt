package com.fishhawk.lisu.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.StartScreen
import com.fishhawk.lisu.data.datastore.getBlocking
import com.fishhawk.lisu.ui.base.BaseActivity
import com.fishhawk.lisu.ui.explore.ExploreScreen
import com.fishhawk.lisu.ui.gallery.GalleryEditScreen
import com.fishhawk.lisu.ui.gallery.GalleryScreen
import com.fishhawk.lisu.ui.globalsearch.GlobalSearchScreen
import com.fishhawk.lisu.ui.history.HistoryScreen
import com.fishhawk.lisu.ui.library.LibraryScreen
import com.fishhawk.lisu.ui.library.LibrarySearchScreen
import com.fishhawk.lisu.ui.more.*
import com.fishhawk.lisu.ui.provider.ProviderScreen
import com.fishhawk.lisu.ui.provider.ProviderSearchScreen
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.ui.BottomNavigation
import com.google.accompanist.insets.ui.Scaffold
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainApp() }
    }
}

@Composable
private fun MainApp() {
    LisuTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.navigationBarsPadding(),
            bottomBar = { BottomBar(navController) }
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
            StartScreen.Library -> Tab.Library
            StartScreen.History -> Tab.History
            StartScreen.Explore -> Tab.Explore
        }.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Tab.Library.route) { LibraryScreen(navController) }
        composable(Tab.History.route) { HistoryScreen(navController) }
        composable(Tab.Explore.route) { ExploreScreen(navController) }
        composable(Tab.More.route) { MoreScreen(navController) }

        composable("global-search?keywords={keywords}", listOf(
            navArgument("keywords") { nullable = true }
        )) { GlobalSearchScreen(navController) }

        composable(
            "library/search",
            listOf(navArgument("keywords") { nullable = true })
        ) { LibrarySearchScreen(navController) }

        composable("provider/{providerId}") { ProviderScreen(navController) }

        composable("provider/{providerId}/search", listOf(
            navArgument("keywords") { nullable = true }
        )) { ProviderSearchScreen(navController) }

        composable(
            "gallery/{mangaId}/detail?manga={manga}",
            listOf(navArgument("manga") {
                nullable = true
                type = MangaNavType
            })
        ) { GalleryScreen(navController) }

        composable("edit") { GalleryEditScreen(navController) }

        composable("setting-general") { SettingGeneralScreen(navController) }
        composable("setting-reader") { SettingReaderScreen(navController) }
        composable("setting-advanced") { SettingAdvancedScreen(navController) }
        composable("about") { AboutScreen(navController) }
        composable("open-source-license") { OpenSourceLicenseScreen(navController) }
    }
}

sealed class Tab(val route: String, val labelResId: Int, val icon: ImageVector) {
    object Library : Tab("library", R.string.label_library, Icons.Filled.CollectionsBookmark)
    object History : Tab("history", R.string.label_history, Icons.Filled.History)
    object Explore : Tab("explore", R.string.label_explore, Icons.Filled.Explore)
    object More : Tab("more", R.string.label_more, Icons.Filled.MoreHoriz)

    companion object {
        val items = listOf(Library, History, Explore, More)
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    AnimatedVisibility(
        visible = (Tab.items.any { it.route == currentDestination?.route }),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
            Tab.items.forEach { tab -> BottomBarTab(tab, currentDestination, navController) }
        }
    }
}

@Composable
private fun RowScope.BottomBarTab(
    tab: Tab,
    currentDestination: NavDestination?,
    navController: NavHostController
) = BottomNavigationItem(
    icon = { Icon(tab.icon, contentDescription = tab.route) },
    label = { Text(stringResource(tab.labelResId)) },
    selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
    selectedContentColor = MaterialTheme.colors.primary,
    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
    onClick = {
        if (tab.route == navController.currentDestination?.route) return@BottomNavigationItem
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
)