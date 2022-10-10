package com.fishhawk.lisu.ui.reader

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.ReaderMode
import com.fishhawk.lisu.data.datastore.ReaderOrientation
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.viewer.PagerViewer
import com.fishhawk.lisu.ui.reader.viewer.ViewerState
import com.fishhawk.lisu.ui.reader.viewer.WebtoonViewer
import com.fishhawk.lisu.util.findActivity
import com.fishhawk.lisu.util.saveImage
import com.fishhawk.lisu.util.shareImage
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.LocalBottomSheetHelper
import com.fishhawk.lisu.widget.StateView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

sealed interface ReaderAction {
    object NavUp : ReaderAction

    object ReloadManga : ReaderAction
    object ReloadChapter : ReaderAction

    data class SetAsImage(val bitmap: Bitmap) : ReaderAction
    data class SavePage(val bitmap: Bitmap, val position: Int) : ReaderAction
    data class SharePage(val bitmap: Bitmap, val position: Int) : ReaderAction

    object OpenPrevChapter : ReaderAction
    object OpenNextChapter : ReaderAction
    object MoveToPrevChapter : ReaderAction
    object MoveToNextChapter : ReaderAction

    object ToggleReaderMode : ReaderAction
    object ToggleReaderOrientation : ReaderAction
    data class UpdateHistory(val page: Int) : ReaderAction
}

@Composable
fun ReaderScreen() {
    val context = LocalContext.current
    val viewModel by viewModel<ReaderViewModel> {
        parametersOf(context.findActivity().intent.extras!!)
    }

    val mangaTitleResult by viewModel.mangaTitleResult.collectAsState()
    val isOnlyOneChapter by viewModel.isOnlyOneChapter.collectAsState()
    val readerMode by viewModel.readerMode.collectAsState()
    val readerOrientation by viewModel.readerOrientation.collectAsState()
    val pointer by viewModel.chapterPointer.collectAsState()

    val onAction: (ReaderAction) -> Unit = { action ->
        when (action) {
            ReaderAction.NavUp -> context.findActivity().finish()
            ReaderAction.ReloadManga -> viewModel.reloadManga()
            ReaderAction.ReloadChapter -> viewModel.loadChapterPointer()
            is ReaderAction.SetAsImage -> viewModel.updateCover(action.bitmap)
            is ReaderAction.SavePage -> context.saveImage(
                action.bitmap,
                "${mangaTitleResult?.getOrNull()!!}-${action.position}"
            )
            is ReaderAction.SharePage -> context.shareImage(
                "Share page via",
                action.bitmap,
                "${mangaTitleResult?.getOrNull()!!}-${action.position}"
            )
            ReaderAction.OpenPrevChapter -> viewModel.openPrevChapter()
            ReaderAction.OpenNextChapter -> viewModel.openNextChapter()
            ReaderAction.MoveToPrevChapter -> viewModel.moveToPrevChapter()
            ReaderAction.MoveToNextChapter -> viewModel.moveToNextChapter()
            is ReaderAction.ToggleReaderMode -> viewModel.toggleReaderMode()
            is ReaderAction.ToggleReaderOrientation -> viewModel.toggleReaderOrientation()
            is ReaderAction.UpdateHistory -> viewModel.updateReadingHistory(action.page)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { effect ->
            when (effect) {
                is ReaderEffect.Message -> context.toast(effect.redId)
            }
        }
    }

    LaunchedEffect(readerOrientation) {
        readerOrientation?.let {
            val activity = context.findActivity()
            val newOrientation = when (it) {
                ReaderOrientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                ReaderOrientation.Landscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            if (newOrientation != activity.requestedOrientation)
                activity.requestedOrientation = newOrientation
        }
    }

    Surface {
        StateView(
            result = mangaTitleResult,
            onRetry = { onAction(ReaderAction.ReloadManga) },
            modifier = Modifier.fillMaxSize(),
        ) { mangaTitle ->
            Reader(
                pages = pointer?.pages,
                startPage = viewModel.startPage,
                readerMode = readerMode ?: return@StateView,
                mangaTitle = mangaTitle,
                isOnlyOneChapter = isOnlyOneChapter,
                onAction = onAction,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
private fun Reader(
    pages: Result<List<ReaderPage>>?,
    startPage: Int,
    readerMode: ReaderMode,
    mangaTitle: String,
    isOnlyOneChapter: Boolean,
    onAction: (ReaderAction) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val viewerStateResult = pages?.mapCatching { pages ->
            rememberSaveable(pages, readerMode, saver = listSaver(
                save = {
                    when (it) {
                        is ViewerState.Webtoon -> listOf(
                            0,
                            it.state.firstVisibleItemIndex,
                            it.state.firstVisibleItemScrollOffset
                        )
                        is ViewerState.Pager -> listOf(
                            1,
                            it.state.currentPage
                        )
                    }
                },
                restore = {
                    when (it[0]) {
                        0 -> ViewerState.Webtoon(
                            state = LazyListState(
                                firstVisibleItemIndex = it[1],
                                firstVisibleItemScrollOffset = it[2]
                            ),
                            pages = pages,
                            requestMoveToPrevChapter = { onAction(ReaderAction.MoveToPrevChapter) },
                            requestMoveToNextChapter = { onAction(ReaderAction.MoveToNextChapter) },
                        )
                        else -> ViewerState.Pager(
                            state = PagerState(currentPage = it[1]),
                            isRtl = readerMode == ReaderMode.Rtl,
                            pages = pages,
                            requestMoveToPrevChapter = { onAction(ReaderAction.MoveToPrevChapter) },
                            requestMoveToNextChapter = { onAction(ReaderAction.MoveToNextChapter) },
                        )
                    }
                }
            )) {
                val startPage = pages
                    .filterIsInstance<ReaderPage.Image>()
                    .let { it.getOrNull(startPage) ?: it.last() }
                    .let { pages.indexOf(it) }
                if (readerMode == ReaderMode.Continuous) ViewerState.Webtoon(
                    state = LazyListState(firstVisibleItemIndex = startPage),
                    pages = pages,
                    requestMoveToPrevChapter = { onAction(ReaderAction.MoveToPrevChapter) },
                    requestMoveToNextChapter = { onAction(ReaderAction.MoveToNextChapter) },
                )
                else ViewerState.Pager(
                    state = PagerState(currentPage = startPage),
                    isRtl = readerMode == ReaderMode.Rtl,
                    pages = pages,
                    requestMoveToPrevChapter = { onAction(ReaderAction.MoveToPrevChapter) },
                    requestMoveToNextChapter = { onAction(ReaderAction.MoveToNextChapter) },
                )
            }
        }

        val isMenuOpened = rememberSaveable { mutableStateOf(false) }
        var currentImagePage by rememberSaveable { mutableStateOf<ReaderPage.Image?>(null) }

        StateView(
            result = viewerStateResult,
            onRetry = { onAction(ReaderAction.ReloadChapter) },
            modifier = Modifier.fillMaxSize(),
        ) { viewerState ->
            ReaderPages(
                viewerState = viewerState,
                isMenuOpened = isMenuOpened,
                onAction = onAction,
            )
            LaunchedEffect(viewerState) {
                snapshotFlow { viewerState.pages[viewerState.position] }
                    .filterIsInstance<ReaderPage.Image>()
                    .collect { currentImagePage = it }
            }
        }

        ReaderInfoBar(
            isMenuOpened = isMenuOpened.value,
            currentImagePage = currentImagePage,
        )

        ReaderColorFilterOverlay()

        ReaderMenu(
            isOpened = isMenuOpened.value,
            mangaTitle = mangaTitle,
            chapterName = "",
            chapterTitle = "",
            readerMode = readerMode,
            isOnlyOneChapter = isOnlyOneChapter,
            currentImagePage = currentImagePage,
            onSnapToPage = { viewerStateResult?.getOrNull()?.scrollToImagePage(it) },
            onAction = onAction
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ReaderPages(
    viewerState: ViewerState,
    isMenuOpened: MutableState<Boolean>,
    onAction: (ReaderAction) -> Unit,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val isLongTapDialogEnabled by PR.isLongTapDialogEnabled.collectAsState()
    val bottomSheetHelper = LocalBottomSheetHelper.current
    val onLongPress = { page: ReaderPage.Image ->
        if (isLongTapDialogEnabled) {
            val bitmap = context.imageLoader.memoryCache?.get(MemoryCache.Key(page.url))?.bitmap
            if (bitmap != null) {
                val sheet = ReaderPageSheet(bitmap, page.index + 1, onAction)
                scope.launch { bottomSheetHelper.open(sheet) }
            }
        }
    }

    val useVolumeKey by PR.useVolumeKey.collectAsState()
    val invertVolumeKey by PR.invertVolumeKey.collectAsState()
    fun onPreviewKeyEvent(keyEvent: KeyEvent): Boolean {
        return when (keyEvent.type) {
            KeyEventType.KeyDown -> {
                when (keyEvent.key) {
                    Key.VolumeUp ->
                        if (useVolumeKey && !isMenuOpened.value) {
                            scope.launch {
                                if (invertVolumeKey) viewerState.toNext()
                                else viewerState.toPrev()
                            }
                            true
                        } else false
                    Key.VolumeDown ->
                        if (useVolumeKey && !isMenuOpened.value) {
                            scope.launch {
                                if (invertVolumeKey) viewerState.toPrev()
                                else viewerState.toNext()
                            }
                            true
                        } else false
                    else -> false
                }
            }
            KeyEventType.KeyUp -> {
                when (keyEvent.key) {
                    Key.Menu -> {
                        isMenuOpened.value = !isMenuOpened.value
                        true
                    }
                    Key.N -> {
                        viewerState.requestMoveToNextChapter()
                        true
                    }
                    Key.P -> {
                        viewerState.requestMoveToPrevChapter()
                        true
                    }
                    Key.DirectionUp, Key.PageUp -> {
                        scope.launch { viewerState.toPrev() }
                        true
                    }
                    Key.DirectionDown, Key.PageDown -> {
                        scope.launch { viewerState.toNext() }
                        true
                    }
                    Key.DirectionLeft -> {
                        scope.launch { viewerState.toLeft() }
                        true
                    }
                    Key.DirectionRight -> {
                        scope.launch { viewerState.toRight() }
                        true
                    }
                    else -> false
                }
            }
            else -> false
        }
    }

    val modifier = Modifier
        .fillMaxSize()
        .onPreviewKeyEvent(::onPreviewKeyEvent)

    when (viewerState) {
        is ViewerState.Pager ->
            PagerViewer(
                modifier = modifier,
                isMenuOpened = isMenuOpened,
                state = viewerState,
                onLongPress = onLongPress,
            )
        is ViewerState.Webtoon ->
            WebtoonViewer(
                modifier = modifier,
                isMenuOpened = isMenuOpened,
                state = viewerState,
                onLongPress = onLongPress,
            )
    }
    LaunchedEffect(viewerState) {
        snapshotFlow { viewerState.pages[viewerState.position] }
            .filterIsInstance<ReaderPage.Image>()
            .collect { page ->
                onAction(ReaderAction.UpdateHistory(page.index))

                // Preload image
                viewerState.pages
                    .filterIsInstance<ReaderPage.Image>()
                    .takeIf { it.isNotEmpty() }
                    ?.slice(
                        (page.index - 3).coerceAtLeast(0)..
                                (page.index + 5).coerceIn(0, page.size - 1)
                    )
                    ?.forEach { adjacentPage ->
                        val request = ImageRequest.Builder(context)
                            .data(adjacentPage.url)
                            .build()
                        context.imageLoader.enqueue(request)
                    }
            }
    }
}