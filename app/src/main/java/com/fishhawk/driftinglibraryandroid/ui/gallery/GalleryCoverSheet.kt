package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.fishhawk.driftinglibraryandroid.databinding.GalleryCoverSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class GalleryCoverSheet(
    context: Context,
    private val listener: Listener
) : BottomSheetDialog(context) {
    private val binding = GalleryCoverSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    var isFromProvider = false
        set(value) {
            field = value
            updateVisibility()
        }

    var hasSource = false
        set(value) {
            field = value
            updateVisibility()
        }

    private fun updateVisibility() {
        binding.editCoverButton.isVisible = !isFromProvider
        binding.syncSourceButton.isVisible = !isFromProvider && hasSource
        binding.deleteSourceButton.isVisible = !isFromProvider && hasSource
    }

    init {
        setContentView(binding.root)

        binding.syncSourceButton.setOnClickListener {
            listener.onSyncSource()
            dismiss()
        }

        binding.deleteSourceButton.setOnClickListener {
            listener.onDeleteSource()
            dismiss()
        }

        binding.editMetadataButton.setOnClickListener {
            listener.onEditMetadata()
            dismiss()
        }

        binding.editCoverButton.setOnClickListener {
            listener.onEditCover()
            dismiss()
        }

        binding.saveCoverButton.setOnClickListener {
            listener.onSaveCover()
            dismiss()
        }

        binding.shareCoverButton.setOnClickListener {
            listener.onShareCover()
            dismiss()
        }
    }

    interface Listener {
        fun onSyncSource()
        fun onDeleteSource()
        fun onEditMetadata()
        fun onEditCover()
        fun onSaveCover()
        fun onShareCover()
    }
}
