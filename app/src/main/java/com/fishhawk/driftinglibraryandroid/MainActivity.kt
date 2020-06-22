package com.fishhawk.driftinglibraryandroid

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.NavigationUI
import com.fishhawk.driftinglibraryandroid.databinding.ActivityMainBinding
import com.fishhawk.driftinglibraryandroid.util.setupTheme

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.mobile_navigation, intent.extras)
        binding.navView.setupWithNavController(navController)
        val mainFragmentSet = setOf(
            R.id.nav_library,
            R.id.nav_history,
            R.id.nav_explore,
            R.id.nav_setting
        )
        val appBarConfiguration = AppBarConfiguration(mainFragmentSet)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                in mainFragmentSet -> binding.navView.visibility = View.VISIBLE
                else -> binding.navView.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
