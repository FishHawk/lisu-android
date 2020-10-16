package com.fishhawk.driftinglibraryandroid.ui.provider

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ActivityProviderBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.setupTheme
import com.fishhawk.driftinglibraryandroid.ui.extension.setupWithNavControllerT

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

        binding = ActivityProviderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        title = intent.extras!!.getString("providerName")!!
        if (savedInstanceState == null) setupBottomNavigationBar()

        SettingsHelper.secureMode.observe(this) {
            val flag = WindowManager.LayoutParams.FLAG_SECURE
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val bottomNavigationView = binding.navView

        val navGraphIds = listOf(
            R.navigation.popular,
            R.navigation.latest,
            R.navigation.category
        )

        bottomNavigationView.setupWithNavControllerT(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = binding.navHostContainer.id,
            intent = intent,
            hasHome = false
        )
    }
}