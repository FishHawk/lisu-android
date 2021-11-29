package com.fishhawk.lisu.ui.reader

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.ReaderMode
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.base.StateView
import com.fishhawk.lisu.ui.base.saveImage
import com.fishhawk.lisu.ui.base.shareImage
import com.fishhawk.lisu.ui.base.toast
import com.fishhawk.lisu.ui.reader.viewer.ListViewer
import com.fishhawk.lisu.ui.reader.viewer.PagerViewer
import com.fishhawk.lisu.ui.reader.viewer.ViewerState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal typealias ReaderActionHandler = (ReaderAction) -> Unit

internal sealed interface ReaderAction {
    object NavUp : ReaderAction

    object OpenSettingSheet : ReaderAction
    object OpenColorFilterSheet : ReaderAction
    class OpenPageSheet(val drawable: Drawable, val position: Int) : ReaderAction

    data class SavePage(val drawable: Drawable, val position: Int) : ReaderAction
    data class SharePage(val drawable: Drawable, val position: Int) : ReaderAction

    object OpenPrevChapter : ReaderAction
    object OpenNextChapter : ReaderAction
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun ReaderScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var currentBottomSheet by remember { mutableStateOf<ReaderAction?>(null) }

    val viewModel = viewModel<ReaderViewModel>()
    val mangaTitle by viewModel.mangaTitle.collectAsState()
    val isMenuOpened by viewModel.isMenuOpened.collectAsState()
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setNavigationBarColor(
            color = Color(0xFF333333).copy(alpha = 0.8f)
        )
    }

    val onAction: ReaderActionHandler = { action ->
        when (action) {
            ReaderAction.NavUp -> Unit

            ReaderAction.OpenSettingSheet,
            ReaderAction.OpenColorFilterSheet,
            is ReaderAction.OpenPageSheet -> scope.launch {
                currentBottomSheet = action
                sheetState.show()
            }
            is ReaderAction.SavePage ->
                context.saveImage(
                    action.drawable,
                    "${mangaTitle}-${action.position}"
                )
            is ReaderAction.SharePage ->
                context.shareImage(
                    "Share page via",
                    action.drawable,
                    "${mangaTitle}-${action.position}"
                )
            ReaderAction.OpenPrevChapter -> viewModel.openPrevChapter()
            ReaderAction.OpenNextChapter -> viewModel.openNextChapter()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReaderEffect.Message -> context.toast(effect.redId)
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            when (currentBottomSheet) {
                ReaderAction.OpenColorFilterSheet -> ReaderOverlaySheet()
                ReaderAction.OpenSettingSheet -> ReaderSettingsSheet()
                is ReaderAction.OpenPageSheet -> ReaderPageSheet(
                    (currentBottomSheet as ReaderAction.OpenPageSheet).drawable,
                    (currentBottomSheet as ReaderAction.OpenPageSheet).position,
                    onAction
                )
                else -> Text("test")
            }
        },
        scrimColor =
        if (currentBottomSheet is ReaderAction.OpenColorFilterSheet) Color.Transparent
        else ModalBottomSheetDefaults.scrimColor
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val mangaViewState by viewModel.mangaLoadState.collectAsState()

            StateView(
                modifier = Modifier.fillMaxSize(),
                viewState = mangaViewState,
                onRetry = { viewModel.refreshReader() }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val pointer by viewModel.chapterPointer.collectAsState()
                    var readerState: ViewerState? = null
                    StateView(
                        modifier = Modifier.fillMaxSize(),
                        viewState = pointer.currChapter.state,
                        onRetry = { }
                    ) {
                        val mode by PR.readerMode.collectAsState()
                        val size = pointer.currChapter.images.size
                        var startPage by remember(pointer) {
                            mutableStateOf(pointer.startPage.coerceAtMost(size - 1))
                        }
                        readerState =
                            if (mode == ReaderMode.Continuous) ViewerState.List(
                                rememberSaveable(pointer, mode, saver = LazyListState.Saver) {
                                    LazyListState(firstVisibleItemIndex = startPage)
                                }
                            ).also {
                                ListViewer(it, pointer, onAction)
                            }
                            else ViewerState.Pager(
                                rememberSaveable(pointer, mode, saver = PagerState.Saver) {
                                    PagerState(currentPage = startPage)
                                }
                            ).also {
                                PagerViewer(it, pointer, mode == ReaderMode.Rtl, onAction)
                            }
                        LaunchedEffect(readerState) {
                            snapshotFlow { readerState!!.position }.collect {
                                startPage = it
                                viewModel.updateReadingHistory(it)
                                pointer.currChapter.images
                                    .slice(
                                        (it - 3).coerceAtLeast(0)..
                                                (it + 5).coerceAtMost(readerState!!.size - 1)
                                    )
                                    .forEach { url ->
                                        val request = ImageRequest.Builder(context)
                                            .data(url)
                                            .build()
                                        context.imageLoader.enqueue(request)
                                    }
                            }
                        }
                    }

                    val name = pointer.currChapter.name
                    val title = pointer.currChapter.title

                    val showInfoBar by PR.showInfoBar.collectAsState()
                    val isOnlyOneChapter by viewModel.isOnlyOneChapter.collectAsState()

                    if (showInfoBar && !isMenuOpened)
                        ReaderInfoBar(name, title, readerState)
                    ReaderColorFilterOverlay()
                    ReaderMenu(
                        isMenuOpened,
                        mangaTitle,
                        name,
                        title,
                        isOnlyOneChapter,
                        readerState,
                        onAction
                    )
                }
            }
        }
    }
}