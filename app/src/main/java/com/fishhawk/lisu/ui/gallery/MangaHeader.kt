package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.fishhawk.lisu.data.network.model.MangaState
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.widget.LisuToolBar
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MangaHeader(
    state: MangaState,
    providerId: String,
    cover: String?,
    title: String,
    authors: List<String>,
    isFinished: Boolean?,
    history: ReadingHistory?,
    onAction: (GalleryAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
    ) {
        val context = LocalContext.current
        var loadedCover by remember { mutableStateOf(cover) }
        LaunchedEffect(cover) {
            // Prevent the image flickering when the cover changes
            snapshotFlow { cover }
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
                            MaterialTheme.colorScheme.background
                        ),
                    )
                )
        )
        Column {
            LisuToolBar(
                onNavUp = { onAction(GalleryAction.NavUp) },
                transparent = true,
            ) {
                if (state != MangaState.Local) {
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val drawable = (painter.state as? AsyncImagePainter.State.Success)?.result?.drawable

                var openBottomSheet by rememberSaveable { mutableStateOf(false) }
                val bottomSheetState = rememberModalBottomSheetState()
                if (openBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { openBottomSheet = false },
                        sheetState = bottomSheetState,
                        dragHandle = {},
                    ) {
                        GalleryCoverSheetContent(cover = drawable, onAction = onAction)
                    }
                }

                Surface(
                    modifier = Modifier
                        .aspectRatio(0.75f)
                        .clickable { openBottomSheet = true },
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
                MangaInfo(
                    providerId = providerId,
                    title = title,
                    authors = authors,
                    isFinished = isFinished,
                    onAction = onAction,
                )
            }
            MangaActionButtons(state, history, onAction)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MangaInfo(
    providerId: String,
    title: String,
    authors: List<String>,
    isFinished: Boolean?,
    onAction: (GalleryAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            MangaInfoTitle(
                text = title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .combinedClickable(
                        onClick = { onAction(GalleryAction.NavToGlobalSearch(title)) },
                        onLongClick = { onAction(GalleryAction.Copy(title, R.string.title_copied)) }
                    ),
            )
        }
        MediumEmphasis {
            ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                if (authors.isNotEmpty()) {
                    val authorsText = authors.joinToString(separator = ";")
                    Text(
                        text = authorsText,
                        modifier = Modifier.combinedClickable(
                            onClick = { onAction(GalleryAction.NavToGlobalSearch(authorsText)) },
                            onLongClick = {
                                onAction(GalleryAction.Copy(authorsText, R.string.author_copied))
                            },
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    isFinished?.let {
                        Text(
                            text = if (it) "Finished" else "Ongoing",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = providerId,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun MangaInfoTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    val defaultTextStyle = MaterialTheme.typography.titleLarge
    var textStyle by remember { mutableStateOf(defaultTextStyle) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight && textStyle.fontSize > 12.sp) {
                readyToDraw = false
                textStyle = textStyle.copy(
                    fontSize = textStyle.fontSize.times(0.9),
                    lineHeight = textStyle.fontSize.times(0.9) / 4 * 5,
                )
            } else {
                readyToDraw = true
            }
        },
        overflow = TextOverflow.Ellipsis,
        style = textStyle,
    )
}

@Composable
private fun MangaActionButtons(
    state: MangaState,
    history: ReadingHistory?,
    onAction: (GalleryAction) -> Unit,
) {
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        when (state) {
            MangaState.Local -> {
                MediumEmphasis {
                    MangaActionButton(
                        icon = LisuIcons.Edit,
                        text = "Edit metadata"
                    ) { onAction(GalleryAction.NavToEdit) }
                }
            }

            MangaState.Remote -> {
                MediumEmphasis {
                    MangaActionButton(
                        icon = LisuIcons.FavoriteBorder,
                        text = "Add to library"
                    ) { onAction(GalleryAction.AddToLibrary) }
                }
            }

            MangaState.RemoteInLibrary -> {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                    MangaActionButton(
                        icon = LisuIcons.Favorite,
                        text = "In library"
                    ) { onAction(GalleryAction.RemoveFromLibrary) }
                }
            }
        }
        MediumEmphasis {
            MangaActionButton(
                icon = LisuIcons.AutoStories,
                text = if (history == null) "Read" else "Continue"
            ) { onAction(GalleryAction.Continue) }
        }
    }
}

@Composable
private fun RowScope.MangaActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            modifier = Modifier.size(24.dp),
            contentDescription = text,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
