package com.fishhawk.driftinglibraryandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.fishhawk.driftinglibraryandroid.databinding.ActivityMainBinding
import com.fishhawk.driftinglibraryandroid.extension.setupTheme
import com.fishhawk.driftinglibraryandroid.extension.setupWithNavControllerT
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (savedInstanceState == null) setupBottomNavigationBar()
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

        val controller = bottomNavigationView.setupWithNavControllerT(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent,
            appBarLayout = binding.appBarLayout
        )

        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(this, navController)
        })
        currentNavController = controller

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
