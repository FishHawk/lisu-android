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
import com.fishhawk.driftinglibraryandroid.databinding.FragmentReaderBinding
import com.fishhawk.driftinglibraryandroid.library.LibraryViewModel
import com.google.android.material.snackbar.Snackbar


class ReaderFragment : Fragment() {
    private lateinit var viewModel: LibraryViewModel
    private lateinit var binding: FragmentReaderBinding
    private var isLoadingChapter: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentReaderBinding.inflate(layoutInflater)
        viewModel = activity?.run { ViewModelProvider(this)[LibraryViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (binding.root as ReaderLayout).apply {
            onClickLeftAreaListener = {
                when (binding.viewPager.layoutDirection) {
                    ViewPager2.LAYOUT_DIRECTION_LTR -> toPrevPage()
                    ViewPager2.LAYOUT_DIRECTION_RTL -> toNextPage()
                }
            }
            onClickRightAreaListener = {
                when (binding.viewPager.layoutDirection) {
                    ViewPager2.LAYOUT_DIRECTION_LTR -> toNextPage()
                    ViewPager2.LAYOUT_DIRECTION_RTL -> toPrevPage()
                }
            }
            onClickCenterAreaListener = { println("center") }
        }

        binding.viewPager.apply {
            offscreenPageLimit = 5
//            layoutDirection = ViewPager2.LAYOUT_DIRECTION_RTL
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
                        "${viewModel.getSelectedChapterTitle()} ${binding.viewPager.currentItem + 1}/${binding.viewPager.adapter?.itemCount}"
                }
            })
        }

        binding.seekBar.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seek: SeekBar) {}
                override fun onStopTrackingTouch(seek: SeekBar) {
                    binding.viewPager.setCurrentItem(seek.progress, false)
                }
            })
        }


        viewModel.selectedChapterContent.observe(viewLifecycleOwner, Observer { images ->
            isLoadingChapter = false
            binding.viewPager.apply {
                adapter = ImageListAdapter(context, images)
                if (!viewModel.fromStart)
                    binding.viewPager.setCurrentItem(images.size - 1, false)
            }
            binding.seekBar.max = images.size
            binding.seekBar.progress = binding.viewPager.currentItem
        })
    }


    private fun toPrevChapter() {
        isLoadingChapter = true
        if (!viewModel.openPrevChapter()) {
            view?.let { Snackbar.make(it, "没有了", Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun toNextChapter() {
        isLoadingChapter = true
        if (!viewModel.openNextChapter()) {
            view?.let { Snackbar.make(it, "没有了", Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun toPrevPage() {
        binding.viewPager.apply {
            if (adapter != null) {
                if (currentItem == 0) toPrevChapter()
                else setCurrentItem(currentItem - 1, false)
            }
        }
    }

    private fun toNextPage() {
        binding.viewPager.apply {
            if (adapter != null) {
                if (currentItem == adapter!!.itemCount - 1) toNextChapter()
                else setCurrentItem(currentItem + 1, false)
            }
        }
    }
}