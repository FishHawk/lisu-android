package com.fishhawk.driftinglibraryandroid.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.Theme
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ApplicationTheme(content: @Composable () -> Unit) {
    val theme by PR.theme.collectAsState()

    CompositionLocalProvider(LocalElevationOverlay provides MainElevationOverlay) {
        MaterialTheme(
            colors = when (theme) {
                Theme.Light -> ColorsLight
                Theme.Dark -> ColorsDark
            },
            typography = Typography
        ) {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            SideEffect {
                // hack, see https://github.com/google/accompanist/issues/683
                systemUiController.setStatusBarColor(Color.Transparent, true)
                systemUiController.setStatusBarColor(Color.Transparent, useDarkIcons)
            }

            ProvideWindowInsets {
                content()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ApplicationTransition(
    content: @Composable AnimatedVisibilityScope.() -> Unit
) = AnimatedVisibility(
    visibleState = remember {
        MutableTransitionState(false)
    }.apply { targetState = true },
    enter = fadeIn(animationSpec = spring(stiffness = 100f)),
    exit = fadeOut(),
    content = content
)

@Composable
fun ApplicationToolBar(
    title: String? = null,
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    actions: @Composable RowScope.() -> Unit = {}
) = ApplicationToolBar(
    modifier = modifier,
    title = title,
    elevation = elevation,
    onNavigationIconClick = navController?.let { { it.navigateUp() } },
    actions = actions
)

@Composable
fun ApplicationToolBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onNavigationIconClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) = TopAppBar(
    title = {
        title?.let { Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    },
    modifier = modifier,
    contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
    navigationIcon = onNavigationIconClick?.let {
        { IconButton(onClick = { it() }) { Icon(Icons.Filled.ArrowBack, "back") } }
    },
    actions = { CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) { actions() } },
    backgroundColor = MaterialTheme.colors.surface,
    elevation = elevation
)