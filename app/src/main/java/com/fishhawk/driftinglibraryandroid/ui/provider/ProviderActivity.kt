package com.fishhawk.driftinglibraryandroid.ui.provider

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderActivityBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.setupTheme
import com.fishhawk.driftinglibraryandroid.ui.extension.setupWithNavControllerT

class ProviderActivity : AppCompatActivity() {
    val viewModel: ProviderViewModel by viewModels {
        val providerId = intent.extras!!.getString("providerId")!!
        ProviderViewModelFactory(providerId, application as MainApplication)
    }
    private lateinit var binding: ProviderActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)

        binding = ProviderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        title = intent.extras!!.getString("providerId")!!
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
            R.navigation.category,
            R.navigation.search
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