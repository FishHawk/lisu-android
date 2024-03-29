package com.fishhawk.lisu.ui.reader

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
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
import com.fishhawk.lisu.util.*
import com.fishhawk.lisu.widget.StateView
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.io.File

sealed interface ReaderAction {
    object NavUp : ReaderAction

    object ReloadManga : ReaderAction
    object ReloadChapter : ReaderAction

    data class SetAsCover(val bitmap: Bitmap) : ReaderAction
    data class SavePage(val bitmap: Bitmap, val position: Int) : ReaderAction
    data class SharePage(val bitmap: Bitmap, val position: Int) : ReaderAction

    data class SaveGifPage(val file: File, val position: Int) : ReaderAction
    data class ShareGifPage(val file: File, val position: Int) : ReaderAction

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
    val viewModel = koinViewModel<ReaderViewModel> {
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
            is ReaderAction.SetAsCover -> viewModel.updateCover(action.bitmap)
            is ReaderAction.SavePage -> context.saveDrawable(
                action.bitmap,
                "${mangaTitleResult?.getOrNull()!!}-${action.position}"
            )

            is ReaderAction.SharePage -> context.shareBitmap(
                "Share page via",
                action.bitmap,
                "${mangaTitleResult?.getOrNull()!!}-${action.position}"
            )

            is ReaderAction.SaveGifPage -> context.saveGifFile(
                action.file,
                "${mangaTitleResult?.getOrNull()!!}-${action.position}"
            )

            is ReaderAction.ShareGifPage -> context.shareGifFile(
                "Share page via",
                action.file,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
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
            val startPage = pages
                .filterIsInstance<ReaderPage.Image>()
                .let { it.getOrNull(startPage) ?: it.last() }
                .let { pages.indexOf(it) }
            if (readerMode == ReaderMode.Continuous) ViewerState.Webtoon(
                state = rememberLazyListState(initialFirstVisibleItemIndex = startPage),
                pages = pages,
                requestMoveToPrevChapter = { onAction(ReaderAction.MoveToPrevChapter) },
                requestMoveToNextChapter = { onAction(ReaderAction.MoveToNextChapter) },
            )
            else ViewerState.Pager(
                state = rememberPagerState(initialPage = startPage),
                isRtl = readerMode == ReaderMode.Rtl,
                pages = pages,
                requestMoveToPrevChapter = { onAction(ReaderAction.MoveToPrevChapter) },
                requestMoveToNextChapter = { onAction(ReaderAction.MoveToNextChapter) },
            )
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

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalCoilApi::class, ExperimentalMaterial3Api::class,
)
@Composable
private fun ReaderPages(
    viewerState: ViewerState,
    isMenuOpened: MutableState<Boolean>,
    onAction: (ReaderAction) -> Unit,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val isLongTapDialogEnabled by PR.isLongTapDialogEnabled.collectAsState()


    val bottomSheetState = rememberModalBottomSheetState()
    var bottomSheetPage by remember { mutableStateOf<ReaderPage.Image?>(null) }

    bottomSheetPage?.let { page ->
        ModalBottomSheet(
            onDismissRequest = { bottomSheetPage = null },
            sheetState = bottomSheetState,
            dragHandle = {},
        ) {
            context.imageLoader.memoryCache?.get(MemoryCache.Key(page.url))?.let {
                // Image types other than gif
                ReaderPageSheetContent(
                    bitmap = it.bitmap,
                    position = page.index + 1,
                    onAction = onAction,
                )
            } ?: context.imageLoader.diskCache?.get(page.url)?.use { snapshot ->
                // Gif
                val imageFile = snapshot.data.toFile()
                ReaderPageGifSheetContent(
                    file = imageFile,
                    position = page.index + 1,
                    onAction = onAction,
                )
            }
        }
    }

    val onLongPress = { page: ReaderPage.Image ->
        if (isLongTapDialogEnabled) {
            bottomSheetPage = page
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