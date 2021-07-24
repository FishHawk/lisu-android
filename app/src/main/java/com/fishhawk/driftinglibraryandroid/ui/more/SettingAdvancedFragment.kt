package com.fishhawk.driftinglibraryandroid.ui.more

//class SettingAdvancedFragment : BaseSettingFragment() {
//    override val titleResId: Int = R.string.label_settings_advanced
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        findPreference<Preference>("clear_image_cache")!!.apply {
//            setOnPreferenceClickListener {
//                lifecycleScope.launch {
//                    context.imageLoader.memoryCache.clear()
//                    CoilUtils.createDefaultCache(context).directory.deleteRecursively()
//                    requireContext().toast(R.string.toast_cache_cleared)
//                }
//                true
//            }
//        }
//    }
//
//    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//        setPreferencesFromResource(R.xml.setting_advanced, rootKey)
//    }
//}
