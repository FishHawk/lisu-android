package com.fishhawk.driftinglibraryandroid.ui.extension

import android.content.Context
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import java.lang.reflect.Method

private fun AppCompatActivity.getThemeResId(): Int {
    val wrapper: Class<*> = Context::class.java
    val method: Method = wrapper.getMethod("getThemeResId")
    method.isAccessible = true
    return method.invoke(this) as Int
}

fun AppCompatActivity.setupFullScreen() {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (getThemeResId() == R.style.Theme_App_Light_TranslucentStatus)
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    }
}

fun AppCompatActivity.setupThemeWithTranslucentStatus() {
    val lightThemeId = R.style.Theme_App_Light_TranslucentStatus
    val darkThemeId = R.style.Theme_App_Dark_TranslucentStatus

    when (SettingsHelper.theme.getValueDirectly()) {
        SettingsHelper.Theme.LIGHT -> setTheme(lightThemeId)
        SettingsHelper.Theme.DARK -> setTheme(darkThemeId)
    }

    SettingsHelper.theme.observe(this, Observer {
        val themeId = when (it) {
            SettingsHelper.Theme.LIGHT -> lightThemeId
            SettingsHelper.Theme.DARK -> darkThemeId
            else -> lightThemeId
        }
        if (getThemeResId() != themeId) {
            recreate()
        }
    })
}

fun AppCompatActivity.setupTheme() {
    val lightThemeId = R.style.Theme_App_Light
    val darkThemeId = R.style.Theme_App_Dark

    when (SettingsHelper.theme.getValueDirectly()) {
        SettingsHelper.Theme.LIGHT -> setTheme(lightThemeId)
        SettingsHelper.Theme.DARK -> setTheme(darkThemeId)
    }

    SettingsHelper.theme.observe(this, Observer {
        val themeId = when (it) {
            SettingsHelper.Theme.LIGHT -> lightThemeId
            SettingsHelper.Theme.DARK -> darkThemeId
            else -> throw InternalError()
        }
        if (getThemeResId() != themeId) {
            recreate()
        }
    })
}
