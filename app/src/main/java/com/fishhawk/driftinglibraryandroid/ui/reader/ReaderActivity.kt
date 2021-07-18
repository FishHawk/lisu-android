package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.data.preference.collectAsState
import com.fishhawk.driftinglibraryandroid.ui.activity.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.base.ErrorView
import com.fishhawk.driftinglibraryandroid.ui.base.EventObserver
import com.fishhawk.driftinglibraryandroid.ui.base.LoadingView
import com.fishhawk.driftinglibraryandroid.ui.base.feedback
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReaderActivity : BaseActivity() {
    val viewModel: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        P.screenOrientation.asFlow()
            .onEach {
                val newOrientation = when (it) {
                    P.ScreenOrientation.DEFAULT -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    P.ScreenOrientation.LOCK -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
                    P.ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    P.ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                if (newOrientation != requestedOrientation) requestedOrientation = newOrientation
            }
            .launchIn(lifecycleScope)

        P.keepScreenOn.asFlow()
            .onEach { setFlag(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, it) }
            .launchIn(lifecycleScope)

        combine(
            P.customBrightness.asFlow(),
            P.customBrightnessValue.asFlow()
        ) { isEnabled, brightness ->
            val attrBrightness =
                if (isEnabled) brightness.coerceIn(0, 100) / 100f
                else BRIGHTNESS_OVERRIDE_NONE
            window.attributes = window.attributes.apply { screenBrightness = attrBrightness }
        }.launchIn(lifecycleScope)

        setContent {
            ApplicationTheme {
                val mangaLoadState by viewModel.mangaLoadState.collectAsState()
                when (mangaLoadState) {
                    LoadState.Loading -> LoadingView()
                    is LoadState.Failure -> ErrorView("Manga error") { viewModel.refreshReader() }
                    LoadState.Loaded -> {
                        val pointer by viewModel.chapterPointer.collectAsState()
                        pointer?.let { ReaderContent(it) }
                    }
                }
            }
        }

        viewModel.feedback.observe(this, EventObserver { feedback(it) })

//        combine(
//            P.readingDirection.asFlow(),
//            P.isPageIntervalEnabled.asFlow(),
//            P.isAreaInterpolationEnabled.asFlow()
//        ) { _, _, _ -> initializeReader() }
//            .launchIn(this.lifecycleScope)
    }

//    private fun initializeReader() {
//        readerContainer.removeAllViews()
//
//        reader = when (P.readingDirection.get()) {
//            P.ReadingDirection.LTR,
//            P.ReadingDirection.RTL,
//            P.ReadingDirection.VERTICAL -> ReaderViewPager(this)
//            P.ReadingDirection.CONTINUOUS -> ReaderViewContinuous(this)
//        }
//
//        reader.readingOrientation = when (P.readingDirection.get()) {
//            P.ReadingDirection.LTR,
//            P.ReadingDirection.RTL -> ReaderView.ReadingOrientation.HORIZONTAL
//            P.ReadingDirection.VERTICAL,
//            P.ReadingDirection.CONTINUOUS -> ReaderView.ReadingOrientation.VERTICAL
//        }
//
//        reader.readingDirection = when (P.readingDirection.get()) {
//            P.ReadingDirection.RTL -> ReaderView.ReadingDirection.RTL
//            else -> ReaderView.ReadingDirection.LTR
//        }
//
//        reader.adapter.isAreaInterpolationEnabled =
//            P.isAreaInterpolationEnabled.get()
//        reader.isPageIntervalEnabled = P.isPageIntervalEnabled.get()
//        reader.useVolumeKey = P.useVolumeKey.get()
//        reader.invertVolumeKey = P.invertVolumeKey.get()
//
//        readerContainer.addView(reader)
//        reader.isFocusable = true
//        reader.isFocusableInTouchMode = true
//        reader.requestFocus()
//
//        reader.onRequestPrevChapter = { viewModel.moveToPrevChapter() }
//        reader.onRequestNextChapter = { viewModel.moveToNextChapter() }
//        reader.onRequestMenuVisibility = { viewModel.isMenuOpened.value ?: false }
//        reader.onRequestMenu = {
//            if (viewModel.readerState.value is ViewState.Content)
//                viewModel.isMenuOpened.value = it
//        }
//        reader.onPageChanged = { viewModel.chapterPosition.value = it }
//        reader.onPageLongClicked = { position, url ->
//            if (P.isLongTapDialogEnabled.get())
//                ReaderPageSheet(this, object : ReaderPageSheet.Listener {
//                    override fun onRefresh() {
//                        reader.refreshPage(position)
//                    }
//
//                    override fun onSave() {
//                        val prefix = viewModel.makeImageFilenamePrefix()
//                            ?: return toast(R.string.toast_chapter_not_loaded)
////                        saveImage(url, "$prefix-$position")
//                    }
//
//                    override fun onShare() {
//                        val prefix = viewModel.makeImageFilenamePrefix()
//                            ?: return toast(R.string.toast_chapter_not_loaded)
////                        lifecycleScope.shareImage(this, url, "$prefix-$position")
//                    }
//                }).show()
//        }
//        viewModel.chapterPointer.value?.let {
//            it.startPage = viewModel.chapterPosition.value ?: it.startPage
//        }
//        viewModel.chapterPointer.value = viewModel.chapterPointer.value
//    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
    @Composable
    private fun ReaderContent(pointer: ReaderViewModel.ReaderChapterPointer) {
        Box(modifier = Modifier.fillMaxSize()) {
            val isMenuOpened by viewModel.isMenuOpened.collectAsState()

            val pagerState = rememberPagerState(
                pageCount = pointer.currChapter.images.size,
                initialPage = pointer.startPage
            )

            when (pointer.currChapter.state) {
                LoadState.Loading -> LoadingView()
                is LoadState.Failure -> ErrorView(message = "Chapter error") { }
                LoadState.Loaded -> ReaderContainer(pagerState, pointer)
            }

            val name = pointer.currChapter.name
            val title = pointer.currChapter.title
            val size = pointer.currChapter.images.size
            val position = pagerState.currentPage

            val showInfoBar by P.showInfoBar.asFlow().collectAsState(
                P.showInfoBar.get()
            )
            if (showInfoBar && !isMenuOpened) InfoBar(
                Modifier.align(Alignment.BottomEnd),
                name, title, position, size
            )

            ColorFilterOverlay()

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
                ReaderMenuBottom(size, pagerState)
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun ReaderContainer(
        pagerState: PagerState,
        pointer: ReaderViewModel.ReaderChapterPointer
    ) {
        val scope = rememberCoroutineScope()
        val readerDirection by P.readingDirection.collectAsState()

        fun toNext() {
            if (pagerState.currentPage < pagerState.pageCount - 1)
                scope.launch { pagerState.scrollToPage(pagerState.currentPage + 1) }
            else viewModel.moveToNextChapter()
        }

        fun toPrev() {
            if (pagerState.currentPage > 1)
                scope.launch { pagerState.scrollToPage(pagerState.currentPage - 1) }
            else viewModel.moveToPrevChapter()
        }

        val isRtl = readerDirection == P.ReadingDirection.RTL
        fun toLeft() = if (isRtl) toNext() else toPrev()
        fun toRight() = if (isRtl) toPrev() else toNext()

        val useVolumeKey by P.useVolumeKey.collectAsState()
        val invertVolumeKey by P.invertVolumeKey.collectAsState()

        val focusRequester = remember { FocusRequester() }
        Box(modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent {
                when (it.type) {
                    KeyEventType.KeyDown -> {
                        when (it.key) {
                            Key.VolumeUp ->
                                if (useVolumeKey && !viewModel.isMenuOpened.value) {
                                    if (invertVolumeKey) toNext() else toPrev()
                                } else return@onPreviewKeyEvent false
                            Key.VolumeDown ->
                                if (useVolumeKey && !viewModel.isMenuOpened.value) {
                                    if (invertVolumeKey) toPrev() else toNext()
                                } else return@onPreviewKeyEvent false
                            else -> return@onPreviewKeyEvent false
                        }
                    }
                    KeyEventType.KeyUp -> {
                        when (it.key) {
                            Key.Menu -> viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value

                            Key.N -> viewModel.moveToNextChapter()
                            Key.P -> viewModel.moveToPrevChapter()

                            Key.DirectionUp, Key.PageUp -> toPrev()
                            Key.DirectionDown, Key.PageDown -> toNext()

                            Key.DirectionLeft -> toLeft()
                            Key.DirectionRight -> toRight()
                            else -> return@onPreviewKeyEvent false
                        }
                    }
                    else -> return@onPreviewKeyEvent false
                }
                true
            }
        ) {
            PagerReader(
                pagerState,
                pointer.currChapter.images,
                onTap = { viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value }
            )
        }

        DisposableEffect(Unit) {
            focusRequester.requestFocus()
            onDispose { }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun ReaderMenuBottom(size: Int, pagerState: PagerState) {
        val readingDirection = when (P.readingDirection.let {
            it.asFlow().collectAsState(it.get())
        }.value) {
            P.ReadingDirection.RTL -> LayoutDirection.Rtl
            else -> LayoutDirection.Ltr
        }

        CompositionLocalProvider(LocalLayoutDirection provides readingDirection) {
            Row(
                modifier = Modifier
                    .background(Color(0xAA000000))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isOnlyOneChapter by viewModel.isOnlyOneChapter.collectAsState()
                if (!isOnlyOneChapter)
                    IconButton(onClick = { viewModel.openPrevChapter() }) {
                        Icon(
                            if (LocalLayoutDirection.current == LayoutDirection.Ltr)
                                Icons.Filled.SkipPrevious else Icons.Filled.SkipNext,
                            "prev", tint = Color.White
                        )
                    }

                if (size > 1) {
                    Text(
                        modifier = Modifier.width(32.dp),
                        text = pagerState.currentPage.plus(1).toString(),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    val scope = rememberCoroutineScope()
                    Slider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp, end = 8.dp),
                        value = pagerState.currentPage.toFloat() / size.minus(1),
                        onValueChange = {
                            val target = (it * (size - 1)).toInt().coerceIn(0, size - 1)
                            scope.launch { pagerState.scrollToPage(target) }
                        }
                    )
                    Text(
                        modifier = Modifier.width(32.dp),
                        text = size.toString(),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (!isOnlyOneChapter)
                    IconButton(onClick = { viewModel.openNextChapter() }) {
                        Icon(
                            if (LocalLayoutDirection.current == LayoutDirection.Ltr)
                                Icons.Filled.SkipNext else Icons.Filled.SkipPrevious,
                            "next", tint = Color.White
                        )
                    }
            }
        }
    }

    @Composable
    private fun ReaderMenuTop(name: String, title: String) {
        Row(
            modifier = Modifier
                .background(Color(0xAA000000))
                .padding(4.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            IconButton(onClick = { finish() }) {
                Icon(Icons.Filled.NavigateBefore, "back", tint = Color.White)
            }
            Text(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .padding(8.dp),
                text = "$name $title",
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )
            IconButton(onClick = {
                ReaderOverlaySheet(context, scope)
                    .apply {
                        setOnDismissListener { viewModel.isMenuOpened.value = true }
                        viewModel.isMenuOpened.value = false
                        window?.setDimAmount(0f)
                    }
                    .show()
            }) {
                Icon(Icons.Filled.BrightnessMedium, "color-filter", tint = Color.White)
            }
            IconButton(onClick = { ReaderSettingsSheet(context).show() }) {
                Icon(Icons.Filled.Settings, "setting", tint = Color.White)
            }
        }
    }
}

@Composable
private fun InfoBar(modifier: Modifier, name: String, title: String, position: Int, size: Int) {
    val infoBarText =
        if (size != 0) "$name $title ${position + 1}/$size"
        else "$name $title"

    Box(
        modifier = modifier
            .background(Color(0xAA000000))
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Text(text = infoBarText, color = Color.White)
    }
}
