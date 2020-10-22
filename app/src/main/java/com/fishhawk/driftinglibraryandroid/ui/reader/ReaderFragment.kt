package com.fishhawk.driftinglibraryandroid.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.makeToast
import com.fishhawk.driftinglibraryandroid.ui.activity.ReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.ReaderViewModelFactory
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
    ): View? {
        binding = ReaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupReaderView()
        setupMenuLayout()

        SettingsHelper.readingDirection.observe(viewLifecycleOwner, Observer {
            binding.reader.mode = when (it) {
                SettingsHelper.ReadingDirection.LTR -> ReaderView.Mode.LTR
                SettingsHelper.ReadingDirection.RTL -> ReaderView.Mode.RTL
                SettingsHelper.ReadingDirection.VERTICAL -> ReaderView.Mode.VERTICAL
                else -> ReaderView.Mode.LTR
            }
        })

        viewModel.readerContent.observe(viewLifecycleOwner, Observer { result ->
            binding.title.text = viewModel.mangaTitle
            when (result) {
                is Result.Success -> {
                    val content = result.data
                    binding.reader.setContent(content)
                    viewModel.chapterPosition.value?.let { binding.reader.setPage(it) }
                }
            }
        })

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

    private fun setupReaderView() {
        binding.reader.onRequestPrevChapter = { openPrevChapter() }
        binding.reader.onRequestNextChapter = { openNextChapter() }
        binding.reader.onRequestMenu = { viewModel.isMenuVisible.value = true }
        binding.reader.onScrolled = { viewModel.chapterPosition.value = it }
        binding.reader.onPageLongClicked = { position, url ->
            if (SettingsHelper.longTapDialog.getValueDirectly())
                ReaderPageSheet(this, position, url).show()
        }
    }

    private fun setupMenuLayout() {
        binding.menuLayout.setOnClickListener { viewModel.isMenuVisible.value = false }

        binding.settingButton.setOnClickListener {
            ReaderSettingsSheet(
                requireContext()
            ).show()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                binding.reader.setPage(seek.progress)
            }
        })
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
