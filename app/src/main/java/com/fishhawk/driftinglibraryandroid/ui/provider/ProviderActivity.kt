package com.fishhawk.driftinglibraryandroid.ui.provider

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.databinding.ActivityProviderBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.setupTheme

class ProviderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProviderBinding
    var keywords: String? = null
        private set
        get() {
            val temp = field
            field = null
            return temp
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)

        keywords = intent.extras?.getString("keywords")
        title = intent.extras!!.getString("providerName")!!

        binding = ActivityProviderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val sectionsPagerAdapter = ProviderPagerAdapter(this, supportFragmentManager)
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.viewPager.offscreenPageLimit = 3
        binding.tabs.setupWithViewPager(binding.viewPager)

        SettingsHelper.secureMode.observe(this) {
            val flag = WindowManager.LayoutParams.FLAG_SECURE
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
        }
    }
}