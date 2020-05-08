package com.fishhawk.driftinglibraryandroid.reader

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.databinding.FragmentReaderBinding
import com.fishhawk.driftinglibraryandroid.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.google.android.material.snackbar.Snackbar

class ReaderFragment : Fragment() {
    private lateinit var viewModel: GalleryViewModel
    private lateinit var binding: FragmentReaderBinding
    private var isLoadingChapter: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run { ViewModelProvider(this)[GalleryViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
    }

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

        binding.readerLayout.apply {
            onClickLeftAreaListener = {
                when (binding.contentHorizontal.layoutDirection) {
                    ViewPager2.LAYOUT_DIRECTION_LTR -> toPrevPage()
                    ViewPager2.LAYOUT_DIRECTION_RTL -> toNextPage()
                }
            }
            onClickRightAreaListener = {
                when (binding.contentHorizontal.layoutDirection) {
                    ViewPager2.LAYOUT_DIRECTION_LTR -> toNextPage()
                    ViewPager2.LAYOUT_DIRECTION_RTL -> toPrevPage()
                }
            }
            onClickCenterAreaListener = { viewModel.isMenuVisible.value = true }
        }

        binding.menuLayout.setOnClickListener { viewModel.isMenuVisible.value = false }

        binding.radioGroupDirection.setOnCheckedChangeListener { _, checkedId ->
            viewModel.readingDirection.value = when (checkedId) {
                R.id.radio_left_to_right -> SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT
                R.id.radio_right_to_left -> SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT
                R.id.radio_vertical -> SettingsHelper.READING_DIRECTION_VERTICAL
                else -> SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT
            }
        }

        binding.contentHorizontal.apply {
            offscreenPageLimit = 5
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                private var isEdge: Boolean = true

                override fun onPageScrollStateChanged(state: Int) {
                    when (state) {
                        ViewPager2.SCROLL_STATE_IDLE -> {
                            if (!isEdge && !isLoadingChapter) {
                                if (currentItem == 0) toPrevChapter()
                                else if (currentItem == adapter!!.itemCount - 1) toNextChapter()
                            }
                        }
                        ViewPager2.SCROLL_STATE_DRAGGING -> isEdge = false
                        ViewPager2.SCROLL_STATE_SETTLING -> isEdge = true
                    }
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    viewModel.chapterPosition.value = position
                }
            })
        }

        binding.seekBar.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seek: SeekBar) {}
                override fun onStopTrackingTouch(seek: SeekBar) {
                    binding.contentHorizontal.setCurrentItem(seek.progress, false)
                }
            })
        }

        viewModel.openedChapterContent.observe(viewLifecycleOwner, Observer { result ->
            isLoadingChapter = false
            when (result) {
                is Result.Success -> {
                    binding.contentHorizontal.apply {
                        adapter = ImageHorizontalListAdapter(context, result.data)
                        if (!viewModel.fromStart)
                            binding.contentHorizontal.setCurrentItem(result.data.size - 1, false)
                    }

                    binding.contentVertical.apply {
                        adapter = ImageVerticalListAdapter(context, result.data)
                    }
                    viewModel.chapterPosition.value = binding.contentHorizontal.currentItem
                }
                is Result.Error -> makeSnakeBar(getString(R.string.image_unknown_error_hint))
            }
        })

        viewModel.readingDirection.observe(viewLifecycleOwner, Observer {
            SettingsHelper.setReadingDirection(it)
        })

        viewModel.layoutDirection.observe(viewLifecycleOwner, Observer {
            binding.contentHorizontal.apply {
                viewModel.layoutDirection.value?.let { layoutDirection = it }
                adapter?.notifyDataSetChanged()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onStop() {
        super.onStop()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }


    private fun toPrevChapter() {
        if (!viewModel.openPrevChapter()) makeSnakeBar(getString(R.string.reader_no_prev_chapter_hint))
        else isLoadingChapter = true
    }

    private fun toNextChapter() {
        if (!viewModel.openNextChapter()) makeSnakeBar(getString(R.string.reader_no_next_chapter_hint))
        else isLoadingChapter = true
    }

    private fun toPrevPage() {
        binding.contentHorizontal.apply {
            if (adapter != null) {
                if (currentItem == 0) toPrevChapter()
                else setCurrentItem(currentItem - 1, false)
            }
        }
    }

    private fun toNextPage() {
        binding.contentHorizontal.apply {
            if (adapter != null) {
                if (currentItem == adapter!!.itemCount - 1) toNextChapter()
                else setCurrentItem(currentItem + 1, false)
            }
        }
    }

    private fun makeSnakeBar(content: String) {
        view?.let { Snackbar.make(it, content, Snackbar.LENGTH_SHORT).show() }
    }
}