package com.fishhawk.lisu.ui.widget

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.R
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

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
                Icon(Icons.Filled.ArrowBack, stringResource(R.string.action_back))
            }
        }
    },
    actions = {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) { actions() }
    },
    backgroundColor = if (transparent) Color.Transparent else MaterialTheme.colors.surface,
    elevation = if (transparent) 0.dp else AppBarDefaults.TopAppBarElevation,
)