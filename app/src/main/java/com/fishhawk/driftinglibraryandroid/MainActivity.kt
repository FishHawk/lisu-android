package com.fishhawk.driftinglibraryandroid

import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.ui.NavigationUI
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsHelper.initialize(this)
        when (SettingsHelper.theme.getValueDirectly()) {
            SettingsHelper.THEME_LIGHT -> setTheme(R.style.AppTheme_NoActionBar)
            SettingsHelper.THEME_DARK -> setTheme(R.style.AppTheme_Dark_NoActionBar)
        }

        SettingsHelper.theme.observe(this, Observer {
            val currentTheme = Util.extractThemeResId(this)
            when (it) {
                SettingsHelper.THEME_LIGHT ->
                    if (currentTheme != R.style.AppTheme_NoActionBar) {
                        setTheme(R.style.AppTheme_NoActionBar)
                        recreate()
                    }
                SettingsHelper.THEME_DARK ->
                    if (currentTheme != R.style.AppTheme_Dark_NoActionBar) {
                        recreate()
                        setTheme(R.style.AppTheme_NoActionBar)
                    }
            }
        })

        setContentView(R.layout.activity_main)

        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration.Builder(R.id.nav_library)
            .setDrawerLayout(drawer)
            .build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
