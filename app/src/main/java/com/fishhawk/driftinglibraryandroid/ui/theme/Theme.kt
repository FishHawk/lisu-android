package com.fishhawk.driftinglibraryandroid.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ApplicationTheme(content: @Composable () -> Unit) {
    val theme by P.theme.asFlow().collectAsState(P.theme.get())

    MaterialTheme(
        colors = when (theme) {
            P.Theme.LIGHT -> ColorsLight
            P.Theme.DARK -> ColorsDark
        },
        typography = Typography
    ) {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        SideEffect {
            systemUiController.setSystemBarsColor(Color.Transparent, useDarkIcons)
            systemUiController.setNavigationBarColor(Color.Transparent, useDarkIcons)
        }

        ProvideWindowInsets {
            content()
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
    title: String,
    navController: NavHostController? = null,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    actions: @Composable RowScope.() -> Unit = {}
) = TopAppBar(
    title = { Text(title) },
    contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
    navigationIcon = navController?.let {
        {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Filled.NavigateBefore, "back")
            }
        }
    },
    actions = { CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) { actions() } },
    backgroundColor = MaterialTheme.colors.surface,
    elevation = elevation
)