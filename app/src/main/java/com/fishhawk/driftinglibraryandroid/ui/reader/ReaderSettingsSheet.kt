package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.data.datastore.PR
import com.fishhawk.driftinglibraryandroid.databinding.ReaderSettingsSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ReaderSettingsSheet(
    context: Context,
    private val scope: CoroutineScope
) : BottomSheetDialog(context) {

    private val binding = ReaderSettingsSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        bindPreference(PR.isPageIntervalEnabled, binding.pageInterval)
        bindPreference(PR.showInfoBar, binding.showInfoBar)
        bindPreference(PR.isLongTapDialogEnabled, binding.longTapDialog)
        bindPreference(PR.isAreaInterpolationEnabled, binding.areaInterpolationEnabled)

        bindPreference(PR.keepScreenOn, binding.keepScreenOn)
        bindPreference(PR.useVolumeKey, binding.useVolumeKey)
        bindPreference(PR.invertVolumeKey, binding.invertVolumeKey)

        setContentView(binding.root)
    }

    private fun bindPreference(
        preference: com.fishhawk.driftinglibraryandroid.data.datastore.Preference<Boolean>,
        switch: SwitchMaterial
    ) {
        preference.flow.onEach { switch.isChecked = it }.launchIn(scope)
        switch.setOnCheckedChangeListener { _, it -> scope.launch { preference.set(it) } }
    }
}