package com.fishhawk.driftinglibraryandroid.ui.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import java.lang.reflect.Method

open class BaseActivity : AppCompatActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

    fun requestPermission(permission: String) {
        requestPermissionLauncher.launch(permission)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)

        setupFullScreen()
        setupSecureModule()
    }

    private fun setupSecureModule() {
        GlobalPreference.secureMode.observe(this) {
            val flag = WindowManager.LayoutParams.FLAG_SECURE
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
        }
    }

    private fun getThemeResId(): Int {
        val wrapper: Class<*> = Context::class.java
        val method: Method = wrapper.getMethod("getThemeResId")
        method.isAccessible = true
        return method.invoke(this) as Int
    }

    private fun setupFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getThemeResId() == R.style.Theme_App_Light_TranslucentStatus)
                window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }

    private fun setupTheme() {
        val lightThemeId = R.style.Theme_App_Light_TranslucentStatus
        val darkThemeId = R.style.Theme_App_Dark_TranslucentStatus

        when (GlobalPreference.theme.getValueDirectly()) {
            GlobalPreference.Theme.LIGHT -> setTheme(lightThemeId)
            GlobalPreference.Theme.DARK -> setTheme(darkThemeId)
        }
    }
}