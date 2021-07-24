package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.ui.base.copyToClipboard
import com.fishhawk.driftinglibraryandroid.ui.base.saveImage
import com.fishhawk.driftinglibraryandroid.ui.base.shareImage
import com.fishhawk.driftinglibraryandroid.ui.base.toast
import com.google.accompanist.insets.statusBarsPadding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun MangaHeader(navController: NavHostController, detail: MangaDetail) {
    val viewModel = hiltViewModel<GalleryViewModel>()
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val context = LocalContext.current
        val painter = rememberImagePainter(detail.cover) {
            crossfade(true)
            crossfade(500)
        }

        Image(
            modifier = Modifier.matchParentSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val newCover = remember { mutableStateOf<Uri?>(null) }
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { newCover.value = it }

            newCover.value?.let {
                val content = context.contentResolver.openInputStream(it)?.readBytes()
                val type = context.contentResolver.getType(it)?.toMediaTypeOrNull()
                if (content != null && type != null)
                    viewModel.updateCover(content.toRequestBody(type))
            }

            Surface(
                modifier = Modifier
                    .aspectRatio(0.75f)
                    .clickable {
                        GalleryCoverSheet(context, object : GalleryCoverSheet.Listener {
                            override fun onSyncSource() {
                                viewModel.syncSource()
                            }

                            override fun onDeleteSource() {
                                viewModel.deleteSource()
                            }

                            override fun onEditMetadata() {
                                navController.navigate("edit")
                            }

                            override fun onEditCover() {
                                if (viewModel.isRefreshing.value)
                                    return context.toast(R.string.toast_manga_not_loaded)
                                launcher.launch("test")
                            }

                            override fun onSaveCover() {
                                if (viewModel.isRefreshing.value)
                                    return context.toast(R.string.toast_manga_not_loaded)
                                val url = detail.cover
                                    ?: return context.toast(R.string.toast_manga_no_cover)
                                context.saveImage(url, "${detail.id}-cover")
                            }

                            override fun onShareCover() {
                                if (viewModel.isRefreshing.value)
                                    return context.toast(R.string.toast_manga_not_loaded)
                                val url = detail.cover
                                    ?: return context.toast(R.string.toast_manga_no_cover)
                                context.shareImage(url, "${detail.id}-cover")
                            }
                        }).show()
                    },
                shape = RoundedCornerShape(4.dp),
                elevation = 4.dp
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            MangaInfo(navController, detail)
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MangaInfo(navController: NavHostController, detail: MangaDetail) {

    fun globalSearch(keywords: String) {
        navController.currentBackStackEntry?.arguments =
            bundleOf("keywords" to keywords)
        navController.navigate("global-search")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val context = LocalContext.current
        detail.title.let {
            Box(modifier = Modifier.weight(1f)) {
                val defaultTextStyle = MaterialTheme.typography.h6
                var textStyle by remember { mutableStateOf(defaultTextStyle) }
                Text(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .combinedClickable(
                            onClick = { globalSearch(it) },
                            onLongClick = {
                                context.copyToClipboard(it, R.string.toast_manga_title_copied)
                            }
                        ),
                    text = it,
                    style = textStyle,
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.didOverflowHeight && textStyle.fontSize > 12.sp) {
                            textStyle = textStyle.copy(fontSize = textStyle.fontSize.times(0.9))
                        }
                    },
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        detail.metadata.authors?.joinToString(separator = ";")?.let {
            Text(
                modifier = Modifier.combinedClickable(
                    onClick = { globalSearch(it) },
                    onLongClick = {
                        context.copyToClipboard(it, R.string.toast_manga_author_copied)
                    }
                ),
                text = it,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            detail.metadata.status?.let {
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.body2
                )
            }
            detail.provider?.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}