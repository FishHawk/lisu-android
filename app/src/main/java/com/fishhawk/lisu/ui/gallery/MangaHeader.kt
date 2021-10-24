package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.google.accompanist.insets.statusBarsPadding

internal val MangaHeaderHeight = 220.dp

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