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
import com.fishhawk.driftinglibraryandroid.util.Util

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsHelper.initialize(this)
        when (SettingsHelper.theme.getValueDirectly()) {
            SettingsHelper.THEME_LIGHT -> setTheme(R.style.AppTheme_NoActionBar)
            SettingsHelper.THEME_DARK -> setTheme(R.style.AppTheme_Dark_NoActionBar)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.contentMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        binding.navView.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration
            .Builder(R.id.nav_library, R.id.nav_history)
            .setDrawerLayout(binding.drawerLayout)
            .build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        SettingsHelper.theme.observe(this, Observer {
            val applyTheme = { themeId: Int, themeString: String ->
                binding.themeSelector.text = themeString
                if (Util.extractThemeResId(this) != themeId) {
                    setTheme(R.style.AppTheme_NoActionBar)
                    recreate()
                }
            }
            when (it) {
                SettingsHelper.THEME_LIGHT -> applyTheme(
                    R.style.AppTheme_NoActionBar,
                    getString(R.string.theme_selector_light)
                )
                SettingsHelper.THEME_DARK -> applyTheme(
                    R.style.AppTheme_Dark_NoActionBar,
                    getString(R.string.theme_selector_dark)
                )
            }
        })
        binding.themeSelector.setOnClickListener {
            when (SettingsHelper.theme.value) {
                SettingsHelper.THEME_LIGHT -> SettingsHelper.theme.setValue(SettingsHelper.THEME_DARK)
                SettingsHelper.THEME_DARK -> SettingsHelper.theme.setValue(SettingsHelper.THEME_LIGHT)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
