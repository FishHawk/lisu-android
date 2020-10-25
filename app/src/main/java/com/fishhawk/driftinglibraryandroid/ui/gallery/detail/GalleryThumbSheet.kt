package com.fishhawk.driftinglibraryandroid.ui.gallery.detail

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.GalleryThumbSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class GalleryThumbSheet(
    context: Context,
    private val listener: Listener
) : BottomSheetDialog(context) {
    private val binding = GalleryThumbSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)

        binding.editButton.setOnClickListener {
            listener.onEdit()
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            listener.onSave()
            dismiss()
        }

        binding.shareButton.setOnClickListener {
            listener.onShare()
            dismiss()
        }
    }

    interface Listener {
        fun onEdit()
        fun onSave()
        fun onShare()
    }
}
