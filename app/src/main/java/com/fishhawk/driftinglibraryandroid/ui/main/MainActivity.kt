package com.fishhawk.driftinglibraryandroid.ui.main

import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ActivityMainBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.extension.setupWithNavControllerT

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            R.navigation.library,
            R.navigation.history,
            R.navigation.explore,
            R.navigation.more
        )

        currentNavController = bottomNavigationView.setupWithNavControllerT(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = binding.navHostContainer.id,
            intent = intent
        )

        bottomNavigationView.selectedItemId =
            when (SettingsHelper.startScreen.getValueDirectly()) {
                SettingsHelper.StartScreen.LIBRARY -> R.id.library
                SettingsHelper.StartScreen.HISTORY -> R.id.history
                SettingsHelper.StartScreen.EXPLORE -> R.id.explore
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
}
