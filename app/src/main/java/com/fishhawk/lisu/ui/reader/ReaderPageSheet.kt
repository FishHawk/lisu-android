package com.fishhawk.lisu.ui.reader

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.widget.BottomSheet
import com.fishhawk.lisu.widget.SheetListItem

internal class ReaderPageSheet(
    private val drawable: Drawable,
    private val position: Int,
    private val onAction: ReaderActionHandler
) : BottomSheet() {
    @Composable
    override fun Content() {
        ReaderPageSheetContent(drawable, position, onAction)
        BackHandler()
    }
}

@Composable
private fun ReaderPageSheetContent(
    drawable: Drawable,
    position: Int,
    onAction: ReaderActionHandler
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SheetListItem(
            icon = LisuIcons.Image,
            title = stringResource(R.string.action_set_as_cover)
        ) { onAction(ReaderAction.SetAsImage(drawable)) }
        SheetListItem(
            icon = LisuIcons.SaveAlt,
            title = stringResource(R.string.action_save_image)
        ) { onAction(ReaderAction.SavePage(drawable, position)) }
        SheetListItem(
            icon = LisuIcons.Share,
            title = stringResource(R.string.action_share_image)
        ) { onAction(ReaderAction.SharePage(drawable, position)) }
    }
}