package com.fishhawk.lisu.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.Theme
import com.fishhawk.lisu.data.datastore.collectAsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val LisuIcons = Icons.Outlined

@Composable
fun MediumEmphasis(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalContentColor provides mediumEmphasisColor(),
        content = content,
    )
}

@Composable
fun mediumEmphasisColor(): Color {
    return LocalContentColor.current.copy(alpha = 0.70f)
}

@Composable
fun LisuTheme(content: @Composable () -> Unit) {
    val theme by PR.theme.collectAsState()
    val colorScheme = when (theme) {
        Theme.Light -> ColorsLight
        Theme.Dark -> ColorsDark
    }

    MaterialTheme(colorScheme = animateColors(colorScheme)) {
        content()

        val controller = rememberSystemUiController()
        val isLight = theme == Theme.Light
        SideEffect {
            // hack, see https://github.com/google/accompanist/issues/683
            controller.setSystemBarsColor(color = Color.Transparent, darkIcons = true)
            controller.setSystemBarsColor(color = Color.Transparent, darkIcons = isLight)
        }
    }
}

@Composable
fun LisuTransition(
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) = AnimatedVisibility(
    visibleState = remember {
        MutableTransitionState(false)
    }.apply { targetState = true },
    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    exit = fadeOut(),
    content = content
)

@Composable
private fun animateColors(colors: ColorScheme): ColorScheme {
    val animationSpec = remember {
        spring<Color>(stiffness = 500f)
    }

    @Composable
    fun animateColor(color: Color): Color = animateColorAsState(
        targetValue = color,
        animationSpec = animationSpec
    ).value

    return colors.copy(
        primary = animateColor(colors.primary),
        primaryContainer = animateColor(colors.primaryContainer),
        secondary = animateColor(colors.secondary),
        secondaryContainer = animateColor(colors.secondaryContainer),
        background = animateColor(colors.background),
        surface = animateColor(colors.surface),
        surfaceVariant = animateColor(colors.surfaceVariant),
        surfaceTint = animateColor(colors.surfaceTint),
    )
}