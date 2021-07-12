package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.activity.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.reader.viewer.*
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import com.google.accompanist.insets.statusBarsPadding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class ReaderActivity : BaseActivity() {
    val viewModel: ReaderViewModel by viewModels()

    lateinit var reader: ReaderView
    lateinit var readerContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalPreference.screenOrientation.asFlow()
            .onEach {
                val newOrientation = when (it) {
                    GlobalPreference.ScreenOrientation.DEFAULT -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    GlobalPreference.ScreenOrientation.LOCK -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
                    GlobalPreference.ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    GlobalPreference.ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                if (newOrientation != requestedOrientation) requestedOrientation = newOrientation
            }
            .launchIn(lifecycleScope)

        GlobalPreference.keepScreenOn.asFlow()
            .onEach { setFlag(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, it) }
            .launchIn(lifecycleScope)

        combine(
            GlobalPreference.customBrightness.asFlow(),
            GlobalPreference.customBrightnessValue.asFlow()
        ) { isEnabled, brightness ->
            val attrBrightness =
                if (isEnabled) brightness.coerceIn(0, 100) / 100f
                else BRIGHTNESS_OVERRIDE_NONE
            window.attributes = window.attributes.apply { screenBrightness = attrBrightness }
        }.launchIn(lifecycleScope)

        readerContainer = FrameLayout(this)
        setContent {
            ApplicationTheme {
                val readerState by viewModel.readerState.observeAsState(ViewState.Loading)
                when (readerState) {
                    is ViewState.Error -> ErrorView(message = "Manga error") { viewModel.refreshReader() }
                    ViewState.Loading -> LoadingView()
                    else -> {
                        val pointer by viewModel.chapterPointer.observeAsState()
                        when (pointer?.currChapter?.state) {
                            is ViewState.Error -> ErrorView(message = "Chapter error") { viewModel.refreshReader() }
                            ViewState.Content -> ReaderContent()
                            else -> LoadingView()
                        }
                    }
                }
            }
        }

        viewModel.feedback.observe(this, EventObserver { feedback(it) })

        viewModel.chapterPointer.observe(this) { pointer ->
            if (pointer != null) {
                val chapterContent = ReaderContent(
                    "${pointer.currChapter.name} ${pointer.currChapter.title}",
                    pointer.currChapter.images,
                    pointer.prevChapter?.let {
                        AdjacentChapter("${it.name} ${it.title}", it.state)
                    },
                    pointer.nextChapter?.let {
                        AdjacentChapter("${it.name} ${it.title}", it.state)
                    }
                )
                reader.adapter.setReaderContent(chapterContent)

                if (pointer.currChapter.state == ViewState.Content) {
                    reader.setPage(
                        pointer.startPage
                            .coerceAtMost(pointer.currChapter.images.size - 1)
                            .coerceAtLeast(0)
                    )
                }
            }
        }
        viewModel.prevChapterStateChanged.observe(this, EventObserver {
            reader.setPrevChapterState(it)
        })
        viewModel.nextChapterStateChanged.observe(this, EventObserver {
            reader.setNextChapterState(it)
        })

        combine(
            GlobalPreference.readingDirection.asFlow(),
            GlobalPreference.isPageIntervalEnabled.asFlow(),
            GlobalPreference.isAreaInterpolationEnabled.asFlow()
        ) { _, _, _ -> initializeReader() }
            .launchIn(this.lifecycleScope)

        GlobalPreference.useVolumeKey.asFlow()
            .onEach { reader.useVolumeKey = it }
            .launchIn(this.lifecycleScope)

        GlobalPreference.invertVolumeKey.asFlow()
            .onEach { reader.invertVolumeKey = it }
            .launchIn(this.lifecycleScope)
    }

    override fun onPause() {
        super.onPause()
        runBlocking {
            viewModel.updateReadingHistory()
        }
    }

    private fun initializeReader() {
        readerContainer.removeAllViews()

        reader = when (GlobalPreference.readingDirection.get()) {
            GlobalPreference.ReadingDirection.LTR,
            GlobalPreference.ReadingDirection.RTL,
            GlobalPreference.ReadingDirection.VERTICAL -> ReaderViewPager(this)
            GlobalPreference.ReadingDirection.CONTINUOUS -> ReaderViewContinuous(this)
        }

        reader.readingOrientation = when (GlobalPreference.readingDirection.get()) {
            GlobalPreference.ReadingDirection.LTR,
            GlobalPreference.ReadingDirection.RTL -> ReaderView.ReadingOrientation.HORIZONTAL
            GlobalPreference.ReadingDirection.VERTICAL,
            GlobalPreference.ReadingDirection.CONTINUOUS -> ReaderView.ReadingOrientation.VERTICAL
        }

        reader.readingDirection = when (GlobalPreference.readingDirection.get()) {
            GlobalPreference.ReadingDirection.RTL -> ReaderView.ReadingDirection.RTL
            else -> ReaderView.ReadingDirection.LTR
        }

        reader.adapter.isAreaInterpolationEnabled =
            GlobalPreference.isAreaInterpolationEnabled.get()
        reader.isPageIntervalEnabled = GlobalPreference.isPageIntervalEnabled.get()
        reader.useVolumeKey = GlobalPreference.useVolumeKey.get()
        reader.invertVolumeKey = GlobalPreference.invertVolumeKey.get()

        readerContainer.addView(reader)
        reader.isFocusable = true
        reader.isFocusableInTouchMode = true
        reader.requestFocus()

        reader.onRequestPrevChapter = { viewModel.moveToPrevChapter() }
        reader.onRequestNextChapter = { viewModel.moveToNextChapter() }
        reader.onRequestMenuVisibility = { viewModel.isMenuOpened.value ?: false }
        reader.onRequestMenu = {
            if (viewModel.readerState.value is ViewState.Content)
                viewModel.isMenuOpened.value = it
        }
        reader.onPageChanged = { viewModel.chapterPosition.value = it }
        reader.onPageLongClicked = { position, url ->
            if (GlobalPreference.isLongTapDialogEnabled.get())
                ReaderPageSheet(this, object : ReaderPageSheet.Listener {
                    override fun onRefresh() {
                        reader.refreshPage(position)
                    }

                    override fun onSave() {
                        val prefix = viewModel.makeImageFilenamePrefix()
                            ?: return toast(R.string.toast_chapter_not_loaded)
//                        saveImage(url, "$prefix-$position")
                    }

                    override fun onShare() {
                        val prefix = viewModel.makeImageFilenamePrefix()
                            ?: return toast(R.string.toast_chapter_not_loaded)
//                        lifecycleScope.shareImage(this, url, "$prefix-$position")
                    }
                }).show()
        }
        viewModel.chapterPointer.value?.let {
            it.startPage = viewModel.chapterPosition.value ?: it.startPage
        }
        viewModel.chapterPointer.value = viewModel.chapterPointer.value
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun ReaderContent() {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { readerContainer }
            )

            val name by viewModel.chapterName.observeAsState("")
            val title by viewModel.chapterTitle.observeAsState("")
            val position by viewModel.chapterPosition.observeAsState(0)
            val size by viewModel.chapterSize.observeAsState(0)

            val isMenuOpened by viewModel.isMenuOpened.observeAsState(false)

            val showInfoBar by GlobalPreference.showInfoBar.asFlow().collectAsState(
                GlobalPreference.showInfoBar.get()
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
                ReaderMenuBottom(size, position)
            }
        }
    }

    @Composable
    private fun ReaderMenuBottom(size: Int, position: Int) {
        val readingDirection = when (GlobalPreference.readingDirection.let {
            it.asFlow().collectAsState(it.get())
        }.value) {
            GlobalPreference.ReadingDirection.RTL -> LayoutDirection.Rtl
            else -> LayoutDirection.Ltr
        }

        CompositionLocalProvider(LocalLayoutDirection provides readingDirection) {
            Row(
                modifier = Modifier
                    .background(Color(0xAA000000))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isOnlyOneChapter by viewModel.isOnlyOneChapter.observeAsState(true)
                if (!isOnlyOneChapter)
                    IconButton(onClick = { viewModel.openPrevChapter() }) {
                        Icon(
                            if (LocalLayoutDirection.current == LayoutDirection.Ltr)
                                Icons.Filled.SkipPrevious else Icons.Filled.SkipNext,
                            "prev", tint = Color.White
                        )
                    }

                if (size > 1) {
                    var sliderPosition by remember {
                        mutableStateOf(position.toFloat() / size.minus(1))
                    }

                    fun sliderPositionToPage() =
                        (sliderPosition * size).toInt().coerceIn(0, size - 1)

                    Text(
                        modifier = Modifier.width(32.dp),
                        text = sliderPositionToPage().plus(1).toString(),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Slider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp, end = 8.dp),
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        onValueChangeFinished = { reader.setPage(sliderPositionToPage()) }
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
