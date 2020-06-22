package com.fishhawk.driftinglibraryandroid.util;

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainActivity
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.gallery.GalleryActivity
import com.fishhawk.driftinglibraryandroid.reader.ReaderActivity
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import java.lang.reflect.Method


fun AppCompatActivity.navToGalleryActivity(
    id: String,
    title: String,
    thumb: String,
    source: String?,
    imageView: ImageView
) {
    val bundle = bundleOf(
        "id" to id,
        "title" to title,
        "thumb" to thumb,
        "source" to source
    )

    val intent = Intent(this, GalleryActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun AppCompatActivity.navToReaderActivity(
    id: String,
    source: String?,
    collectionIndex: Int = 0,
    chapterIndex: Int = 0,
    pageIndex: Int = 0
) {
    val bundle = bundleOf(
        "id" to id,
        "source" to source,
        "collectionIndex" to collectionIndex,
        "chapterIndex" to chapterIndex,
        "pageIndex" to pageIndex
    )

    val intent = Intent(this, ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun AppCompatActivity.navToMainActivity(
    filter: String
) {
    val bundle = bundleOf("filter" to filter)
    val intent = Intent(this, MainActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

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
        SettingsHelper.THEME_LIGHT -> setTheme(lightThemeId)
        SettingsHelper.THEME_DARK -> setTheme(darkThemeId)
    }

    SettingsHelper.theme.observe(this, Observer {
        val themeId = when (it) {
            SettingsHelper.THEME_LIGHT -> lightThemeId
            SettingsHelper.THEME_DARK -> darkThemeId
            else -> lightThemeId
        }
        if (getThemeResId() != themeId) {
            setTheme(themeId)
            recreate()
        }
    })
}

fun AppCompatActivity.setupTheme() {
    val lightThemeId = R.style.Theme_App_Light
    val darkThemeId = R.style.Theme_App_Dark

    when (SettingsHelper.theme.getValueDirectly()) {
        SettingsHelper.THEME_LIGHT -> setTheme(lightThemeId)
        SettingsHelper.THEME_DARK -> setTheme(darkThemeId)
    }

    SettingsHelper.theme.observe(this, Observer {
        val themeId = when (it) {
            SettingsHelper.THEME_LIGHT -> lightThemeId
            SettingsHelper.THEME_DARK -> darkThemeId
            else -> lightThemeId
        }
        if (getThemeResId() != themeId) {
            setTheme(themeId)
            recreate()
        }
    })
}

