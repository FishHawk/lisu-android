package com.fishhawk.lisu.widget

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.fishhawk.lisu.ui.theme.LisuIcons

@Composable
fun OverflowMenuButton(
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
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