package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatSpinner
import com.fishhawk.driftinglibraryandroid.databinding.ReaderColorFilterSheetBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.preference.PreferenceBooleanLiveData
import com.fishhawk.driftinglibraryandroid.preference.PreferenceEnumLiveData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial


class ReaderOverlaySheet(context: Context) : BottomSheetDialog(context) {

    private val binding = ReaderColorFilterSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)
    }
}