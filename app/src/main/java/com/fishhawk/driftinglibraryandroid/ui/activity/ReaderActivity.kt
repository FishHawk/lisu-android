package com.fishhawk.driftinglibraryandroid.ui.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import com.fishhawk.driftinglibraryandroid.databinding.ActivityReaderBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference

class ReaderActivity : BaseActivity() {
    lateinit var binding: ActivityReaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GlobalPreference.screenOrientation.observe(this) {
            val newOrientation = when (it) {
                GlobalPreference.ScreenOrientation.DEFAULT -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                GlobalPreference.ScreenOrientation.LOCK -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
                GlobalPreference.ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                GlobalPreference.ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            if (newOrientation != requestedOrientation) {
                requestedOrientation = newOrientation
            }
        }

        GlobalPreference.keepScreenOn.observe(this) {
            val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
        }
    }

    var listener: OnVolumeKeyEvent? = null

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val useVolumeKey = GlobalPreference.useVolumeKey.getValueDirectly()
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> useVolumeKey.also { if (it) listener?.onVolumeUp() }
            KeyEvent.KEYCODE_VOLUME_DOWN -> useVolumeKey.also { if (it) listener?.onVolumeDown() }
            else -> super.dispatchKeyEvent(event)
        }
    }

    interface OnVolumeKeyEvent {
        fun onVolumeUp()
        fun onVolumeDown()
    }
}
