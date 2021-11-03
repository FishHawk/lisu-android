package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.OriginalSize
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuToolBar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull

internal val MangaHeaderHeight = 250.dp

@OptIn(ExperimentalFoundationApi::class, coil.annotation.ExperimentalCoilApi::class)
@Composable
internal fun MangaHeader(
    detail: MangaDetailDto,
    onAction: GalleryActionHandler
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MangaHeaderHeight)
    ) {
        val context = LocalContext.current
        var loadedCover by remember { mutableStateOf(detail.cover) }
        LaunchedEffect(detail) {
            // Prevent the image flickering when the cover changes
            snapshotFlow { detail.cover }
                .filterNotNull()
                .collect {
                    val request = ImageRequest.Builder(context)
                        .data(it)
                        .size(OriginalSize)
                        .listener(onSuccess = { _, _ -> loadedCover = it })
                        .build()
                    context.imageLoader.enqueue(request)
                }
        }

        val painter = rememberImagePainter(loadedCover) {
            size(OriginalSize)
            crossfade(true)
            crossfade(500)
        }
        Image(
            modifier = Modifier.matchParentSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colors.background
                        ),
                    )
                )
        )

        Column {
            LisuToolBar(
                onNavUp = { onAction(GalleryAction.NavUp) },
                transparent = true,
            ) {
                if (detail.inLibrary) {
                    IconButton(onClick = { onAction(GalleryAction.NavToEdit) }) {
                        Icon(LisuIcons.Edit, contentDescription = "edit")
                    }
                }
                IconButton(onClick = { onAction(GalleryAction.Share) }) {
                    Icon(LisuIcons.Share, contentDescription = "share")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.aspectRatio(0.75f).let { modifier ->
                        (painter.state as? ImagePainter.State.Success)?.result?.drawable?.let {
                            modifier.combinedClickable(
                                onClick = { onAction(GalleryAction.SaveCover(it)) },
                                onLongClick = { onAction(GalleryAction.ShareCover(it)) }
                            )
                        } ?: modifier
                    },
                    shape = RoundedCornerShape(4.dp),
                    elevation = 4.dp,
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                MangaInfo(
                    providerId = detail.providerId,
                    title = (detail.title ?: detail.id),
                    author = detail.authors?.joinToString(separator = ";"),
                    isFinished = detail.isFinished,
                    onTitleClick = { onAction(GalleryAction.NavToGlobalSearch(it)) },
                    onAuthorClick = { onAction(GalleryAction.NavToGlobalSearch(it)) },
                    onTitleLongClick = { onAction(GalleryAction.Copy(it, R.string.title_copied)) },
                    onAuthorLongClick = { onAction(GalleryAction.Copy(it, R.string.author_copied)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MangaInfo(
    providerId: String,
    title: String,
    author: String?,
    isFinished: Boolean?,
    onTitleClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onTitleLongClick: (String) -> Unit,
    onAuthorLongClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        title.let {
            Box(modifier = Modifier.weight(1f)) {
                MangaInfoTitle(
                    text = it,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .combinedClickable(
                            onClick = { onTitleClick(it) },
                            onLongClick = { onTitleLongClick(it) }
                        )
                )
            }
        }
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            author?.let {
                MangaInfoSubtitle(
                    text = it,
                    modifier = Modifier.combinedClickable(
                        onClick = { onAuthorClick(it) },
                        onLongClick = { onAuthorLongClick(it) }
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                isFinished?.let { MangaInfoSubtitle(text = if (it) "Finished" else "Ongoing") }
                MangaInfoSubtitle(text = providerId)
            }
        }
    }
}

@Composable
private fun MangaInfoTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val defaultTextStyle = MaterialTheme.typography.h6
    var textStyle by remember { mutableStateOf(defaultTextStyle) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = textStyle,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight && textStyle.fontSize > 12.sp) {
                readyToDraw = false
                textStyle = textStyle.copy(fontSize = textStyle.fontSize.times(0.9))
            } else {
                readyToDraw = true
            }
        },
        overflow = TextOverflow.Ellipsis
    )
}


@Composable
private fun MangaInfoSubtitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.body2,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
