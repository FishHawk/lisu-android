package com.fishhawk.driftinglibraryandroid.reader

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


class ImageListAdapter(
    fragmentManager: FragmentManager,
    private val data: List<String>
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_SET_USER_VISIBLE_HINT) {
    override fun getItem(position: Int): Fragment {
        val image = data[position]
        return ImageFragment.newInstance(image)
    }

    override fun getCount() = data.size
}