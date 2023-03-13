package com.fishhawk.lisu.widget

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BottomSheetListItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = { onClick() }),
        leadingContent = { Icon(imageVector = icon, contentDescription = title) },
        headlineContent = { Text(text = title) }
    )
}