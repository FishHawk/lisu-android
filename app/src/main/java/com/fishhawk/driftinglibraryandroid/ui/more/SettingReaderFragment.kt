package com.fishhawk.driftinglibraryandroid.ui.more

import android.os.Bundle
import com.fishhawk.driftinglibraryandroid.R

class SettingReaderFragment : BaseSettingFragment() {
    override val titleResId: Int = R.string.label_settings_reader
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_reader, rootKey)
    }
}