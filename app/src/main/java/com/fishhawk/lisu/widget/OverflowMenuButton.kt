package com.fishhawk.lisu.widget

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import com.fishhawk.lisu.ui.theme.LisuIcons

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OverflowMenuButton(
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = LisuIcons.MoreVert,
                contentDescription = null,
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                content = content,
            )
        }
    }
}