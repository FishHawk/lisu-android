package com.fishhawk.lisu.ui.gallery

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.widget.BottomSheet
import com.fishhawk.lisu.ui.widget.SheetListItem

internal class GalleryCoverSheet(
    private val drawable: Drawable,
    private val onAction: GalleryActionHandler
) : BottomSheet() {
    @Composable
    override fun Content() {
        GalleryCoverSheetContent(drawable, onAction)
    }
}

@Composable
private fun GalleryCoverSheetContent(
    cover: Drawable,
    onAction: GalleryActionHandler
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SheetListItem(
            icon = LisuIcons.Refresh,
            title = stringResource(R.string.action_edit_cover)
        ) { onAction(GalleryAction.EditCover) }
        SheetListItem(
            icon = LisuIcons.SaveAlt,
            title = stringResource(R.string.action_save_cover)
        ) { onAction(GalleryAction.SaveCover(cover)) }
        SheetListItem(
            icon = LisuIcons.Share,
            title = stringResource(R.string.action_share_cover)
        ) { onAction(GalleryAction.ShareCover(cover)) }
    }
}
