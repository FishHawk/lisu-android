package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.databinding.ActivityGalleryBinding
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
    }
}
