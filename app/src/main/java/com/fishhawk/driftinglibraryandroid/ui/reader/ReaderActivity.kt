package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
import androidx.activity.compose.setContent
import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderMode
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderOrientation
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.ui.activity.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.base.ErrorView
import com.fishhawk.driftinglibraryandroid.ui.base.LoadingView
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class BottomSheet() {
    object SettingSheet : BottomSheet()
    object ColorFilterSheet : BottomSheet()
    class PageSheet(val argument: String) : BottomSheet()
}

lateinit var openSheet: (BottomSheet) -> Unit
lateinit var closeSheet: () -> Unit

@AndroidEntryPoint
class ReaderActivity : BaseActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PR.readerOrientation.flow
            .onEach {
                val newOrientation = when (it) {
                    ReaderOrientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    ReaderOrientation.Landscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                if (newOrientation != requestedOrientation) requestedOrientation = newOrientation
            }
            .launchIn(lifecycleScope)

        PR.keepScreenOn.flow
            .onEach { setFlag(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, it) }
            .launchIn(lifecycleScope)

        combine(
            PR.enableCustomBrightness.flow,
            PR.customBrightness.flow
        ) { isEnabled, brightness ->
            val attrBrightness =
                if (isEnabled) brightness.coerceIn(0f, 1f)
                else BRIGHTNESS_OVERRIDE_NONE
            window.attributes = window.attributes.apply { screenBrightness = attrBrightness }
        }.launchIn(lifecycleScope)

        setContent {
            ApplicationTheme {
                val scope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
                var currentBottomSheet by remember { mutableStateOf<BottomSheet?>(null) }

                openSheet = { it: BottomSheet ->
                    scope.launch {
                        currentBottomSheet = it
                        sheetState.show()
                    }
                }

                closeSheet = { scope.launch { sheetState.hide() } }

                ModalBottomSheetLayout(
                    sheetState = sheetState,
                    sheetContent = {
                        when (currentBottomSheet) {
                            BottomSheet.ColorFilterSheet -> ReaderOverlaySheet()
                            BottomSheet.SettingSheet -> ReaderSettingsSheet()
                            else -> {
                                Text("test")
                            }
                        }
                    },
                    scrimColor = Color.Transparent
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) { ReaderScreen() }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ReaderScreen() {
    val viewModel = viewModel<ReaderViewModel>()
    val mangaLoadState by viewModel.mangaLoadState.collectAsState()

    when (mangaLoadState) {
        LoadState.Loading -> LoadingView()
        is LoadState.Failure -> ErrorView((mangaLoadState as LoadState.Failure).exception) { viewModel.refreshReader() }
        LoadState.Loaded -> {
            val pointer by viewModel.chapterPointer.collectAsState()
            Box(modifier = Modifier.fillMaxSize()) {
                var readerState: ReaderState? = null
                when (val state = pointer.currChapter.state) {
                    LoadState.Loading -> LoadingView()
                    is LoadState.Failure -> ErrorView(state.exception) { }
                    LoadState.Loaded -> {
                        val mode by PR.readerMode.collectAsState()
                        val size = pointer.currChapter.images.size
                        var startPage by remember(pointer) {
                            mutableStateOf(pointer.startPage.coerceAtMost(size - 1))
                        }

                        if (mode == ReaderMode.Continuous) {
                            readerState = ReaderState.List(
                                rememberSaveable(pointer, mode, saver = LazyListState.Saver) {
                                    LazyListState(firstVisibleItemIndex = startPage)
                                }
                            )
                            ListReader(readerState, pointer)
                        } else {
                            readerState = ReaderState.Pager(
                                rememberSaveable(pointer, mode, saver = PagerState.Saver) {
                                    PagerState(
                                        pageCount = size,
                                        currentPage = startPage,
                                        offscreenLimit = 3,
                                    )
                                }
                            )
                            PagerReader(readerState.state, pointer, mode == ReaderMode.Rtl)
                        }
                        LaunchedEffect(readerState.position) {
                            readerState.position.let {
                                startPage = it
                                viewModel.updateReadingHistory(it)
                            }
                        }
                    }
                }

                val name = pointer.currChapter.name
                val title = pointer.currChapter.title

                ReaderInfoBar(name, title, readerState)
                ReaderColorFilterOverlay()
                ReaderMenu(name, title, readerState)
            }
        }
    }
}

sealed interface ReaderState {
    @get:IntRange(from = 0)
    val position: Int

    @get:IntRange(from = 0)
    val size: Int

    suspend fun scrollToPage(@IntRange(from = 0) page: Int)

    @OptIn(ExperimentalPagerApi::class)
    class Pager(val state: PagerState) : ReaderState {
        override val position: Int
            get() = state.currentPage

        override val size: Int
            get() = state.pageCount

        override suspend fun scrollToPage(page: Int) = state.scrollToPage(page)
    }

    class List(val state: LazyListState) : ReaderState {
        override val position: Int
            get() = state.firstVisibleItemIndex

        override val size: Int
            get() = state.layoutInfo.totalItemsCount

        override suspend fun scrollToPage(page: Int) = state.scrollToItem(page)
    }
}