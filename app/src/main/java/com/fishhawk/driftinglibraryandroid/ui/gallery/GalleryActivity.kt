package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.databinding.ActivityGalleryBinding
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.setupFullScreen
import com.fishhawk.driftinglibraryandroid.ui.extension.setupThemeWithTranslucentStatus

class GalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeWithTranslucentStatus()
        super.onCreate(savedInstanceState)
        setupFullScreen()

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SettingsHelper.secureMode.observe(this) {
            val flag = WindowManager.LayoutParams.FLAG_SECURE
            if (it) window.addFlags(flag)
            else window.clearFlags(flag)
        }
    }
}
