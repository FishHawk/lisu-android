package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderMode
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.data.datastore.setNext
import com.fishhawk.driftinglibraryandroid.ui.base.findActivity
import com.fishhawk.driftinglibraryandroid.ui.reader.viewer.ViewerState
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@Composable
internal fun BoxScope.ReaderInfoBar(
    name: String,
    title: String,
    readerState: ViewerState?
) {
    val viewModel = viewModel<ReaderViewModel>()
    val isMenuOpened by viewModel.isMenuOpened.collectAsState()

    val showInfoBar by PR.showInfoBar.collectAsState()
    if (!showInfoBar || isMenuOpened) return

    val infoBarText =
        if (readerState != null) "$name $title ${readerState.position + 1}/${readerState.size}"
        else "$name $title"

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .background(Color(0xAA000000))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = infoBarText, color = Color.White)
    }
}

@Composable
internal fun BoxScope.ReaderMenu(
    name: String,
    title: String,
    viewerState: ViewerState?,
    onAction: ReaderActionHandler
) {
    val viewModel = viewModel<ReaderViewModel>()
    val isMenuOpened by viewModel.isMenuOpened.collectAsState()

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    LaunchedEffect(isMenuOpened) {
        systemUiController.setStatusBarColor(
            Color.Transparent,
            darkIcons = useDarkIcons && !isMenuOpened
        )
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.TopCenter),
        visible = isMenuOpened,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        ReaderMenuTop(name, title)
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = isMenuOpened,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        ReaderMenuBottom(viewerState, onAction)
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

@Composable
private fun ReaderMenuTop(name: String, title: String) {
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
                val viewModel = viewModel<ReaderViewModel>()
                val mangaTitle by viewModel.mangaTitle.collectAsState()
                Text(
                    text = mangaTitle,
                    style = MaterialTheme.typography.subtitle1.copy(fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "$name $title",
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
    viewerState: ViewerState?,
    onAction: ReaderActionHandler
) {
    Column {
        val viewModel = viewModel<ReaderViewModel>()

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
                val isOnlyOneChapter by viewModel.isOnlyOneChapter.collectAsState()
                if (!isOnlyOneChapter)
                    ReaderMenuSurface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        IconButton(onClick = { viewModel.openPrevChapter() }) {
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
                        IconButton(onClick = { viewModel.openNextChapter() }) {
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
                val context = LocalContext.current

                IconButton(modifier = Modifier.weight(1f), onClick = {
                    viewModel.viewModelScope.launch { PR.readerMode.setNext() }
                }) {
                    val icon = when (readingDirection) {
                        ReaderMode.Ltr -> Icons.Filled.ArrowForward
                        ReaderMode.Rtl -> Icons.Filled.ArrowBack
                        ReaderMode.Continuous -> Icons.Filled.Expand
                    }
                    Icon(icon, "setting")
                }

                IconButton(modifier = Modifier.weight(1f), onClick = {
                    viewModel.viewModelScope.launch { PR.readerOrientation.setNext() }
                }) { Icon(Icons.Filled.ScreenRotation, null) }

                IconButton(modifier = Modifier.weight(1f), onClick = {
                    onAction(ReaderAction.OpenColorFilterSheet)
                }) { Icon(Icons.Filled.BrightnessMedium, "color-filter") }

                IconButton(modifier = Modifier.weight(1f), onClick = {
                    onAction(ReaderAction.OpenSettingSheet)
                }) { Icon(Icons.Filled.Settings, "setting") }
            }
        }
    }
}
