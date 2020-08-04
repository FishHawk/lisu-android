package com.fishhawk.driftinglibraryandroid.ui.reader

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.widget.NestedScrollView
import com.fishhawk.driftinglibraryandroid.databinding.ReaderSettingsSheetBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.google.android.material.bottomsheet.BottomSheetDialog

class IgnoreFirstSpinnerListener(
    private val onSelected: (Int) -> Unit
) : AdapterView.OnItemSelectedListener {
    private var hasSelected = false

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (hasSelected) onSelected(position)
        else hasSelected = true
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}

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