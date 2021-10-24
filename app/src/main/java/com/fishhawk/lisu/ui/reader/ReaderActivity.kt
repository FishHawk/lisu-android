package com.fishhawk.lisu.ui.reader

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
import androidx.activity.compose.setContent
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
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.ReaderMode
import com.fishhawk.lisu.data.datastore.ReaderOrientation
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.activity.BaseActivity
import com.fishhawk.lisu.ui.base.StateView
import com.fishhawk.lisu.ui.reader.viewer.ListViewer
import com.fishhawk.lisu.ui.reader.viewer.PagerViewer
import com.fishhawk.lisu.ui.reader.viewer.ViewerState
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReaderActivity : BaseActivity() {
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
            LisuTheme {
                ReaderScreen()
            }
        }
    }
}

//                ReaderPageSheet(this, object : ReaderPageSheet.Listener {
//                    override fun onRefresh() {
//                        reader.refreshPage(position)
//                    }
//
//                    override fun onSave() {
//                        val prefix = viewModel.makeImageFilenamePrefix()
//                            ?: return toast(R.string.toast_chapter_not_loaded)
//                        saveImage(url, "$prefix-$position")
//                    }
//
//                    override fun onShare() {
//                        val prefix = viewModel.makeImageFilenamePrefix()
//                            ?: return toast(R.string.toast_chapter_not_loaded)
//                        lifecycleScope.shareImage(this, url, "$prefix-$position")
//                    }
//                }).show()

internal typealias ReaderActionHandler = (ReaderAction) -> Unit

internal sealed interface ReaderAction {
    object NavUp : ReaderAction

    object OpenSettingSheet : ReaderAction
    object OpenColorFilterSheet : ReaderAction
    class OpenPageSheet(val url: String) : ReaderAction

    object SharePage : ReaderAction
    object SavePage : ReaderAction
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
private fun ReaderScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<ReaderViewModel>()

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var currentBottomSheet by remember { mutableStateOf<ReaderAction?>(null) }

    val onAction: ReaderActionHandler = { action ->
        when (action) {
            ReaderAction.NavUp -> Unit

            ReaderAction.OpenSettingSheet,
            ReaderAction.OpenColorFilterSheet,
            is ReaderAction.OpenPageSheet -> scope.launch {
                currentBottomSheet = action
                sheetState.show()
            }
            ReaderAction.SavePage -> {
            }
            ReaderAction.SharePage -> {
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            when (currentBottomSheet) {
                ReaderAction.OpenColorFilterSheet -> ReaderOverlaySheet()
                ReaderAction.OpenSettingSheet -> ReaderSettingsSheet()
                is ReaderAction.OpenPageSheet -> ReaderPageSheet(onAction)
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

                        if (mode == ReaderMode.Continuous) {
                            readerState = ViewerState.List(
                                rememberSaveable(pointer, mode, saver = LazyListState.Saver) {
                                    LazyListState(firstVisibleItemIndex = startPage)
                                }
                            ).also {
                                ListViewer(it, pointer, onAction)
                            }
                        } else {
                            readerState = ViewerState.Pager(
                                rememberSaveable(pointer, mode, saver = PagerState.Saver) {
                                    PagerState(
                                        pageCount = size,
                                        currentPage = startPage,
                                        offscreenLimit = 3,
                                    )
                                }
                            ).also {
                                PagerViewer(it, pointer, mode == ReaderMode.Rtl, onAction)
                            }
                        }
                        LaunchedEffect(readerState!!.position) {
                            readerState!!.position.let {
                                startPage = it
                                viewModel.updateReadingHistory(it)
                            }
                        }
                    }

                    val name = pointer.currChapter.name
                    val title = pointer.currChapter.title

                    ReaderInfoBar(name, title, readerState)
                    ReaderColorFilterOverlay()
                    ReaderMenu(name, title, readerState, onAction)
                }
            }
        }
    }
}