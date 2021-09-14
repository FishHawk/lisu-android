package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fishhawk.driftinglibraryandroid.R

@Preview
@Composable
internal fun ReaderPageSheet() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SheetListItem(
            icon = Icons.Filled.Refresh,
            title = stringResource(R.string.page_action_refresh)
        ) { }
        SheetListItem(
            icon = Icons.Default.SaveAlt,
            title = stringResource(R.string.page_action_save)
        ) { }
        SheetListItem(
            icon = Icons.Default.Share,
            title = stringResource(R.string.page_action_share)
        ) { }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SheetListItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = { onClick() }),
        icon = { Icon(icon, contentDescription = "") },
        text = { Text(text = title) }
    )
}