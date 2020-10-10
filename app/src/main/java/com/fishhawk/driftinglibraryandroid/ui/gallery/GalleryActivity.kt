package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fishhawk.driftinglibraryandroid.databinding.GalleryActivityBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.setupFullScreen
import com.fishhawk.driftinglibraryandroid.ui.extension.setupThemeWithTranslucentStatus

class GalleryActivity : AppCompatActivity() {
    private lateinit var binding: GalleryActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeWithTranslucentStatus()
        super.onCreate(savedInstanceState)
        setupFullScreen()

        binding = GalleryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
