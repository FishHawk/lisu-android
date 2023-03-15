package com.fishhawk.lisu.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Expand
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.ReaderMode
import com.fishhawk.lisu.data.datastore.Theme
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.util.findActivity
import com.fishhawk.lisu.widget.TooltipIconButton
import com.fishhawk.lisu.widget.LisuSlider
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull

@Composable
internal fun BoxScope.ReaderInfoBar(
    isMenuOpened: Boolean,
    currentImagePage: ReaderPage.Image?,
) {
    val showInfoBar by PR.showInfoBar.collectAsState()
    if (!showInfoBar || isMenuOpened || currentImagePage == null) return

    val infoBarText = currentImagePage.let { "${it.index + 1}/${it.size}" }
    Text(
        text = infoBarText,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 4.dp),
        color = Color.White,
        style = MaterialTheme.typography.titleMedium.copy(
            shadow = Shadow(
                color = Color.Black,
                blurRadius = 3f,
            ),
        ),
    )
}

@Composable
internal fun BoxScope.ReaderMenu(
    isOpened: Boolean,
    mangaTitle: String,
    chapterName: String,
    chapterTitle: String,
    readerMode: ReaderMode,
    isOnlyOneChapter: Boolean,
    currentImagePage: ReaderPage.Image?,
    onSnapToPage: suspend (Int) -> Unit,
    onAction: (ReaderAction) -> Unit,
) {
    val controller = rememberSystemUiController()
    val theme by PR.theme.collectAsState()
    val useDarkIcons = theme == Theme.Light && !isOpened
    LaunchedEffect(Unit) {
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
    }
    LaunchedEffect(isOpened) {
        controller.setSystemBarsColor(color = Color.Transparent, darkIcons = useDarkIcons, false)
        controller.isNavigationBarVisible = isOpened
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.TopCenter),
        visible = isOpened,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        ReaderMenuTop(
            firstLineText = mangaTitle,
            secondLineText = "$chapterName $chapterTitle",
        )
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = isOpened,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        ReaderMenuBottom(
            readerMode = readerMode,
            isOnlyOneChapter = isOnlyOneChapter,
            currentImagePage = currentImagePage,
            onSnapToPage = onSnapToPage,
            onAction = onAction
        )
    }
}

@Composable
private fun ReaderMenuTop(
    firstLineText: String,
    secondLineText: String,
) {
    ReaderMenuSurface {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            TooltipIconButton(
                tooltip = stringResource(R.string.action_back),
                icon = LisuIcons.ArrowBack,
                onClick = { context.findActivity().finish() },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
            ) {
                Text(
                    text = firstLineText,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondLineText.isNotBlank()) {
                    MediumEmphasis {
                        Text(
                            text = secondLineText,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderMenuBottom(
    readerMode: ReaderMode,
    isOnlyOneChapter: Boolean,
    currentImagePage: ReaderPage.Image?,
    onSnapToPage: suspend (Int) -> Unit,
    onAction: (ReaderAction) -> Unit,
) {
    Column {
        CompositionLocalProvider(
            LocalLayoutDirection provides
                    if (readerMode == ReaderMode.Rtl) LayoutDirection.Rtl
                    else LayoutDirection.Ltr
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!isOnlyOneChapter) ReaderMenuSurface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                ) {
                    TooltipIconButton(
                        tooltip = stringResource(R.string.action_prev_chapter),
                        icon = if (LocalLayoutDirection.current == LayoutDirection.Ltr) {
                            LisuIcons.SkipPrevious
                        } else {
                            LisuIcons.SkipNext
                        },
                        onClick = { onAction(ReaderAction.OpenPrevChapter) },
                    )
                }

                ReaderMenuSurface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        var sizeLabelWidth by remember { mutableStateOf<Int?>(null) }
                        val widthModifier = sizeLabelWidth?.let { width ->
                            with(LocalDensity.current) {
                                Modifier.width(width.toDp())
                            }
                        } ?: Modifier

                        Text(
                            modifier = widthModifier,
                            text = currentImagePage?.index?.plus(1)?.toString() ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        var isSliderChanging by remember { mutableStateOf(false) }
                        var sliderValue by remember {
                            mutableStateOf(
                                currentImagePage?.let {
                                    it.index.toFloat() /
                                            (it.size - 1).coerceAtLeast(1).toFloat()
                                } ?: 0f
                            )
                        }
                        currentImagePage?.let {
                            if (!isSliderChanging) {
                                sliderValue = it.index.toFloat() / (it.size - 1).coerceAtLeast(1)
                            }
                        }

                        LaunchedEffect(Unit) {
                            snapshotFlow { sliderValue }
                                .mapNotNull {
                                    if (currentImagePage == null) null
                                    else (it * (currentImagePage.size - 1)).toInt()
                                }
                                .collectLatest(onSnapToPage)
                        }
                        LisuSlider(
                            value = sliderValue,
                            onValueChange = {
                                isSliderChanging = true
                                sliderValue = it
                            },
                            modifier = Modifier.weight(1f),
                            enabled = currentImagePage != null && currentImagePage.size > 1,
                            onValueChangeFinished = { isSliderChanging = false },
                        )

                        Text(text = currentImagePage?.size?.toString() ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            onTextLayout = { sizeLabelWidth = it.size.width })
                    }
                }

                if (!isOnlyOneChapter) ReaderMenuSurface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                ) {
                    TooltipIconButton(
                        tooltip = stringResource(R.string.action_next_chapter),
                        icon = if (LocalLayoutDirection.current == LayoutDirection.Ltr) {
                            LisuIcons.SkipNext
                        } else {
                            LisuIcons.SkipPrevious
                        },
                        onClick = { onAction(ReaderAction.OpenNextChapter) },
                    )
                }
            }
        }

        ReaderMenuSurface {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TooltipIconButton(
                    tooltip = stringResource(R.string.action_reader_mode),
                    icon = when (readerMode) {
                        ReaderMode.Ltr -> LisuIcons.ArrowForward
                        ReaderMode.Rtl -> LisuIcons.ArrowBack
                        ReaderMode.Continuous -> LisuIcons.Expand
                    },
                    modifier = Modifier.weight(1f),
                    onClick = { onAction(ReaderAction.ToggleReaderMode) },
                )

                TooltipIconButton(
                    tooltip = stringResource(R.string.action_reader_orientation),
                    icon = LisuIcons.ScreenRotation,
                    modifier = Modifier.weight(1f),
                    onClick = { onAction(ReaderAction.ToggleReaderOrientation) },
                )

                val bottomSheetState = rememberModalBottomSheetState()
                var openBottomSheet by rememberSaveable { mutableStateOf(0) }


                TooltipIconButton(
                    tooltip = stringResource(R.string.action_reader_color_filter),
                    icon = LisuIcons.BrightnessMedium,
                    modifier = Modifier.weight(1f),
                    onClick = { openBottomSheet = 1 },
                )

                TooltipIconButton(
                    tooltip = stringResource(R.string.action_reader_setting),
                    icon = LisuIcons.Settings,
                    modifier = Modifier.weight(1f),
                    onClick = { openBottomSheet = 2 },
                )

                if (openBottomSheet > 0) {
                    ModalBottomSheet(
                        onDismissRequest = { openBottomSheet = 0 },
                        sheetState = bottomSheetState,
                        dragHandle = {},
                    ) {
                        if (openBottomSheet == 1) {
                            ReaderOverlaySheetContent()
                        } else {
                            ReaderSettingsSheetContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderMenuSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit,
) = Surface(
    modifier = modifier,
    shape = shape,
    color = Color(0xFF333333).copy(alpha = 0.8f),
    contentColor = Color.White,
    content = content
)
