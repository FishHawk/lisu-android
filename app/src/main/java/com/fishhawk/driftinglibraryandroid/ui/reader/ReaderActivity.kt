package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fishhawk.driftinglibraryandroid.data.datastore.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderOrientation
import com.fishhawk.driftinglibraryandroid.ui.activity.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.base.ErrorView
import com.fishhawk.driftinglibraryandroid.ui.base.LoadingView
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
                if (isEnabled) brightness.coerceIn(0, 100) / 100f
                else BRIGHTNESS_OVERRIDE_NONE
            window.attributes = window.attributes.apply { screenBrightness = attrBrightness }
        }.launchIn(lifecycleScope)

        setContent {
            ApplicationTheme {
                val viewModel = viewModel<ReaderViewModel>()
                val mangaLoadState by viewModel.mangaLoadState.collectAsState()
                when (val state = mangaLoadState) {
                    LoadState.Loading -> LoadingView()
                    is LoadState.Failure -> ErrorView(state.exception) { viewModel.refreshReader() }
                    LoadState.Loaded -> {
                        val pointer by viewModel.chapterPointer.collectAsState()
                        Reader(pointer)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
private fun Reader(pointer: ReaderViewModel.ReaderChapterPointer) {
    Box(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(
            pageCount = pointer.currChapter.images.size,
            initialOffscreenLimit = 3
        )

        when (val state = pointer.currChapter.state) {
            LoadState.Loading -> LoadingView()
            is LoadState.Failure -> ErrorView(state.exception) { }
            LoadState.Loaded -> ReaderContent(pagerState, pointer)
        }

        val name = pointer.currChapter.name
        val title = pointer.currChapter.title
        val size = pointer.currChapter.images.size
        val position = pagerState.currentPage

        ReaderInfoBar(name, title, position, size)
        ReaderColorFilterOverlay()
        ReaderMenu(name, title, size, pagerState)
    }
}