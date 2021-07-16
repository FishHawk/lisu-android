package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.fishhawk.driftinglibraryandroid.data.remote.model.*
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.fishhawk.driftinglibraryandroid.ui.theme.MaterialColors
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun GalleryScreen(navController: NavHostController) {
    navController.previousBackStackEntry!!.arguments!!.getParcelable<MangaOutline>("outline").let {
        navController.currentBackStackEntry!!.arguments!!.putParcelable("outline", it)
    }
    navController.previousBackStackEntry!!.arguments!!.getParcelable<ProviderInfo>("provider").let {
        navController.currentBackStackEntry!!.arguments!!.putParcelable("provider", it)
    }

    val viewModel = hiltViewModel<GalleryViewModel>()
    val detail by viewModel.detail.observeAsState()

    fun search(keywords: String) {
        navController.currentBackStackEntry?.arguments =
            bundleOf(
                "keywords" to keywords,
                "provider" to viewModel.provider
            )
        if (viewModel.isFromProvider) navController.navigate("search/${viewModel.provider!!.id}")
        else navController.navigate("library-search")
    }

    ApplicationTransition {
        Column {
            MangaHeader(navController, detail)
            val isRefreshing by viewModel.isRefreshing.observeAsState(true)
            SwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.refreshManga() },
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    detail?.source?.let { MangaSource(it) }
                    detail?.metadata?.description?.let { MangaDescription(it) }
                    detail?.metadata?.tags?.let { tags ->
                        val context = LocalContext.current
                        MangaTagGroups(tags,
                            onTagClick = { search(it) },
                            onTagLongClick = {
                                context.copyToClipboard(it, R.string.toast_manga_tag_saved)
                            }
                        )
                    }
                    detail?.let { MangaContent(viewModel, it) }
                }
            }
        }
    }
}

@Composable
private fun MangaHeader(navController: NavHostController, detail: MangaDetail?) {
    val viewModel = hiltViewModel<GalleryViewModel>()
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val context = LocalContext.current
        Image(
            painter = rememberImagePainter(detail?.cover) {
                crossfade(true)
                crossfade(500)
            },
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
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
                                if (viewModel.detail.value != null)
                                    navController.navigate("edit")
                            }

                            override fun onEditCover() {
                                launcher.launch("test")
                            }

                            override fun onSaveCover() {
                                if (detail == null)
                                    return context.toast(R.string.toast_manga_not_loaded)
                                val url = detail.cover
                                    ?: return context.toast(R.string.toast_manga_no_cover)
                                context.saveImage(url, "${detail.id}-cover")
                            }

                            override fun onShareCover() {
                                if (detail == null)
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
                    painter = rememberImagePainter(detail?.cover) {
                        crossfade(true)
                        crossfade(500)
                    },
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
private fun MangaInfo(navController: NavHostController, detail: MangaDetail?) {
    val viewModel = hiltViewModel<GalleryViewModel>()

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
        (detail?.title ?: viewModel.outline.title).let {
            Box(modifier = Modifier.weight(1f)) {
                val defaultTextStyle = MaterialTheme.typography.subtitle1.copy(fontSize = 18.sp)
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
        (detail?.metadata?.authors ?: viewModel.outline.metadata.authors)
            ?.joinToString(separator = ";")?.let {
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
            (detail?.metadata?.status ?: viewModel.outline.metadata.status)?.let {
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.body2
                )
            }
            viewModel.provider?.title?.let {
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

@Composable
private fun MangaSource(source: Source) {
    Text(
        text = "From ${source.providerId} - ${source.mangaId} ${source.state}",
        color = when (source.state) {
            SourceState.DOWNLOADING -> MaterialColors.Blue400
            SourceState.WAITING -> MaterialColors.Green400
            SourceState.ERROR -> MaterialTheme.colors.error
            SourceState.UPDATED -> MaterialTheme.colors.onSurface
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MangaDescription(description: String) {
    val context = LocalContext.current
    Text(
        text = description,
        style = MaterialTheme.typography.body2,
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = {
                context.copyToClipboard(description, R.string.toast_manga_description_copied)
            }
        )
    )
}

@Composable
private fun MangaContent(viewModel: GalleryViewModel, detail: MangaDetail) {
    val context = LocalContext.current
    val hasPreview = detail.collections.size == 1 && !detail.preview.isNullOrEmpty()
    val hasChapter = detail.collections.isNotEmpty()
    when {
        hasPreview -> MangaContentPreview(
            preview = detail.preview!!,
            onPageClick = {
                context.navToReaderActivity(detail, 0, 0, it)
            })
        hasChapter -> {
            val history by viewModel.history.collectAsState()
            MangaContentChapter(
                collections = detail.collections,
                chapterMark = history?.let {
                    ChapterMark(it.collectionIndex, it.chapterIndex, it.pageIndex)
                },
                onChapterClick = { collectionIndex, chapterIndex, pageIndex ->
                    context.navToReaderActivity(detail, collectionIndex, chapterIndex, pageIndex)
                }
            )
        }
        else -> MangaNoChapter()
    }
}
