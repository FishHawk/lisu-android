package com.fishhawk.driftinglibraryandroid.ui.main.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.SettingFragmentBinding

abstract class BaseSettingFragment : PreferenceFragmentCompat() {
    protected lateinit var binding: SettingFragmentBinding
    protected abstract val titleResId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding = SettingFragmentBinding.inflate(inflater, container, false)
        binding.preference.addView(view)
        binding.toolbar.title = getString(titleResId)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        return binding.root
    }
}