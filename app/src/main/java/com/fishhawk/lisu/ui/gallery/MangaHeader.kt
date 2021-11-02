package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuToolBar

internal val MangaHeaderHeight = 250.dp

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
        val painter = rememberImagePainter(detail.cover) {
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
                    modifier = Modifier
                        .aspectRatio(0.75f)
                        .clickable { },
                    shape = RoundedCornerShape(4.dp),
                    elevation = 4.dp
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                MangaInfo(detail, onAction)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MangaInfo(
    detail: MangaDetailDto,
    onAction: GalleryActionHandler
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (detail.title ?: detail.id).let {
            Box(modifier = Modifier.weight(1f)) {
                MangaInfoTitle(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .combinedClickable(
                            onClick = { onAction(GalleryAction.NavToGlobalSearch(it)) },
                            onLongClick = {
                                onAction(GalleryAction.Copy(it, R.string.toast_manga_title_copied))
                            }
                        ),
                    text = it
                )
            }
        }
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            detail.authors?.joinToString(separator = ";")?.let {
                MangaInfoSubtitle(
                    modifier = Modifier.combinedClickable(
                        onClick = { onAction(GalleryAction.NavToGlobalSearch(it)) },
                        onLongClick = {
                            onAction(GalleryAction.Copy(it, R.string.toast_manga_author_copied))
                        }
                    ),
                    text = it
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                detail.isFinished?.let { MangaInfoSubtitle(text = if (it) "Finished" else "Ongoing") }
                MangaInfoSubtitle(text = detail.providerId)
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
