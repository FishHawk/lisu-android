package com.fishhawk.driftinglibraryandroid.ui.activity

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ActivityMainBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        currentNavController = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = binding.navHostContainer.id,
            intent = intent
        )

        bottomNavigationView.selectedItemId =
            when (GlobalPreference.startScreen.getValueDirectly()) {
                GlobalPreference.StartScreen.LIBRARY -> R.id.nav_graph_library
                GlobalPreference.StartScreen.HISTORY -> R.id.nav_graph_history
                GlobalPreference.StartScreen.EXPLORE -> R.id.nav_graph_explore
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
}
