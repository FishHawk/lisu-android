package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.network.model.MangaDetailDto
import com.fishhawk.lisu.data.network.model.MangaState
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.widget.LisuToolBar
import com.fishhawk.lisu.widget.LocalBottomSheetHelper
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal val MangaHeaderHeight = 290.dp

@Composable
internal fun MangaHeader(
    detail: MangaDetailDto,
    history: ReadingHistory?,
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
                        .size(Size.ORIGINAL)
                        .listener(onSuccess = { _, _ -> loadedCover = it })
                        .build()
                    context.imageLoader.enqueue(request)
                }
        }

        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(loadedCover)
                .crossfade(true)
                .crossfade(500)
                .build()
        )
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
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
                if (detail.state != MangaState.Local) {
                    IconButton(onClick = { onAction(GalleryAction.NavToComment) }) {
                        Icon(
                            LisuIcons.Comment,
                            "Comment"
                        )
                    }
                }
                IconButton(onClick = { onAction(GalleryAction.Share) }) {
                    Icon(LisuIcons.Share, stringResource(R.string.action_share_manga))
                }
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val scope = rememberCoroutineScope()
                val bottomSheetHelper = LocalBottomSheetHelper.current
                val drawable = (painter.state as? AsyncImagePainter.State.Success)?.result?.drawable
                Surface(
                    modifier = Modifier
                        .aspectRatio(0.75f)
                        .clickable {
                            val sheet = GalleryCoverSheet(drawable, onAction)
                            scope.launch { bottomSheetHelper.open(sheet) }
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
                    author = detail.authors.joinToString(separator = ";"),
                    isFinished = detail.isFinished,
                    onTitleClick = { onAction(GalleryAction.NavToGlobalSearch(it)) },
                    onAuthorClick = { onAction(GalleryAction.NavToGlobalSearch(it)) },
                    onTitleLongClick = { onAction(GalleryAction.Copy(it, R.string.title_copied)) },
                    onAuthorLongClick = { onAction(GalleryAction.Copy(it, R.string.author_copied)) }
                )
            }

            MangaActionButtons(detail, history, onAction)
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

@Composable
private fun MangaActionButtons(
    detail: MangaDetailDto,
    history: ReadingHistory?,
    onAction: GalleryActionHandler
) {
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        when (detail.state) {
            MangaState.Local -> {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    MangaActionButton(
                        icon = LisuIcons.Edit,
                        text = "Edit metadata"
                    ) { onAction(GalleryAction.NavToEdit) }
                }
            }
            MangaState.Remote -> {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    MangaActionButton(
                        icon = LisuIcons.FavoriteBorder,
                        text = "Add to library"
                    ) { onAction(GalleryAction.AddToLibrary) }
                }
            }
            MangaState.RemoteInLibrary -> {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.primary) {
                    MangaActionButton(
                        icon = LisuIcons.Favorite,
                        text = "In library"
                    ) { onAction(GalleryAction.RemoveFromLibrary) }
                }
            }
        }
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            MangaActionButton(
                icon = LisuIcons.AutoStories,
                text = if (history == null) "Read" else "Continue"
            ) {
                onAction(
                    GalleryAction.NavToReader(
                        collectionId = history?.collectionId
                            ?: detail.collections.keys.first(),
                        chapterId = history?.chapterId
                            ?: detail.collections.values.first().first().id,
                        page = history?.page ?: 0
                    )
                )
            }
        }
    }
}

@Composable
private fun RowScope.MangaActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            modifier = Modifier.size(24.dp),
            contentDescription = text
        )
        Text(text, style = MaterialTheme.typography.caption)
    }
}
