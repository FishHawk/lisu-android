package com.fishhawk.driftinglibraryandroid.ui.more

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import coil.imageLoader
import coil.util.CoilUtils
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.base.toast
import kotlinx.coroutines.launch

class SettingAdvancedFragment : BaseSettingFragment() {
    override val titleResId: Int = R.string.label_settings_advanced
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<Preference>("clear_image_cache")!!.apply {
            setOnPreferenceClickListener {
                lifecycleScope.launch {
                    context.imageLoader.memoryCache.clear()
                    CoilUtils.createDefaultCache(context).directory.deleteRecursively()
                    requireContext().toast(R.string.toast_cache_cleared)
                }
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_advanced, rootKey)
    }
}
