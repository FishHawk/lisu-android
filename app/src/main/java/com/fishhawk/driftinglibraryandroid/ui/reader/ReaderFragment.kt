package com.fishhawk.driftinglibraryandroid.ui.reader

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderFragmentBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.ui.ReaderViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.activity.ReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.base.makeToast
import com.fishhawk.driftinglibraryandroid.ui.base.saveImage
import com.fishhawk.driftinglibraryandroid.ui.base.shareImage
import com.fishhawk.driftinglibraryandroid.widget.SimpleAnimationListener
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

class ReaderFragment : Fragment() {
    val viewModel: ReaderViewModel by viewModels {
        val arguments = requireActivity().intent.extras!!

        val id = arguments.getString("id")!!
        val providerId = arguments.getString("providerId")
        val collectionIndex = arguments.getInt("collectionIndex")
        val chapterIndex = arguments.getInt("chapterIndex")
        val pageIndex = arguments.getInt("pageIndex")
        val application = requireActivity().application as MainApplication
        ReaderViewModelFactory(
            id,
            providerId,
            collectionIndex,
            chapterIndex,
            pageIndex,
            application
        )
    }
    lateinit var binding: ReaderFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ReaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeReader()
        initializeOverlay()
        initializeMenu()

        (requireActivity() as ReaderActivity).listener = object : ReaderActivity.OnVolumeKeyEvent {
            override fun onVolumeUp() = binding.reader.gotoPrevPage()
            override fun onVolumeDown() = binding.reader.gotoNextPage()
        }
    }

    override fun onPause() {
        super.onPause()
        runBlocking {
            viewModel.updateReadingHistory()
        }
    }

    private fun initializeReader() {
        binding.reader.onRequestPrevChapter = { openPrevChapter() }
        binding.reader.onRequestNextChapter = { openNextChapter() }
        binding.reader.onRequestMenu = { setMenuLayoutVisibility(it) }
        binding.reader.onScrolled = { viewModel.chapterPosition.value = it }
        binding.reader.onPageLongClicked = { position, url ->
            if (GlobalPreference.longTapDialog.get())
                ReaderPageSheet(requireContext(), object : ReaderPageSheet.Listener {
                    override fun onRefresh() {
                        binding.reader.refreshPage(position)
                    }

                    override fun onSave() {
                        val prefix = viewModel.makeImageFilenamePrefix()
                            ?: return makeToast(R.string.toast_chapter_not_loaded)
                        saveImage(url, "$prefix-$position")
                    }

                    override fun onShare() {
                        val prefix = viewModel.makeImageFilenamePrefix()
                            ?: return makeToast(R.string.toast_chapter_not_loaded)
                        shareImage(url, "$prefix-$position")
                    }
                }).show()
        }

        viewModel.readerContent.observe(viewLifecycleOwner) { result ->
            binding.title.text = viewModel.mangaTitle
            when (result) {
                is Result.Success -> {
                    val content = result.data
                    binding.reader.setContent(content)
                    viewModel.chapterPosition.value?.let { binding.reader.setPage(it) }
                }
            }
        }

        GlobalPreference.readingDirection.asFlow()
            .onEach {
                val preset = when (it) {
                    GlobalPreference.ReadingDirection.LTR -> ReaderView.Preset.LTR
                    GlobalPreference.ReadingDirection.RTL -> ReaderView.Preset.RTL
                    GlobalPreference.ReadingDirection.VERTICAL -> ReaderView.Preset.VERTICAL
                }
                binding.reader.applyPreset(preset)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
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
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun initializeMenu() {
        combine(
            viewModel.chapterName.asFlow(),
            viewModel.chapterTitle.asFlow(),
            viewModel.chapterPosition.asFlow(),
            viewModel.chapterSize.asFlow()
        ) { name, title, position, size ->
            val indicator = "$name $title ${position + 1}/$size"
            binding.readerIndicator.text = indicator
            binding.chapterPositionLabel.text = (position + 1).toString()
            binding.chapterSizeLabel.text = size.toString()
            binding.chapterProgress.max = size - 1
            binding.chapterProgress.progress = position
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.settingButton.setOnClickListener { ReaderSettingsSheet(requireContext()).show() }
        binding.overlayButton.setOnClickListener {
            ReaderOverlaySheet(requireContext(), viewLifecycleOwner.lifecycleScope)
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
                binding.reader.setPage(seek.progress)
            }
        })

        binding.buttonPrevChapter.setOnClickListener { openPrevChapter() }
        binding.buttonNextChapter.setOnClickListener { openNextChapter() }


        GlobalPreference.readingDirection.asFlow()
            .onEach {
                binding.menuBottomLayout.layoutDirection =
                    if (it == GlobalPreference.ReadingDirection.RTL) View.LAYOUT_DIRECTION_RTL
                    else View.LAYOUT_DIRECTION_LTR
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setMenuLayoutVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.menuLayout.isVisible = true
            binding.readerIndicator.isVisible = false

            val topAnim =
                AnimationUtils.loadAnimation(requireContext(), R.anim.enter_from_top)
            val bottomAnim =
                AnimationUtils.loadAnimation(requireContext(), R.anim.enter_from_bottom)
            binding.menuTopLayout.startAnimation(topAnim)
            binding.menuBottomLayout.startAnimation(bottomAnim)
        } else {
            val topAnim =
                AnimationUtils.loadAnimation(requireContext(), R.anim.exit_to_top)
            val bottomAnim =
                AnimationUtils.loadAnimation(requireContext(), R.anim.exit_to_bottom)
            topAnim.setAnimationListener(
                object : SimpleAnimationListener() {
                    override fun onAnimationEnd(animation: Animation) {
                        binding.menuLayout.isVisible = false
                        binding.readerIndicator.isVisible = true
                    }
                }
            )
            binding.menuTopLayout.startAnimation(topAnim)
            binding.menuBottomLayout.startAnimation(bottomAnim)
        }
    }

    private fun openPrevChapter() {
        if (viewModel.isLoading) return
        if (!viewModel.openPrevChapter()) makeToast(R.string.toast_no_prev_chapter)
    }

    private fun openNextChapter() {
        if (viewModel.isLoading) return
        if (!viewModel.openNextChapter()) makeToast(R.string.toast_no_next_chapter)
    }
}
