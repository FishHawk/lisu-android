package com.fishhawk.driftinglibraryandroid.reader

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.library.LibraryViewModel
import com.google.android.material.snackbar.Snackbar


class ReaderFragment : Fragment() {
    private lateinit var viewModel: LibraryViewModel
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun toPrevChapter() {
        if (!viewModel.openPrevChapter()) {
            view?.let { Snackbar.make(it, "没有了", Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun toNextChapter() {
        if (!viewModel.openNextChapter()) {
            view?.let { Snackbar.make(it, "没有了", Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun toPrevPage() {
        viewPager.apply {
            if (adapter != null) {
                if (currentItem == 0) toPrevChapter()
                else setCurrentItem(currentItem - 1, false)
            }
        }
    }

    private fun toNextPage() {
        viewPager.apply {
            if (adapter != null) {
                if (currentItem == adapter!!.itemCount - 1) toNextChapter()
                else setCurrentItem(currentItem + 1, false)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_reader, container, false)
        viewPager = root.findViewById(R.id.view_pager)
        val seekBar: SeekBar = root.findViewById(R.id.seek_bar)
        val hintTextView: TextView = root.findViewById(R.id.hint)

        (root as ReaderLayout).apply {
            onClickLeftAreaListener = {
                when (viewPager.layoutDirection) {
                    ViewPager2.LAYOUT_DIRECTION_LTR -> toPrevPage()
                    ViewPager2.LAYOUT_DIRECTION_RTL -> toNextPage()
                }
            }
            onClickRightAreaListener = {
                when (viewPager.layoutDirection) {
                    ViewPager2.LAYOUT_DIRECTION_LTR -> toNextPage()
                    ViewPager2.LAYOUT_DIRECTION_RTL -> toPrevPage()
                }
            }
            onClickCenterAreaListener = { println("center") }
        }

        viewPager.apply {
            offscreenPageLimit = 5
//            layoutDirection = ViewPager2.LAYOUT_DIRECTION_RTL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                private var isEdge: Boolean = true
                private var isFirstPage: Boolean = false
                private var isLastPage: Boolean = false

                override fun onPageScrollStateChanged(state: Int) {
                    when (state) {
                        ViewPager2.SCROLL_STATE_IDLE -> {

                            if (!isEdge && isFirstPage && !viewModel.openPrevChapter()) {
                                Snackbar.make(
                                    root, "没有了", Snackbar.LENGTH_LONG
                                ).setAction("Action", null).show()
                            } else if (!isEdge && isLastPage && !viewModel.openNextChapter()) {
                                Snackbar.make(
                                    root, "没有了", Snackbar.LENGTH_LONG
                                ).setAction("Action", null).show()
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
                    isFirstPage = position == 0
                    isLastPage = position == adapter?.itemCount?.minus(1) ?: false
                    seekBar.progress = position
                    hintTextView.text =
                        "${viewModel.getSelectedChapterTitle()} ${viewPager.currentItem + 1}/${viewPager.adapter?.itemCount}"
                }
            })
        }

        seekBar.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seek: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seek: SeekBar) {}
                override fun onStopTrackingTouch(seek: SeekBar) {
                    viewPager.setCurrentItem(seek.progress, false)
                }
            })
        }


        viewModel.selectedChapterContent.observe(viewLifecycleOwner, Observer { images ->
            viewPager.apply {
                adapter = ImageListAdapter(context, images)
                if (!viewModel.fromStart)
                    viewPager.setCurrentItem(images.size - 1, false)
            }

            seekBar.max = images.size
            seekBar.progress = viewPager.currentItem
        })
        return root
    }
}