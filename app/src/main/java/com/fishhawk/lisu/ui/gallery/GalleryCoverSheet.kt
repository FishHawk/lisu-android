package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.ui.theme.LisuIcons

@Composable
internal fun GalleryCoverSheet(onAction: GalleryActionHandler) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextButton(onClick = { onAction(GalleryAction.EditCover) }) {
            Row {
                Icon(LisuIcons.Edit, "edit cover")
                Text("Edit cover")
            }
        }
        TextButton(onClick = { onAction(GalleryAction.SaveCover) }) {
            Row {
                Icon(LisuIcons.SaveAlt, "save cover")
                Text("Save cover")
            }
        }
        TextButton(onClick = { onAction(GalleryAction.ShareCover) }) {
            Row {
                Icon(LisuIcons.Share, "share cover")
                Text("Share cover")
            }
        }
    }
}
