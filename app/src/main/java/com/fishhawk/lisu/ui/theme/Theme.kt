package com.fishhawk.lisu.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.Colors
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
        val colors = when (theme) {
            Theme.Light -> ColorsLight
            Theme.Dark -> ColorsDark
        }
        MaterialTheme(
            colors = animateColors(colors),
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

@Composable
private fun animateColors(colors: Colors): Colors {
    val animationSpec = remember {
        spring<Color>(stiffness = 500f)
    }

    @Composable
    fun animateColor(color: Color): Color = animateColorAsState(
        targetValue = color,
        animationSpec = animationSpec
    ).value

    return Colors(
        primary = animateColor(colors.primary),
        primaryVariant = animateColor(colors.primaryVariant),
        secondary = animateColor(colors.secondary),
        secondaryVariant = animateColor(colors.secondaryVariant),
        background = animateColor(colors.background),
        surface = animateColor(colors.surface),
        error = animateColor(colors.error),
        onPrimary = animateColor(colors.onPrimary),
        onSecondary = animateColor(colors.onSecondary),
        onBackground = animateColor(colors.onBackground),
        onSurface = animateColor(colors.onSurface),
        onError = animateColor(colors.onError),
        isLight = colors.isLight,
    )
}