package com.fishhawk.lisu.ui.reader

import android.graphics.Bitmap
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
    private val bitmap: Bitmap,
    private val position: Int,
    private val onAction: (ReaderAction) -> Unit,
) : BottomSheet() {
    @Composable
    override fun Content() {
        ReaderPageSheetContent(bitmap, position, onAction)
        BackHandler()
    }
}

@Composable
private fun ReaderPageSheetContent(
    bitmap: Bitmap,
    position: Int,
    onAction: (ReaderAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SheetListItem(
            icon = LisuIcons.Image,
            title = stringResource(R.string.action_set_as_cover)
        ) { onAction(ReaderAction.SetAsImage(bitmap)) }
        SheetListItem(
            icon = LisuIcons.SaveAlt,
            title = stringResource(R.string.action_save_image)
        ) { onAction(ReaderAction.SavePage(bitmap, position)) }
        SheetListItem(
            icon = LisuIcons.Share,
            title = stringResource(R.string.action_share_image)
        ) { onAction(ReaderAction.SharePage(bitmap, position)) }
    }
}