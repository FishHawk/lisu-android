package com.fishhawk.lisu.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.Theme
import com.fishhawk.lisu.data.datastore.collectAsState
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val LisuIcons = Icons.Outlined

@Composable
fun LisuTheme(content: @Composable () -> Unit) {
    val theme by PR.theme.collectAsState()

    CompositionLocalProvider(LocalElevationOverlay provides LisuElevationOverlay) {
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

@Composable
fun LisuTransition(
    content: @Composable AnimatedVisibilityScope.() -> Unit
) = AnimatedVisibility(
    visibleState = remember {
        MutableTransitionState(false)
    }.apply { targetState = true },
    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    exit = fadeOut(),
    content = content
)

@Composable
fun LisuToolBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    transparent: Boolean = false,
    onNavUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) = TopAppBar(
    title = {
        title?.let {
            Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    },
    modifier = modifier,
    contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
    navigationIcon = onNavUp?.let {
        {
            IconButton(onClick = { it() }) {
                Icon(Icons.Filled.ArrowBack, "back")
            }
        }
    },
    actions = {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) { actions() }
    },
    backgroundColor = if (transparent) Color.Transparent else MaterialTheme.colors.surface,
    elevation = if (transparent) 0.dp else AppBarDefaults.TopAppBarElevation,
)