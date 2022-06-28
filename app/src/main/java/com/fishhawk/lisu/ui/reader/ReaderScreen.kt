package com.fishhawk.lisu.ui.reader

import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.ReaderMode
import com.fishhawk.lisu.data.datastore.ReaderOrientation
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.viewer.ListViewer
import com.fishhawk.lisu.ui.reader.viewer.PagerViewer
import com.fishhawk.lisu.ui.reader.viewer.ViewerState
import com.fishhawk.lisu.util.findActivity
import com.fishhawk.lisu.util.saveImage
import com.fishhawk.lisu.util.shareImage
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.LocalBottomSheetHelper
import com.fishhawk.lisu.widget.StateView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

internal typealias ReaderActionHandler = (ReaderAction) -> Unit

sealed interface ReaderAction {
    object NavUp : ReaderAction

    object ReloadManga : ReaderAction
    object ReloadChapter : ReaderAction

    data class SetAsImage(val drawable: Drawable) : ReaderAction
    data class SavePage(val drawable: Drawable, val position: Int) : ReaderAction
    data class SharePage(val drawable: Drawable, val position: Int) : ReaderAction

    object OpenPrevChapter : ReaderAction
    object OpenNextChapter : ReaderAction
    object MoveToPrevChapter : ReaderAction
    object MoveToNextChapter : ReaderAction

    object ToggleReaderMode : ReaderAction
    object ToggleReaderOrientation : ReaderAction
    data class UpdateHistory(val page: Int) : ReaderAction
}

@OptIn(ExperimentalPagerApi::class)
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

    val onAction: ReaderActionHandler = { action ->
        when (action) {
            ReaderAction.NavUp -> context.findActivity().finish()
            ReaderAction.ReloadManga -> viewModel.reloadManga()
            ReaderAction.ReloadChapter -> viewModel.loadChapterPointer()
            is ReaderAction.SetAsImage -> viewModel.updateCover(action.drawable)
            is ReaderAction.SavePage -> context.saveImage(
                action.drawable,
                "${mangaTitleResult?.getOrNull()!!}-${action.position}"
            )
            is ReaderAction.SharePage -> context.shareImage(
                "Share page via",
                action.drawable,
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
                pointer = pointer ?: return@StateView,
                readerMode = readerMode,
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
    pointer: ReaderViewModel.ReaderChapterPointer,
    readerMode: ReaderMode?,
    mangaTitle: String,
    isOnlyOneChapter: Boolean,
    onAction: ReaderActionHandler,
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        var viewerState: ViewerState? = null
        val isMenuOpened = rememberSaveable { mutableStateOf(false) }
        StateView(
            result = pointer.pages,
            onRetry = { onAction(ReaderAction.ReloadChapter) },
            modifier = Modifier.fillMaxSize(),
        ) { pages ->
            var startPage by remember(pointer) {
                mutableStateOf(pointer.startPage.coerceAtMost(pages.size - 1))
            }

            val scope = rememberCoroutineScope()
            val isLongTapDialogEnabled by PR.isLongTapDialogEnabled.collectAsState()
            val bottomSheetHelper = LocalBottomSheetHelper.current
            val onLongPress = { drawable: Drawable, position: Int ->
                if (isLongTapDialogEnabled) {
                    val sheet = ReaderPageSheet(drawable, position, onAction)
                    scope.launch { bottomSheetHelper.open(sheet) }
                }
            }

            viewerState =
                if (readerMode == ReaderMode.Continuous) ViewerState.List(
                    rememberSaveable(pointer, readerMode, saver = LazyListState.Saver) {
                        LazyListState(firstVisibleItemIndex = startPage)
                    }
                ).also {
                    ListViewer(
                        isMenuOpened = isMenuOpened,
                        state = it,
                        pages = pages,
                        requestMoveToPrevChapter = { onAction(ReaderAction.MoveToPrevChapter) },
                        requestMoveToNextChapter = { onAction(ReaderAction.MoveToNextChapter) },
                        onLongPress = onLongPress,
                    )
                }
                else ViewerState.Pager(
                    rememberSaveable(pointer, readerMode, saver = PagerState.Saver) {
                        PagerState(currentPage = startPage)
                    }
                ).also {
                    PagerViewer(
                        isMenuOpened = isMenuOpened,
                        state = it,
                        pages = pages,
                        isRtl = readerMode == ReaderMode.Rtl,
                        requestMoveToPrevChapter = { onAction(ReaderAction.MoveToPrevChapter) },
                        requestMoveToNextChapter = { onAction(ReaderAction.MoveToNextChapter) },
                        onLongPress = onLongPress,
                    )
                }
            LaunchedEffect(viewerState) {
                snapshotFlow { viewerState!!.position }.collect {
                    startPage = it
                    onAction(ReaderAction.UpdateHistory(it))
                    pages
                        .slice(
                            (it - 3).coerceAtLeast(0)..
                                    (it + 5).coerceAtMost(viewerState!!.size - 1)
                        )
                        .forEach { page ->
                            if (page is ReaderPage.Image) {
                                val request = ImageRequest.Builder(context)
                                    .data(page.url)
                                    .build()
                                context.imageLoader.enqueue(request)
                            }
                        }
                }
            }
        }

        val showInfoBar by PR.showInfoBar.collectAsState()
        if (showInfoBar && !isMenuOpened.value)
            viewerState?.let { ReaderInfoBar(it) }

        ReaderColorFilterOverlay()

        ReaderMenu(
            isOpened = isMenuOpened.value,
            mangaTitle = mangaTitle,
            chapterName = pointer.chapterName,
            chapterTitle = pointer.chapterTitle,
            readerMode = readerMode ?: return@Box,
            isOnlyOneChapter = isOnlyOneChapter,
            viewerState = viewerState,
            onAction = onAction
        )
    }
}