package com.fishhawk.lisu.ui.reader

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.theme.LisuIcons

@Composable
internal fun ReaderPageSheet(
    drawable: Drawable,
    position: Int,
    onAction: ReaderActionHandler
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SheetListItem(
            icon = LisuIcons.Refresh,
            title = stringResource(R.string.page_action_refresh)
        ) { }
        SheetListItem(
            icon = LisuIcons.SaveAlt,
            title = stringResource(R.string.page_action_save)
        ) { onAction(ReaderAction.SavePage(drawable, position)) }
        SheetListItem(
            icon = LisuIcons.Share,
            title = stringResource(R.string.page_action_share)
        ) { onAction(ReaderAction.SharePage(drawable, position)) }
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