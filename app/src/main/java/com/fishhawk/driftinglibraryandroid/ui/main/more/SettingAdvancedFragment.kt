package com.fishhawk.driftinglibraryandroid.ui.main.more

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.bumptech.glide.Glide
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.base.makeToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingAdvancedFragment : BaseSettingFragment() {
    override val titleResId: Int = R.string.label_settings_advanced
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<Preference>("clear_image_cache")!!.apply {
            setOnPreferenceClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        Glide.get(context).clearDiskCache()
                    }
                    Glide.get(context).clearMemory()
                    makeToast(R.string.toast_cache_cleared)
                }
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_advanced, rootKey)
    }
}
