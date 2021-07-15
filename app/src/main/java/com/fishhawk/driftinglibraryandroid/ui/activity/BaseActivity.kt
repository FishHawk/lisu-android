package com.fishhawk.driftinglibraryandroid.ui.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.data.preference.P
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

open class BaseActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    fun requestPermission(permission: String) {
        requestPermissionLauncher.launch(permission)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupFullScreen()
        setupSecureModule()
    }

    private fun setupSecureModule() {
        P.secureMode.asFlow()
            .onEach { setFlag(WindowManager.LayoutParams.FLAG_SECURE, it) }
            .launchIn(lifecycleScope)
    }

    private fun setupFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    protected fun setFlag(flag: Int, isEnabled: Boolean) {
        if (isEnabled) window.addFlags(flag)
        else window.clearFlags(flag)
    }
}