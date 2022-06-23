package com.fishhawk.lisu.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.ReaderMode
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.setNext
import com.fishhawk.lisu.util.findActivity
import com.fishhawk.lisu.ui.reader.viewer.ViewerState
import com.fishhawk.lisu.widget.LocalBottomSheetHelper
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
internal fun BoxScope.ReaderInfoBar(
    chapterName: String,
    chapterTitle: String,
    readerState: ViewerState?
) {
    val infoBarText =
        "$chapterName $chapterTitle" +
                readerState?.let { " ${it.position + 1}/${it.size}" }
    ReaderMenuSurface(
        modifier = Modifier.align(Alignment.BottomEnd)
    ) {
        Text(
            text = infoBarText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White
        )
    }
}

@Composable
internal fun BoxScope.ReaderMenu(
    isOpened: Boolean,
    mangaTitle: String,
    chapterName: String,
    chapterTitle: String,
    isOnlyOneChapter: Boolean,
    viewerState: ViewerState?,
    onAction: ReaderActionHandler
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    LaunchedEffect(isOpened) {
        systemUiController.setStatusBarColor(
            Color.Transparent,
            darkIcons = useDarkIcons && !isOpened
        )
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.TopCenter),
        visible = isOpened,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        ReaderMenuTop(mangaTitle, chapterName, chapterTitle)
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = isOpened,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        ReaderMenuBottom(isOnlyOneChapter, viewerState, onAction)
    }
}


@Composable
private fun ReaderMenuTop(
    mangaTitle: String,
    chapterName: String,
    chapterTitle: String
) {
    ReaderMenuSurface {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            IconButton(onClick = { context.findActivity().finish() }) {
                Icon(Icons.Filled.ArrowBack, "back")
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = mangaTitle,
                    style = MaterialTheme.typography.subtitle1.copy(fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "$chapterName $chapterTitle",
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderMenuBottom(
    isOnlyOneChapter: Boolean,
    viewerState: ViewerState?,
    onAction: ReaderActionHandler
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        val readingDirection by PR.readerMode.collectAsState()
        val layoutDirection =
            if (readingDirection == ReaderMode.Rtl) LayoutDirection.Rtl
            else LayoutDirection.Ltr

        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isOnlyOneChapter)
                    ReaderMenuSurface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        IconButton(onClick = { onAction(ReaderAction.OpenPrevChapter) }) {
                            Icon(
                                if (LocalLayoutDirection.current == LayoutDirection.Ltr)
                                    Icons.Filled.SkipPrevious else Icons.Filled.SkipNext,
                                "prev"
                            )
                        }
                    }

                ReaderMenuSurface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var sizeLabelWidth by remember { mutableStateOf<Int?>(null) }
                        val widthModifier = sizeLabelWidth?.let { width ->
                            with(LocalDensity.current) {
                                Modifier.width(width.toDp())
                            }
                        } ?: Modifier

                        Text(
                            modifier = widthModifier,
                            text = viewerState?.position?.plus(1)?.toString() ?: "-",
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Center
                        )

                        val scope = rememberCoroutineScope()
                        Slider(
                            modifier = Modifier.weight(1f),
                            value = viewerState?.run {
                                position.toFloat() / size.minus(1).coerceAtLeast(1)
                            } ?: 0f,
                            onValueChange = {
                                viewerState?.apply {
                                    val target = (it * (size - 1)).toInt().coerceIn(0, size - 1)
                                    scope.launch { scrollToPage(target) }
                                }
                            },
                            enabled = viewerState != null && viewerState.size > 1
                        )

                        Text(
                            text = viewerState?.size?.toString() ?: "-",
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Center,
                            onTextLayout = { sizeLabelWidth = it.size.width }
                        )
                    }
                }

                if (!isOnlyOneChapter)
                    ReaderMenuSurface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        IconButton(onClick = { onAction(ReaderAction.OpenNextChapter) }) {
                            Icon(
                                if (LocalLayoutDirection.current == LayoutDirection.Ltr)
                                    Icons.Filled.SkipNext else Icons.Filled.SkipPrevious,
                                "next"
                            )
                        }
                    }
            }
        }

        ReaderMenuSurface {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(modifier = Modifier.weight(1f), onClick = {
                    runBlocking { PR.readerMode.setNext() }
                }) {
                    val icon = when (readingDirection) {
                        ReaderMode.Ltr -> Icons.Filled.ArrowForward
                        ReaderMode.Rtl -> Icons.Filled.ArrowBack
                        ReaderMode.Continuous -> Icons.Filled.Expand
                    }
                    Icon(icon, "setting")
                }

                IconButton(modifier = Modifier.weight(1f), onClick = {
                    runBlocking { PR.readerOrientation.setNext() }
                }) { Icon(Icons.Filled.ScreenRotation, null) }

                val bottomSheetHelper = LocalBottomSheetHelper.current
                val scope = rememberCoroutineScope()
                IconButton(modifier = Modifier.weight(1f), onClick = {
                    scope.launch { bottomSheetHelper.open(ReaderSettingsSheet) }
                }) { Icon(Icons.Filled.BrightnessMedium, "color-filter") }

                IconButton(modifier = Modifier.weight(1f), onClick = {
                    scope.launch { bottomSheetHelper.open(ReaderOverlaySheet) }
                }) { Icon(Icons.Filled.Settings, "setting") }
            }
        }
    }
}

@Composable
private fun ReaderMenuSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit
) = Surface(
    modifier = modifier,
    shape = shape,
    color = Color(0xFF333333).copy(alpha = 0.8f),
    contentColor = Color.White,
    content = content
)
