package com.fishhawk.driftinglibraryandroid.reader

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.FragmentReaderBinding
import com.fishhawk.driftinglibraryandroid.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_library.view.*

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
            onClickCenterAreaListener = {
                binding.menuLayout.visibility = View.VISIBLE
                binding.hint.visibility = View.INVISIBLE
            }
        }

        when (SettingsHelper.getReadingDirection()) {
            SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT -> setReadingDirectionLeftToRight()
            SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT -> setReadingDirectionRightToLeft()
            SettingsHelper.READING_DIRECTION_VERTICAL -> setReadingDirectionVertical()
        }
        binding.radioGroupDirection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_left_to_right -> setReadingDirectionLeftToRight()
                R.id.radio_right_to_left -> setReadingDirectionRightToLeft()
                R.id.radio_vertical -> setReadingDirectionVertical()
            }
        }

        binding.menuLayout.setOnClickListener {
            binding.menuLayout.visibility = View.GONE
            binding.hint.visibility = View.VISIBLE
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
                    binding.seekBar.progress = position
                    binding.hint.text =
                        "${viewModel.getSelectedChapterTitle()} ${binding.contentHorizontal.currentItem + 1}/${binding.contentHorizontal.adapter?.itemCount}"
                }
            })
        }
        binding.contentVertical.apply {
            layoutManager = LinearLayoutManager(context)
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


        viewModel.selectedChapterContent.observe(viewLifecycleOwner, Observer { images ->
            isLoadingChapter = false
            binding.contentHorizontal.apply {
                adapter = ImageHorizontalListAdapter(context, images)
                if (!viewModel.fromStart)
                    binding.contentHorizontal.setCurrentItem(images.size - 1, false)
            }
            binding.contentVertical.apply {
                adapter = ImageVerticalListAdapter(context, images)
            }
            binding.seekBar.max = images.size - 1
            binding.seekBar.progress = binding.contentHorizontal.currentItem
            binding.hint.text =
                "${viewModel.getSelectedChapterTitle()} ${binding.contentHorizontal.currentItem + 1}/${binding.contentHorizontal.adapter?.itemCount}"
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

    private fun setReadingDirectionLeftToRight() {
        SettingsHelper.setReadingDirection(SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT)
        binding.radioLeftToRight.isChecked = true
        binding.contentHorizontal.visibility = View.VISIBLE
        binding.contentVertical.visibility = View.GONE
        binding.contentHorizontal.apply {
            layoutDirection = ViewPager2.LAYOUT_DIRECTION_LTR

            val index = currentItem
            adapter =
                ImageHorizontalListAdapter(
                    context,
                    viewModel.selectedChapterContent.value!!
                )
            setCurrentItem(index, false)
        }
    }

    private fun setReadingDirectionRightToLeft() {
        SettingsHelper.setReadingDirection(SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT)
        binding.radioRightToLeft.isChecked = true
        binding.contentHorizontal.visibility = View.VISIBLE
        binding.contentVertical.visibility = View.GONE
        binding.contentHorizontal.apply {
            layoutDirection = ViewPager2.LAYOUT_DIRECTION_RTL

            val index = currentItem
            adapter =
                ImageHorizontalListAdapter(
                    context,
                    viewModel.selectedChapterContent.value!!
                )
            setCurrentItem(index, false)
        }
    }

    private fun setReadingDirectionVertical() {
        SettingsHelper.setReadingDirection(SettingsHelper.READING_DIRECTION_VERTICAL)
        binding.radioVertical.isChecked = true
        binding.contentVertical.visibility = View.VISIBLE
        binding.contentHorizontal.visibility = View.GONE
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