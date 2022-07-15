package com.fishhawk.lisu.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.getBlocking
import com.fishhawk.lisu.data.network.model.GitHubReleaseDto
import com.fishhawk.lisu.notification.AppUpdateNotification
import com.fishhawk.lisu.ui.base.BaseActivity
import com.fishhawk.lisu.ui.explore.ExploreScreen
import com.fishhawk.lisu.ui.gallery.GalleryCommentScreen
import com.fishhawk.lisu.ui.gallery.GalleryEditScreen
import com.fishhawk.lisu.ui.gallery.GalleryScreen
import com.fishhawk.lisu.ui.globalsearch.GlobalSearchScreen
import com.fishhawk.lisu.ui.history.HistoryScreen
import com.fishhawk.lisu.ui.library.LibraryScreen
import com.fishhawk.lisu.ui.more.*
import com.fishhawk.lisu.ui.provider.ProviderLoginScreen
import com.fishhawk.lisu.ui.provider.ProviderScreen
import com.fishhawk.lisu.ui.provider.ProviderSearchScreen
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.fishhawk.lisu.widget.LisuModalBottomSheetLayout
import com.fishhawk.lisu.util.findActivity
import com.fishhawk.lisu.util.toUriCompat
import com.fishhawk.lisu.util.toast
import com.google.accompanist.insets.ui.BottomNavigation
import com.google.accompanist.insets.ui.Scaffold
import org.koin.androidx.compose.viewModel
import java.io.File

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainApp() }
    }
}

@Composable
private fun MainApp() {
    val viewModel by viewModel<MainViewModel>()

    var latestRelease by remember { mutableStateOf<GitHubReleaseDto?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { effect ->
            when (effect) {
                is MainEffect.Message -> context.toast(context.getString(effect.messageId))
                is MainEffect.StringMessage -> context.toast(effect.message)
                is MainEffect.ShowUpdateDialog -> latestRelease = effect.release
                MainEffect.NotifyDownloadStart ->
                    AppUpdateNotification.onDownloadStart(context)
                is MainEffect.NotifyDownloadProgress ->
                    AppUpdateNotification.onProgressChange(context, effect.progress)
                is MainEffect.NotifyDownloadFinish ->
                    AppUpdateNotification.onDownloadFinished(
                        context,
                        effect.file.toUriCompat(context)
                    )
                is MainEffect.NotifyDownloadError ->
                    AppUpdateNotification.onDownloadError(context, effect.url)
            }
        }
    }

    LaunchedEffect(Unit) {
        AppUpdateNotification.retryFlow.collect {
            viewModel.downloadApk(File(context.externalCacheDir, "update.apk"), it)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                viewModel.checkForUpdate()
            }
        })
    }

    LisuTheme {
        LisuModalBottomSheetLayout {
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

            latestRelease?.let {
                NewVersionAvailableDialog(
                    it,
                    onDismiss = { latestRelease = null },
                    onConfirm = {
                        viewModel.downloadApk(
                            File(context.externalCacheDir, "update.apk"),
                            it.getDownloadLink()
                        )
                    }
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

        composable("provider/{providerId}/board/{boardId}") { ProviderScreen(navController) }
        composable("provider/{providerId}/login?provider={provider}", listOf(
            navArgument("provider") {
                nullable = true
                type = ProviderNavType
            }
        )) { ProviderLoginScreen(navController) }
        composable("provider/{providerId}/search?keywords={keywords}", listOf(
            navArgument("keywords") { nullable = true }
        )) { ProviderSearchScreen(navController) }

        composable("gallery/{mangaId}/detail?manga={manga}", listOf(
            navArgument("manga") {
                nullable = true
                type = MangaNavType
            }
        )) { GalleryScreen(navController) }

        composable("edit") { GalleryEditScreen(navController) }

        composable("comment") { GalleryCommentScreen(navController) }

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

@Composable
fun NewVersionAvailableDialog(
    release: GitHubReleaseDto,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = stringResource(R.string.dialog_new_version_available)) },
        text = { Text(text = release.info, modifier = Modifier.heightIn(max = 400.dp)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(stringResource(R.string.dialog_new_version_available_download))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.dialog_new_version_available_ignore))
            }
        }
    )
}