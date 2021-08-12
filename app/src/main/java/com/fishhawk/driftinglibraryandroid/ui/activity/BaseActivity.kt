package com.fishhawk.driftinglibraryandroid.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.PR
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        PR.secureMode.flow
            .onEach { setFlag(WindowManager.LayoutParams.FLAG_SECURE, it) }
            .launchIn(lifecycleScope)
    }

    protected fun setFlag(flag: Int, isEnabled: Boolean) =
        if (isEnabled) window.addFlags(flag)
        else window.clearFlags(flag)
}