package com.fishhawk.lisu.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.Theme
import com.fishhawk.lisu.data.datastore.collectAsState
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
            content()
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