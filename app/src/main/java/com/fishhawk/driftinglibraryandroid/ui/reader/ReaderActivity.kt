package com.fishhawk.driftinglibraryandroid.ui.reader

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.databinding.ActivityReaderBinding
import com.fishhawk.driftinglibraryandroid.ui.activity.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.base.EventObserver
import com.fishhawk.driftinglibraryandroid.ui.base.feedback
import com.fishhawk.driftinglibraryandroid.ui.base.toast
import com.fishhawk.driftinglibraryandroid.ui.reader.viewer.*
import com.fishhawk.driftinglibraryandroid.widget.SimpleAnimationListener
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class ReaderActivity : BaseActivity() {
    val viewModel: ReaderViewModel by viewModels()
    lateinit var binding: ActivityReaderBinding
    lateinit var reader: ReaderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

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


        viewModel.feedback.observe(this, EventObserver { feedback(it) })

        viewModel.readerState.observe(this) {
            if (it is ViewState.Error) reader.viewState = it
        }

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
                reader.viewState = pointer.currChapter.state

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
        viewModel.isOnlyOneChapter.observe(this) {
            binding.buttonPrevChapter.isVisible = !it
            binding.buttonNextChapter.isVisible = !it
        }

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

        initializeOverlay()
        initializeMenu()
    }

    override fun onPause() {
        super.onPause()
        runBlocking {
            viewModel.updateReadingHistory()
        }
    }

    private fun initializeReader() {
        binding.readerContainer.removeAllViews()

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

        binding.readerContainer.addView(reader)
        reader.isFocusable = true
        reader.isFocusableInTouchMode = true
        reader.requestFocus()

        reader.onRequestPrevChapter = { viewModel.moveToPrevChapter() }
        reader.onRequestNextChapter = { viewModel.moveToNextChapter() }
        reader.onRequestMenuVisibility = { binding.menuLayout.isVisible }
        reader.onRequestMenu = {
            if (viewModel.readerState.value is ViewState.Content)
                setMenuLayoutVisibility(it)
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
        reader.onRetry = { viewModel.refreshReader() }
        viewModel.chapterPointer.value?.let {
            it.startPage = viewModel.chapterPosition.value ?: it.startPage
        }
        viewModel.chapterPointer.value = viewModel.chapterPointer.value
    }

    private fun initializeOverlay() {
        val colorFilterValue = combine(
            GlobalPreference.colorFilterH.asFlow(),
            GlobalPreference.colorFilterS.asFlow(),
            GlobalPreference.colorFilterL.asFlow(),
            GlobalPreference.colorFilterA.asFlow()
        ) { h, s, l, a ->
            Color.HSVToColor(
                a.coerceIn(0, 255),
                floatArrayOf(
                    h.coerceIn(0, 360).toFloat(),
                    s.coerceIn(0, 100).toFloat() / 100f,
                    l.coerceIn(0, 100).toFloat() / 100f
                )
            )
        }

        combine(
            GlobalPreference.colorFilter.asFlow(),
            GlobalPreference.colorFilterMode.asFlow(),
            colorFilterValue
        ) { isEnabled, mode, color ->
            if (isEnabled) {
                binding.colorOverlay.setFilterColor(color, mode)
                binding.colorOverlay.isVisible = true
            } else {
                binding.colorOverlay.isVisible = false
            }
        }.launchIn(lifecycleScope)
    }

    @SuppressLint("SetTextI18n")
    private fun initializeMenu() {
        GlobalPreference.showInfoBar.asFlow()
            .onEach { if (!binding.menuLayout.isVisible) binding.infoBar.isVisible = it }
            .launchIn(lifecycleScope)

        combine(
            viewModel.chapterName.asFlow(),
            viewModel.chapterTitle.asFlow(),
            viewModel.chapterPosition.asFlow(),
            viewModel.chapterSize.asFlow()
        ) { name, title, position, size ->
            binding.infoBar.text =
                if (size != 0) "$name $title ${position + 1}/$size"
                else "$name $title"
            binding.title.text = "$name $title"

            binding.chapterPositionLabel.isVisible = size != 0
            binding.chapterPositionLabel.text = (position + 1).toString()

            binding.chapterSizeLabel.isVisible = size != 0
            binding.chapterSizeLabel.text = size.toString()

            binding.chapterProgress.isEnabled = size > 1
            binding.chapterProgress.max = size - 1
            binding.chapterProgress.progress = position
        }.launchIn(lifecycleScope)

        binding.backButton.setOnClickListener { finish() }
        binding.settingButton.setOnClickListener { ReaderSettingsSheet(this).show() }
        binding.overlayButton.setOnClickListener {
            ReaderOverlaySheet(this, lifecycleScope)
                .apply {
                    setOnDismissListener { setMenuLayoutVisibility(true) }
                    setMenuLayoutVisibility(false)
                    window?.setDimAmount(0f)
                }
                .show()
        }

        binding.chapterProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                reader.setPage(seek.progress)
            }
        })

        binding.buttonPrevChapter.setOnClickListener { viewModel.openPrevChapter() }
        binding.buttonNextChapter.setOnClickListener { viewModel.openNextChapter() }


        GlobalPreference.readingDirection.asFlow()
            .onEach {
                binding.menuBottomLayout.layoutDirection =
                    if (it == GlobalPreference.ReadingDirection.RTL) View.LAYOUT_DIRECTION_RTL
                    else View.LAYOUT_DIRECTION_LTR
            }
            .launchIn(lifecycleScope)
    }

    private fun setMenuLayoutVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.menuLayout.isVisible = true
            binding.infoBar.isVisible = false

            val topAnim =
                AnimationUtils.loadAnimation(this, R.anim.enter_from_top)
            val bottomAnim =
                AnimationUtils.loadAnimation(this, R.anim.enter_from_bottom)
            binding.menuTopLayout.startAnimation(topAnim)
            binding.menuBottomLayout.startAnimation(bottomAnim)
        } else {
            if (GlobalPreference.showInfoBar.get()) binding.infoBar.isVisible = true

            val topAnim =
                AnimationUtils.loadAnimation(this, R.anim.exit_to_top)
            val bottomAnim =
                AnimationUtils.loadAnimation(this, R.anim.exit_to_bottom)
            topAnim.setAnimationListener(
                object : SimpleAnimationListener() {
                    override fun onAnimationEnd(animation: Animation) {
                        binding.menuLayout.isVisible = false
                    }
                }
            )
            binding.menuTopLayout.startAnimation(topAnim)
            binding.menuBottomLayout.startAnimation(bottomAnim)
        }
    }
}
