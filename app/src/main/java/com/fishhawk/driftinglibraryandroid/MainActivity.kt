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
import androidx.navigation.ui.NavigationUI
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

       SettingsHelper.initialize(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
