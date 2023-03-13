package com.fishhawk.lisu.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.getBlocking
import com.fishhawk.lisu.data.network.model.GitHubRelease
import com.fishhawk.lisu.notification.AppUpdateNotification
import com.fishhawk.lisu.ui.base.BaseActivity
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.ui.download.DownloadScreen
import com.fishhawk.lisu.ui.explore.ExploreScreen
import com.fishhawk.lisu.ui.explore.LoginCookiesScreen
import com.fishhawk.lisu.ui.explore.LoginPasswordScreen
import com.fishhawk.lisu.ui.explore.LoginWebsiteScreen
import com.fishhawk.lisu.ui.gallery.GalleryCommentScreen
import com.fishhawk.lisu.ui.gallery.GalleryEditScreen
import com.fishhawk.lisu.ui.gallery.GalleryScreen
import com.fishhawk.lisu.ui.globalsearch.GlobalSearchScreen
import com.fishhawk.lisu.ui.history.HistoryScreen
import com.fishhawk.lisu.ui.library.LibraryScreen
import com.fishhawk.lisu.ui.more.*
import com.fishhawk.lisu.ui.provider.ProviderScreen
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.fishhawk.lisu.util.findActivity
import com.fishhawk.lisu.util.toUriCompat
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.LisuDialog
import com.fishhawk.lisu.widget.LisuScaffold
import org.koin.androidx.compose.koinViewModel

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp(
    viewModel: MainViewModel = koinViewModel(),
) {

    var latestRelease by remember { mutableStateOf<GitHubRelease?>(null) }

    val context = LocalContext.current

    OnEvent(viewModel.event) {
        when (it) {
            MainEvent.NoNewUpdates -> context.toast(R.string.update_check_no_new_updates)
            is MainEvent.CheckUpdateFailure -> context.toast(it.exception)
            is MainEvent.ShowUpdateDialog -> latestRelease = it.release
            MainEvent.AlreadyDownloading -> context.toast("Already downloading apk.")
            MainEvent.NotifyDownloadStart ->
                AppUpdateNotification.onDownloadStart(context)

            is MainEvent.NotifyDownloadProgress ->
                AppUpdateNotification.onProgressChange(context, it.progress)

            is MainEvent.NotifyDownloadFinish ->
                AppUpdateNotification.onDownloadFinished(
                    context,
                    it.file.toUriCompat(context)
                )

            is MainEvent.NotifyDownloadError ->
                AppUpdateNotification.onDownloadError(context, it.url)
        }
    }

    LaunchedEffect(Unit) {
        AppUpdateNotification.retryFlow.collect {
            viewModel.downloadApk(context.externalCacheDir, it)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val autoCheckUpdate by PR.enableAutoUpdates.collectAsState()
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (autoCheckUpdate) {
                    viewModel.checkForUpdate()
                }
            }
        })
    }

    LisuTheme {
        val navController = rememberNavController()
        LisuScaffold(
            bottomBar = { BottomBar(navController) },
            contentWindowInsets = WindowInsets.ime.add(WindowInsets.navigationBars),
        ) { innerPadding ->
            MainNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
        }

        latestRelease?.let {
            LisuDialog(
                title = stringResource(R.string.dialog_new_version_available),
                confirmText = stringResource(R.string.dialog_new_version_available_download),
                dismissText = stringResource(R.string.dialog_new_version_available_ignore),
                onConfirm = {
                    viewModel.downloadApk(context.externalCacheDir, it.getDownloadLink())
                },
                onDismiss = { latestRelease = null },
                text = it.body,
            )
        }

        var exitPressedOnce by remember { mutableStateOf(false) }
        val isConfirmExitEnabled by PR.isConfirmExitEnabled.collectAsState()
        BackHandler(
            navController.backQueue.size <= 1
                    && isConfirmExitEnabled
        ) {
            if (exitPressedOnce) {
                context.findActivity().finish()
            } else {
                exitPressedOnce = true
                context.toast(R.string.confirm_exit)
                Handler(Looper.getMainLooper()).postDelayed({
                    exitPressedOnce = false
                }, 2000)
            }
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier,
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

        composable("provider/{providerId}/login-website") { LoginWebsiteScreen(navController) }
        composable("provider/{providerId}/login-cookies") { LoginCookiesScreen(navController) }
        composable("provider/{providerId}/login-password") { LoginPasswordScreen(navController) }

        composable("global-search?keywords={keywords}", listOf(
            navArgument("keywords") { nullable = true }
        )) { GlobalSearchScreen(navController) }

        composable("provider/{providerId}/board/{boardId}?keywords={keywords}", listOf(
            navArgument("keywords") { nullable = true }
        )) { ProviderScreen(navController) }

        composable("gallery/{mangaId}/detail?manga={manga}", listOf(
            navArgument("manga") {
                nullable = true
                type = MangaNavType
            }
        )) { GalleryScreen(navController) }

        composable("edit") { GalleryEditScreen(navController) }

        composable("comment") { GalleryCommentScreen(navController) }

        composable("download") { DownloadScreen(navController) }

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
    val bottomBarVisible = Tab.items.any { it.route == currentDestination?.route }

    AnimatedVisibility(
        visible = bottomBarVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Surface(shadowElevation = 16.dp) {
            NavigationBar {
                Tab.items.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == tab.route
                    } ?: false
                    BottomBarTab(
                        tab = tab,
                        selected = selected,
                        navController = navController,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarTab(
    tab: Tab,
    selected: Boolean,
    navController: NavHostController,
) {
    NavigationBarItem(
        icon = { Icon(tab.icon, contentDescription = tab.route) },
        label = { Text(stringResource(tab.labelResId)) },
        selected = selected,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.surface,
        ),
        onClick = {
            if (tab.route == navController.currentDestination?.route) return@NavigationBarItem
            navController.navigate(tab.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}