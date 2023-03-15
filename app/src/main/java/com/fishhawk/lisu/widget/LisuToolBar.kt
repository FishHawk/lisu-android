package com.fishhawk.lisu.widget

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.theme.LisuIcons

@Composable
fun LisuToolBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    transparent: Boolean = false,
    onNavUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    LisuToolBar(
        title = {
            title?.let {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        modifier = modifier,
        transparent = transparent,
        onNavUp = onNavUp,
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LisuToolBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    transparent: Boolean = false,
    onNavUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = {
            onNavUp?.let {
                TooltipIconButton(
                    tooltip = stringResource(R.string.action_back),
                    icon = LisuIcons.ArrowBack,
                    onClick = { it() },
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = if (transparent) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    )
}

@Composable
fun LisuNonCenterAlignedToolBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    transparent: Boolean = false,
    onNavUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    LisuNonCenterAlignedToolBar(
        title = {
            title?.let {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        modifier = modifier,
        transparent = transparent,
        onNavUp = onNavUp,
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LisuNonCenterAlignedToolBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    transparent: Boolean = false,
    onNavUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = {
            onNavUp?.let {
                TooltipIconButton(
                    tooltip = stringResource(R.string.action_back),
                    icon = LisuIcons.ArrowBack,
                    onClick = { it() },
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = if (transparent) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    )
}
