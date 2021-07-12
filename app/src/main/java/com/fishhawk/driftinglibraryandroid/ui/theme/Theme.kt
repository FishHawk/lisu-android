package com.fishhawk.driftinglibraryandroid.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ApplicationTheme(content: @Composable () -> Unit) {
    val theme by GlobalPreference.theme.asFlow().collectAsState(GlobalPreference.theme.get())

    MaterialTheme(
        colors = when (theme) {
            GlobalPreference.Theme.LIGHT -> ColorsLight
            GlobalPreference.Theme.DARK -> ColorsDark
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