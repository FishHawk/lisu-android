package com.fishhawk.driftinglibraryandroid.ui.reader.reader

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.makeToast
import com.fishhawk.driftinglibraryandroid.ui.reader.*
import com.fishhawk.driftinglibraryandroid.util.FileUtil
import kotlinx.coroutines.launch
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
                ReaderPageSheet(
                    requireContext(), position,
                    onRefreshed = { refreshImage(position) },
                    onSaved = { lifecycleScope.launch { saveImage(position, url) } },
                    onShared = { lifecycleScope.launch { shareImage(url) } }
                ).show()
        }
    }

    private fun setupMenuLayout() {
        binding.menuLayout.setOnClickListener { viewModel.isMenuVisible.value = false }

        binding.settingButton.setOnClickListener { ReaderSettingsSheet(
            requireContext()
        ).show() }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                binding.reader.setPage(seek.progress)
            }
        })
    }

//    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        val useVolumeKey = SettingsHelper.useVolumeKey.getValueDirectly()
//        return when (event.keyCode) {
//            KeyEvent.KEYCODE_VOLUME_UP -> useVolumeKey.also { if (it) binding.reader.gotoPrevPage() }
//            KeyEvent.KEYCODE_VOLUME_DOWN -> useVolumeKey.also { if (it) binding.reader.gotoNextPage() }
//            else -> super.dispatchKeyEvent(event)
//        }
//    }

    private fun openPrevChapter() {
        if (viewModel.isLoading) return
        if (!viewModel.openPrevChapter()) binding.root.makeToast(getString(R.string.reader_no_prev_chapter_hint))
    }

    private fun openNextChapter() {
        if (viewModel.isLoading) return
        if (!viewModel.openNextChapter()) binding.root.makeToast(getString(R.string.reader_no_next_chapter_hint))
    }


    fun refreshImage(page: Int) {
        binding.reader.refreshPage(page)
    }

    suspend fun shareImage(url: String) {
        val file = FileUtil.downloadImage(requireContext(), url)

        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireActivity().packageName}.fileprovider", file
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        startActivity(Intent.createChooser(shareIntent, "Share image"))
    }

    suspend fun saveImage(page: Int, url: String) {
        val prefix = viewModel.makeImageFilenamePrefix()
        if (prefix == null) {
            binding.root.makeToast("Chapter not open")
        } else {
            val filename = "$prefix-$page"
            val uri = FileUtil.createImageInGallery(requireContext(), filename)
            if (uri == null) {
                binding.root.makeToast("Image already exist")
            } else {
                val context = requireContext()
                lifecycleScope.launch {
                    try {
                        FileUtil.saveImage(context, url, uri)
                        binding.root.makeToast("Image saved")
                    } catch (e: Throwable) {
                        binding.root.makeToast(e.message ?: "Unknown error")
                    }
                }
            }
        }
    }
}
