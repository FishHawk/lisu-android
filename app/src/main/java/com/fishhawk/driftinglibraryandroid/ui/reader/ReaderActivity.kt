package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.DialogChapterImageBinding
import com.fishhawk.driftinglibraryandroid.databinding.ReaderActivityBinding
import com.fishhawk.driftinglibraryandroid.extension.makeToast
import com.fishhawk.driftinglibraryandroid.extension.setupFullScreen
import com.fishhawk.driftinglibraryandroid.extension.setupThemeWithTranslucentStatus
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.DiskUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class ReaderActivity : AppCompatActivity() {
    private val viewModel: ReaderViewModel by viewModels {
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
    private lateinit var binding: ReaderActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupThemeWithTranslucentStatus()
        setupFullScreen()

        binding = ReaderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupReaderLayout()
        setupMenuLayout()
        setupHorizontalReader()
        setupVerticalReader()

        viewModel.isReaderDirectionEqualRightToLeft.observe(this, Observer {
            binding.horizontalReader.layoutDirection =
                if (it) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            viewModel.readerContent.value = viewModel.readerContent.value
        })


        viewModel.readerContent.observe(this, Observer { result ->
            when (result) {
                is Result.Success -> {
                    val content = result.data
                    if (viewModel.isReaderDirectionEqualVertical.value!!)
                        binding.verticalReader.apply {
                            adapter = ImageVerticalListAdapter(context, content).apply {
                                onCardLongClicked = { page, url ->
                                    createChapterImageActionDialog(page, url)
                                }
                            }
                        }
                    else
                        binding.horizontalReader.apply {
                            adapter = ImageHorizontalListAdapter(context, content).apply {
                                onCardLongClicked = { page, url ->
                                    createChapterImageActionDialog(page, url)
                                }
                            }
                        }
                    viewModel.chapterPosition.value?.let { setReaderPage(it) }
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

    private fun setupReaderLayout() {
        val gotoPrevPage = {
            if (viewModel.chapterPosition.value!! == 0) openPrevChapter()
            else setHorizontalReaderPage(viewModel.chapterPosition.value!! - 1)
        }
        val gotoNextPage = {
            if (viewModel.chapterPosition.value!! == viewModel.chapterSize.value!! - 1) openNextChapter()
            else setHorizontalReaderPage(viewModel.chapterPosition.value!! + 1)
        }

        binding.readerLayout.onClickLeftAreaListener = {
            when (viewModel.readingDirection.value) {
                SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT -> gotoPrevPage()
                SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT -> gotoNextPage()
                SettingsHelper.READING_DIRECTION_VERTICAL -> viewModel.isMenuVisible.value = true
            }
        }
        binding.readerLayout.onClickRightAreaListener = {
            when (viewModel.readingDirection.value) {
                SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT -> gotoNextPage()
                SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT -> gotoPrevPage()
                SettingsHelper.READING_DIRECTION_VERTICAL -> viewModel.isMenuVisible.value = true
            }
        }
        binding.readerLayout.onClickCenterAreaListener = { viewModel.isMenuVisible.value = true }

    }

    private fun setupMenuLayout() {
        binding.menuLayout.setOnClickListener { viewModel.isMenuVisible.value = false }

        binding.buttonReadingDirection.setOnClickListener {
            val direction = when (viewModel.readingDirection.value) {
                SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT ->
                    SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT
                SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT ->
                    SettingsHelper.READING_DIRECTION_VERTICAL
                SettingsHelper.READING_DIRECTION_VERTICAL ->
                    SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT
                else ->
                    SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT
            }
            viewModel.readingDirection.setValue(direction)
        }

        viewModel.readingDirection.observe(this, Observer {
            when (it) {
                SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT -> {
                    binding.buttonReadingDirection.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
                    binding.hintReadingDirection.setText(R.string.hint_reading_direction_left_to_right)
                }
                SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT -> {
                    binding.buttonReadingDirection.setImageResource(R.drawable.ic_baseline_keyboard_arrow_left_24)
                    binding.hintReadingDirection.setText(R.string.hint_reading_direction_right_to_Left)
                }
                SettingsHelper.READING_DIRECTION_VERTICAL -> {
                    binding.buttonReadingDirection.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                    binding.hintReadingDirection.setText(R.string.hint_reading_direction_vertical)
                }
            }
        })

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) = setReaderPage(seek.progress)
        })
    }

    private fun setupHorizontalReader() {
        binding.horizontalReader.apply {
            offscreenPageLimit = 5
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                private var isEdge: Boolean = true

                override fun onPageScrollStateChanged(state: Int) {
                    when (state) {
                        ViewPager2.SCROLL_STATE_IDLE -> {
                            if (!isEdge && !viewModel.isLoading.value!!) {
                                if (currentItem == 0) openPrevChapter()
                                else if (currentItem == adapter!!.itemCount - 1) openNextChapter()
                            }
                        }
                        ViewPager2.SCROLL_STATE_DRAGGING -> isEdge = false
                        ViewPager2.SCROLL_STATE_SETTLING -> isEdge = true
                    }
                }

                override fun onPageSelected(position: Int) {
                    viewModel.chapterPosition.value = position
                }
            })
        }
    }

    private fun setupVerticalReader() {
        binding.verticalReader.apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var isTopReached: Boolean = false
                private var isBottomReached: Boolean = false

                override fun onScrollStateChanged(recyclerView: RecyclerView, state: Int) {
                    when (state) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            isTopReached = !recyclerView.canScrollVertically(-1)
                            isBottomReached = !recyclerView.canScrollVertically(1)
                        }
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                            isTopReached = isTopReached && !recyclerView.canScrollVertically(-1)
                            isBottomReached =
                                isBottomReached && !recyclerView.canScrollVertically(1)
                        }
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            isTopReached = isTopReached && !recyclerView.canScrollVertically(-1)
                            isBottomReached =
                                isBottomReached && !recyclerView.canScrollVertically(1)

                            if (!viewModel.isLoading.value!!) {
                                if (isTopReached) openPrevChapter()
                                if (isBottomReached) openNextChapter()
                            }
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    viewModel.chapterPosition.value =
                        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                }
            })
        }
    }

    private fun openPrevChapter() {
        if (!viewModel.openPrevChapter()) binding.root.makeToast(getString(R.string.reader_no_prev_chapter_hint))
    }

    private fun openNextChapter() {
        if (!viewModel.openNextChapter()) binding.root.makeToast(getString(R.string.reader_no_next_chapter_hint))
    }

    private fun setHorizontalReaderPage(position: Int) {
        binding.horizontalReader.setCurrentItem(position, false)
    }

    private fun setVerticalReaderPage(position: Int) {
        (binding.verticalReader.layoutManager as LinearLayoutManager)
            .scrollToPositionWithOffset(position, 0)
    }

    private fun setReaderPage(position: Int) {
        if (viewModel.readingDirection.value == SettingsHelper.READING_DIRECTION_VERTICAL)
            setVerticalReaderPage(position)
        else setHorizontalReaderPage(position)
    }

    private fun createChapterImageActionDialog(page: Int, url: String) {
        val dialogBinding =
            DialogChapterImageBinding
                .inflate(LayoutInflater.from(this), null, false)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Page ${page + 1}")
            .setView(dialogBinding.root)
            .create()

        dialogBinding.refreshButton.setOnClickListener {
            dialog.dismiss()
            if (SettingsHelper.readingDirection.getValueDirectly() == SettingsHelper.READING_DIRECTION_VERTICAL)
                binding.verticalReader.adapter?.notifyItemChanged(page)
            else
                binding.horizontalReader.adapter?.notifyItemChanged(page)
        }
        dialogBinding.shareButton.setOnClickListener {
            dialog.dismiss()
            lifecycleScope.launch { shareImage(url) }
        }
        dialogBinding.saveButton.setOnClickListener {
            dialog.dismiss()
            lifecycleScope.launch { saveImage(page, url) }
        }
        dialog.show()
    }

    private suspend fun shareImage(url: String) {
        val activity = this
        val file = withContext(Dispatchers.IO) {
            Glide.with(activity)
                .downloadOnly()
                .load(url)
                .submit()
                .get()
        }

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
                DiskUtil.saveImage(this, url, "$prefix-$page")
                binding.root.makeToast("Image saved")
            } catch (e: Throwable) {
                binding.root.makeToast(e.message ?: "Unknown error")
            }
        }
    }
}
