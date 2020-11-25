package com.fishhawk.driftinglibraryandroid.ui.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.databinding.ActivityReaderBinding
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ReaderActivity : BaseActivity() {
    lateinit var binding: ActivityReaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GlobalPreference.screenOrientation.asFlow()
            .onEach {
                val newOrientation = when (it) {
                    GlobalPreference.ScreenOrientation.DEFAULT -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    GlobalPreference.ScreenOrientation.LOCK -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
                    GlobalPreference.ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    GlobalPreference.ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                if (newOrientation != requestedOrientation) requestedOrientation = newOrientation
            }
            .launchIn(lifecycleScope)

        GlobalPreference.keepScreenOn.asFlow()
            .onEach { setFlag(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, it) }
            .launchIn(lifecycleScope)
    }

    var listener: OnVolumeKeyEvent? = null

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val useVolumeKey = GlobalPreference.useVolumeKey.get()
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
