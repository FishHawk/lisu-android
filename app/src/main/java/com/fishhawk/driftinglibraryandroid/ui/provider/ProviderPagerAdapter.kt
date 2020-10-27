package com.fishhawk.driftinglibraryandroid.ui.provider

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.fishhawk.driftinglibraryandroid.R
import java.lang.IllegalArgumentException

class ProviderPagerAdapter(
    private val context: Context,
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val popularFragment by lazy { PopularFragment() }
    private val latestFragment by lazy { LatestFragment() }
    private val categoryFragment by lazy { CategoryFragment() }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> popularFragment
            1 -> latestFragment
            2 -> categoryFragment
            else -> throw IllegalArgumentException("impossible position")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }

    companion object {
        private val TAB_TITLES = arrayOf(
            R.string.label_popular,
            R.string.label_latest,
            R.string.label_category
        )
    }
}