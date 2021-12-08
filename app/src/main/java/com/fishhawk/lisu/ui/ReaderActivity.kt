package com.fishhawk.lisu.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.ReaderOrientation
import com.fishhawk.lisu.ui.base.BaseActivity
import com.fishhawk.lisu.ui.reader.ReaderScreen
import com.fishhawk.lisu.ui.theme.LisuTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ReaderActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PR.readerOrientation.flow
            .onEach {
                val newOrientation = when (it) {
                    ReaderOrientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    ReaderOrientation.Landscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                if (newOrientation != requestedOrientation) requestedOrientation = newOrientation
            }
            .launchIn(lifecycleScope)

        PR.keepScreenOn.flow
            .onEach { setFlag(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, it) }
            .launchIn(lifecycleScope)

        combine(
            PR.enableCustomBrightness.flow,
            PR.customBrightness.flow
        ) { isEnabled, brightness ->
            val attrBrightness =
                if (isEnabled) brightness.coerceIn(0f, 1f)
                else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = window.attributes.apply { screenBrightness = attrBrightness }
        }.launchIn(lifecycleScope)

        setContent {
            LisuTheme {
                ReaderScreen()
            }
        }
    }
}
