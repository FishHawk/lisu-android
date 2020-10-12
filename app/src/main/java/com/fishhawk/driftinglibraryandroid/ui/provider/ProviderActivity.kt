package com.fishhawk.driftinglibraryandroid.ui.provider

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ActivityProviderBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.setupTheme
import com.fishhawk.driftinglibraryandroid.ui.extension.setupWithNavControllerT

class ProviderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProviderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)

        binding = ActivityProviderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        title = intent.extras!!.getString("providerName")!!
        if (savedInstanceState == null) setupBottomNavigationBar()
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
            appBarLayout = binding.appBarLayout
        )
    }
}