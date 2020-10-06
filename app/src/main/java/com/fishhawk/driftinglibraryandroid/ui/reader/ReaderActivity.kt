package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderActivityBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.makeToast
import com.fishhawk.driftinglibraryandroid.ui.extension.setupFullScreen
import com.fishhawk.driftinglibraryandroid.ui.extension.setupThemeWithTranslucentStatus
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.FileUtil
import kotlinx.coroutines.runBlocking

class ReaderActivity : AppCompatActivity() {
    val viewModel: ReaderViewModel by viewModels {
        val arguments = intent.extras!!

        val id = arguments.getString("id")!!
        val providerId = arguments.getString("providerId")
        val collectionIndex = arguments.getInt("collectionIndex")
        val chapterIndex = arguments.getInt("chapterIndex")
        val pageIndex = arguments.getInt("pageIndex")
        val application = applicationContext as MainApplication
        ReaderViewModelFactory(
            id,
            providerId,
            collectionIndex,
            chapterIndex,
            pageIndex,
            application
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

        SettingsHelper.screenOrientation.observe(this, Observer {
            val newOrientation = when (it) {
                SettingsHelper.ScreenOrientation.DEFAULT -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                SettingsHelper.ScreenOrientation.LOCK -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
                SettingsHelper.ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                SettingsHelper.ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            if (newOrientation != requestedOrientation) {
                requestedOrientation = newOrientation
            }
        })

        SettingsHelper.keepScreenOn.observe(this, Observer {
            val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
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
        binding.reader.onPageLongClicked = { position, url ->
            if (SettingsHelper.longTapDialog.getValueDirectly())
                ReaderPageSheet(this, position, url).show()
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

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val useVolumeKey = SettingsHelper.useVolumeKey.getValueDirectly()
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> useVolumeKey.also { if (it) binding.reader.gotoPrevPage() }
            KeyEvent.KEYCODE_VOLUME_DOWN -> useVolumeKey.also { if (it) binding.reader.gotoNextPage() }
            else -> super.dispatchKeyEvent(event)
        }
    }

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

    suspend fun saveImage(page: Int, url: String) {
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
