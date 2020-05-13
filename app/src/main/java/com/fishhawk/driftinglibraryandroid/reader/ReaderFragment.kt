package com.fishhawk.driftinglibraryandroid.reader

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.SeekBar
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.Util
import com.fishhawk.driftinglibraryandroid.databinding.FragmentReaderBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.google.android.material.snackbar.Snackbar
import java.lang.reflect.Method

class ReaderFragment : Fragment() {
    private val viewModel: ReaderViewModel by viewModels {
        val detail = arguments?.getParcelable<MangaDetail>("detail")!!
        val collectionIndex: Int = arguments?.getInt("collectionIndex") ?: 0
        val chapterIndex: Int = arguments?.getInt("chapterIndex") ?: 0
        ReaderViewModelFactory(detail, collectionIndex, chapterIndex)
    }
    private lateinit var binding: FragmentReaderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        setupReaderLayout()
        setupMenuLayout()
        setupHorizontalReader()
        setupVerticalReader()

        viewModel.isReaderDirectionEqualRightToLeft.observe(viewLifecycleOwner, Observer {
            binding.horizontalReader.layoutDirection =
                if (it) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            viewModel.startPage = viewModel.chapterPosition.value!!
            viewModel.readerContent.value = viewModel.readerContent.value
        })

        viewModel.readerContent.observe(viewLifecycleOwner, Observer { content ->
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
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.fitsSystemWindows = false
        activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
            ?.setStatusBarBackgroundColor(Color.TRANSPARENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val themeId = Util.extractThemeResId(context!!)
            if (themeId as Int == R.style.AppTheme_NoActionBar)
                activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun onStop() {
        super.onStop()
        val statusBarColor = TypedValue()
        context?.theme?.resolveAttribute(R.attr.colorPrimaryDark, statusBarColor, true)

        (activity as? AppCompatActivity)?.supportActionBar?.show()
        activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.fitsSystemWindows = true
        activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
            ?.setStatusBarBackgroundColor(statusBarColor.data)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val wrapper: Class<*> = Context::class.java
            val method: Method = wrapper.getMethod("getThemeResId")
            method.isAccessible = true
            if (method.invoke(context) as Int == R.style.AppTheme_NoActionBar)
                activity?.window?.decorView?.systemUiVisibility = 0
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
        if (!viewModel.openPrevChapter()) makeSnakeBar(getString(R.string.reader_no_prev_chapter_hint))
    }

    private fun openNextChapter() {
        if (!viewModel.openNextChapter()) makeSnakeBar(getString(R.string.reader_no_next_chapter_hint))
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

    private fun makeSnakeBar(content: String) {
        view?.let { Snackbar.make(it, content, Snackbar.LENGTH_SHORT).show() }
    }
}