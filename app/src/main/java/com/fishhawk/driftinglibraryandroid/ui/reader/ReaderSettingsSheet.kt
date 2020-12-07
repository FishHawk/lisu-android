package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.ReaderSettingsSheetBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.widget.PreferenceBottomSheetDialog

class ReaderSettingsSheet(context: Context) : PreferenceBottomSheetDialog(context) {

    private val binding =
        ReaderSettingsSheetBinding.inflate(
            LayoutInflater.from(context), null, false
        )

    init {
        bindPreference(GlobalPreference.readingDirection, binding.direction)
        bindPreference(GlobalPreference.screenOrientation, binding.orientation)

        bindPreference(GlobalPreference.pageIntervalEnabled, binding.pageInterval)
        bindPreference(GlobalPreference.keepScreenOn, binding.keepScreenOn)
        bindPreference(GlobalPreference.volumeKeyEnabled, binding.useVolumeKey)
        bindPreference(GlobalPreference.longTapDialogEnabled, binding.longTapDialog)

        setContentView(binding.root)
    }
}