package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.databinding.ReaderActivityBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.setupFullScreen
import com.fishhawk.driftinglibraryandroid.ui.extension.setupThemeWithTranslucentStatus
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

class ReaderActivity : AppCompatActivity() {
    lateinit var binding: ReaderActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeWithTranslucentStatus()
        super.onCreate(savedInstanceState)
        setupFullScreen()

        binding = ReaderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SettingsHelper.screenOrientation.observe(this, Observer {
            val newOrientation = when (it) {
                SettingsHelper.ScreenOrientation.DEFAULT -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                SettingsHelper.ScreenOrientation.LOCK -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
                SettingsHelper.ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                SettingsHelper.ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            if (newOrientation != requestedOrientation) {
                requestedOrientation = newOrientation
            }
        })

        SettingsHelper.keepScreenOn.observe(this, Observer {
            val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
        })
    }
}
