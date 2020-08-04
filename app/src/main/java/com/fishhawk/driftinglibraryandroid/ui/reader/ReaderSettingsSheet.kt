package com.fishhawk.driftinglibraryandroid.ui.reader

import android.os.Bundle
import com.fishhawk.driftinglibraryandroid.databinding.ReaderSettingsSheetBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.google.android.material.bottomsheet.BottomSheetDialog


class ReaderSettingsSheet(activity: ReaderActivity) : BottomSheetDialog(activity) {
    private val binding = ReaderSettingsSheetBinding.inflate(activity.layoutInflater, null, false)

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGeneralPreferences()
    }

    private fun initGeneralPreferences() {
        binding.direction.onItemSelectedListener = IgnoreFirstSpinnerListener { position ->
            SettingsHelper.readingDirection.setValue(position)
        }
        binding.direction.setSelection(
            SettingsHelper.readingDirection.getOrdinal(), false
        )
    }
}