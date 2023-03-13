package com.fishhawk.lisu.ui.gallery

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.BottomSheetListItem

@Composable
internal fun GalleryCoverSheetContent(
    cover: Drawable?,
    onAction: (GalleryAction) -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        BottomSheetListItem(
            icon = LisuIcons.Refresh,
            title = stringResource(R.string.action_edit_cover),
        ) { onAction(GalleryAction.EditCover) }
        BottomSheetListItem(
            icon = LisuIcons.SaveAlt,
            title = stringResource(R.string.action_save_cover),
        ) {
            if (cover == null) context.toast("There is no cover.")
            else onAction(GalleryAction.SaveCover(cover))
        }
        BottomSheetListItem(
            icon = LisuIcons.Share,
            title = stringResource(R.string.action_share_cover),
        ) {
            if (cover == null) context.toast("There is no cover.")
            else onAction(GalleryAction.ShareCover(cover))
        }
    }
}
