package com.fishhawk.driftinglibraryandroid

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.ui.NavigationUI
import com.fishhawk.driftinglibraryandroid.databinding.ActivityMainBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.getThemeResId

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (SettingsHelper.theme.getValueDirectly()) {
            SettingsHelper.THEME_LIGHT -> setTheme(R.style.AppTheme_NoActionBar)
            SettingsHelper.THEME_DARK -> setTheme(R.style.AppTheme_Dark_NoActionBar)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.contentMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.mobile_navigation, intent.extras)
        binding.navView.setupWithNavController(navController)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_library, R.id.nav_history, R.id.nav_explore, R.id.nav_setting)
        )
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        SettingsHelper.theme.observe(this, Observer {
            val applyTheme = { themeId: Int ->
                if (getThemeResId() != themeId) {
                    setTheme(R.style.AppTheme_NoActionBar)
                    recreate()
                }
            }
            when (it) {
                SettingsHelper.THEME_LIGHT -> applyTheme(
                    R.style.AppTheme_NoActionBar
                )
                SettingsHelper.THEME_DARK -> applyTheme(
                    R.style.AppTheme_Dark_NoActionBar
                )
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
