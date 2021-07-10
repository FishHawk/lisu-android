package com.fishhawk.driftinglibraryandroid.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference

@Composable
fun ApplicationTheme(
    theme: GlobalPreference.Theme = GlobalPreference.Theme.LIGHT,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = when (theme) {
            GlobalPreference.Theme.LIGHT -> ColorsLight
            GlobalPreference.Theme.DARK -> ColorsDark
        },
        typography = Typography,
        content = content
    )
}