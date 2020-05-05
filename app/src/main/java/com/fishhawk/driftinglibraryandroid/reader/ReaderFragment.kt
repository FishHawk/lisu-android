package com.fishhawk.driftinglibraryandroid.reader

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.FragmentReaderBinding
import com.fishhawk.driftinglibraryandroid.gallery.GalleryViewModel
import com.fishhawk.driftinglibraryandroid.library.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.google.android.material.snackbar.Snackbar

class ReaderFragment : Fragment() {
    private lateinit var viewModel: GalleryViewModel
    private lateinit var binding: FragmentReaderBinding
    private var isLoadingChapter: Boolean = true

    private lateinit var sharedPreferences: SharedPreferences

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

        when (SettingsHelper.getReadingDirection()) {
            0 -> setReadingDirectionLTR()
            1 -> setReadingDirectionRTL()
            2 -> setReadingDirectionVertical()
        }

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
            }
        }

        binding.radioGroupDirection.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_ltr -> setReadingDirectionLTR()
                R.id.radio_rtl -> setReadingDirectionRTL()
                R.id.radio_vertical -> setReadingDirectionVertical()
            }
        }

        binding.menuLayout.setOnClickListener {
            binding.menuLayout.visibility = View.GONE
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
            binding.seekBar.max = images.size
            binding.seekBar.progress = binding.contentHorizontal.currentItem
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

    private fun setReadingDirectionLTR() {
        SettingsHelper.setReadingDirection(0)
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
    private fun setReadingDirectionRTL() {
        SettingsHelper.setReadingDirection(1)
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
        SettingsHelper.setReadingDirection(2)
        binding.contentVertical.visibility = View.VISIBLE
        binding.contentHorizontal.visibility = View.GONE
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
}