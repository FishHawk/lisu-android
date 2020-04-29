package com.fishhawk.driftinglibraryandroid.reader

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.library.LibraryViewModel
import com.google.android.material.snackbar.Snackbar


class ReaderFragment : Fragment() {
    private lateinit var viewModel: LibraryViewModel

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_reader, container, false)
        val viewPager: ViewPager = root.findViewById(R.id.view_pager)
        val seekBar: SeekBar = root.findViewById(R.id.seek_bar)
        val hintTextView: TextView = root.findViewById(R.id.hint)

        viewPager.offscreenPageLimit = 5

        viewModel.selectedChapterContent.observe(viewLifecycleOwner, Observer { images ->
            viewPager.apply {
                adapter = ImageListAdapter(childFragmentManager, images)
                if (!viewModel.fromStart) {
                    currentItem = adapter!!.count - 1
                }
                clearOnPageChangeListeners()
                addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    private var isEdge: Boolean = true
                    private var isFirstPage: Boolean = currentItem == 0
                    private var isLastPage: Boolean = currentItem == adapter!!.count - 1
                    private var hasJump: Boolean = false

                    override fun onPageScrollStateChanged(state: Int) {
                        when (state) {
                            ViewPager.SCROLL_STATE_IDLE -> {
                                if (!isEdge && !hasJump) {
                                    if (isFirstPage) {
                                        hasJump = viewModel.openPrevChapter()
                                        if (!hasJump) {
                                            Snackbar.make(
                                                root, "没有了", Snackbar.LENGTH_LONG
                                            ).setAction("Action", null).show()
                                        }
                                    } else if (isLastPage) {
                                        hasJump = viewModel.openNextChapter()
                                        if (!hasJump) {
                                            Snackbar.make(
                                                root, "没有了", Snackbar.LENGTH_LONG
                                            ).setAction("Action", null).show()
                                        }
                                    }
                                }
                            }
                            ViewPager.SCROLL_STATE_DRAGGING -> {
                                isEdge = false
                            }
                            ViewPager.SCROLL_STATE_SETTLING -> {
                                isEdge = true
                            }
                        }
                    }

                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                    }

                    override fun onPageSelected(position: Int) {
                        seekBar.progress = position
                        isFirstPage = position == 0
                        isLastPage = position == adapter!!.count - 1
                        hintTextView.text =
                            "${viewModel.getSelectedChapterTitle()} ${viewPager.currentItem + 1}/${viewPager.adapter?.count}"
                    }
                })
            }

            hintTextView.text =
                "${viewModel.getSelectedChapterTitle()} ${viewPager.currentItem + 1}/${viewPager.adapter?.count}"

            seekBar.apply {
                max = viewPager.adapter!!.count - 1
                progress = viewPager.currentItem
                setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
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
        })
        return root
    }
}