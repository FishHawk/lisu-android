package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.ReaderSettingsSheetBinding
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.widget.PreferenceBottomSheetDialog

class ReaderSettingsSheet(context: Context) : PreferenceBottomSheetDialog(context) {

    private val binding =
        ReaderSettingsSheetBinding.inflate(
            LayoutInflater.from(context), null, false
        )

    init {
        bindPreference(P.readingDirection, binding.direction)
        bindPreference(P.screenOrientation, binding.orientation)

        bindPreference(P.isPageIntervalEnabled, binding.pageInterval)
        bindPreference(P.showInfoBar, binding.showInfoBar)
        bindPreference(P.keepScreenOn, binding.keepScreenOn)
        bindPreference(P.useVolumeKey, binding.useVolumeKey)
        bindPreference(P.invertVolumeKey, binding.invertVolumeKey)
        bindPreference(P.isLongTapDialogEnabled, binding.longTapDialog)

        bindPreference(P.isAreaInterpolationEnabled, binding.areaInterpolationEnabled)

        setContentView(binding.root)
    }
}