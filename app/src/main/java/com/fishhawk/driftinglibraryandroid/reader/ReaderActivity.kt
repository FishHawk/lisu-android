package com.fishhawk.driftinglibraryandroid.reader

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderActivityBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.getThemeResId
import com.fishhawk.driftinglibraryandroid.util.makeSnackBar
import kotlinx.coroutines.runBlocking

class ReaderActivity : AppCompatActivity() {
    private val viewModel: ReaderViewModel by viewModels {
        val arguments = intent.extras!!

        val id = arguments.getString("id")!!
        val collectionIndex = arguments.getInt("collectionIndex")
        val chapterIndex = arguments.getInt("chapterIndex")
        val pageIndex = arguments.getInt("pageIndex")
        val application = applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        val readingHistoryRepository = application.readingHistoryRepository
        ReaderViewModelFactory(
            id,
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

        when (SettingsHelper.theme.getValueDirectly()) {
            SettingsHelper.THEME_LIGHT -> setTheme(R.style.AppTheme_NoActionBar)
            SettingsHelper.THEME_DARK -> setTheme(R.style.AppTheme_Dark_NoActionBar)
        }

        setupSystemUiVisibility()

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
            viewModel.startPage = viewModel.chapterPosition.value!!
            viewModel.readerContent.value = viewModel.readerContent.value
        })

        viewModel.readerContent.observe(this, Observer { result ->
            when (result) {
                is Result.Success -> {
                    val content = result.data
                    if (viewModel.isReaderDirectionEqualVertical.value!!)
                        binding.verticalReader.apply {
                            adapter = ImageVerticalListAdapter(context, content)
                        }
                    else
                        binding.horizontalReader.apply {
                            adapter = ImageHorizontalListAdapter(context, content)
                        }
                    setReaderPage(viewModel.startPage)
                    viewModel.chapterPosition.value = viewModel.startPage
                    viewModel.chapterSize.value = content.size
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        runBlocking {
            viewModel.updateReadingHistory()
        }
    }

    private fun setupSystemUiVisibility() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getThemeResId() == R.style.AppTheme_NoActionBar)
                window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
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

        binding.radioGroupDirection.setOnCheckedChangeListener { _, checkedId ->
            val direction = when (checkedId) {
                R.id.radio_left_to_right -> SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT
                R.id.radio_right_to_left -> SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT
                R.id.radio_vertical -> SettingsHelper.READING_DIRECTION_VERTICAL
                else -> SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT
            }
            viewModel.readingDirection.setValue(direction)
        }

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
        if (!viewModel.openPrevChapter()) binding.root.makeSnackBar(getString(R.string.reader_no_prev_chapter_hint))
    }

    private fun openNextChapter() {
        if (!viewModel.openNextChapter()) binding.root.makeSnackBar(getString(R.string.reader_no_next_chapter_hint))
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
}
