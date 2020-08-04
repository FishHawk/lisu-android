package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderActivityBinding
import com.fishhawk.driftinglibraryandroid.extension.makeToast
import com.fishhawk.driftinglibraryandroid.extension.setupFullScreen
import com.fishhawk.driftinglibraryandroid.extension.setupThemeWithTranslucentStatus
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.FileUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ReaderActivity : AppCompatActivity() {
    val viewModel: ReaderViewModel by viewModels {
        val arguments = intent.extras!!

        val id = arguments.getString("id")!!
        val source = arguments.getString("source")
        val collectionIndex = arguments.getInt("collectionIndex")
        val chapterIndex = arguments.getInt("chapterIndex")
        val pageIndex = arguments.getInt("pageIndex")
        val application = applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        val readingHistoryRepository = application.readingHistoryRepository
        ReaderViewModelFactory(
            id,
            source,
            collectionIndex,
            chapterIndex,
            pageIndex,
            remoteLibraryRepository,
            readingHistoryRepository
        )
    }
    lateinit var binding: ReaderActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeWithTranslucentStatus()
        super.onCreate(savedInstanceState)
        setupFullScreen()

        binding = ReaderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupReaderView()
        setupMenuLayout()

        SettingsHelper.readingDirection.observe(this, Observer {
            binding.reader.mode = when (it) {
                SettingsHelper.ReadingDirection.LTR -> ReaderView.Mode.LTR
                SettingsHelper.ReadingDirection.RTL -> ReaderView.Mode.RTL
                SettingsHelper.ReadingDirection.VERTICAL -> ReaderView.Mode.VERTICAL
                else -> ReaderView.Mode.LTR
            }
        })

        viewModel.readerContent.observe(this, Observer { result ->
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

        binding.reader.adapter.apply {
            onImageShare = { page, url -> lifecycleScope.launch { shareImage(page, url) } }
            onImageSave = { page, url -> lifecycleScope.launch { saveImage(page, url) } }
        }
    }

    private fun setupMenuLayout() {
        binding.menuLayout.setOnClickListener { viewModel.isMenuVisible.value = false }

        binding.settingButton.setOnClickListener { ReaderSettingsSheet(this).show() }

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
        if (!viewModel.openPrevChapter()) binding.root.makeToast(getString(R.string.reader_no_prev_chapter_hint))
    }

    private fun openNextChapter() {
        if (viewModel.isLoading) return
        if (!viewModel.openNextChapter()) binding.root.makeToast(getString(R.string.reader_no_next_chapter_hint))
    }


    private suspend fun shareImage(page: Int, url: String) {
        val file = FileUtil.downloadImage(this, url)

        val uri = FileProvider.getUriForFile(
            this, "$packageName.fileprovider", file
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        startActivity(Intent.createChooser(shareIntent, "Share image"))
    }

    private suspend fun saveImage(page: Int, url: String) {
        val prefix = viewModel.makeImageFilenamePrefix()
        if (prefix == null) {
            binding.root.makeToast("Chapter not open")
        } else {
            try {
                FileUtil.saveImage(this, url, "$prefix-$page")
                binding.root.makeToast("Image saved")
            } catch (e: Throwable) {
                binding.root.makeToast(e.message ?: "Unknown error")
            }
        }
    }
}
