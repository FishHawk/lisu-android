package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.GalleryThumbSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class GalleryThumbSheet(
    context: Context,
    private val onSaved: () -> Unit,
    private val onEdited: () -> Unit
) : BottomSheetDialog(context) {
    private val binding = GalleryThumbSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)
        binding.saveButton.setOnClickListener { onSaved() }
        binding.editButton.setOnClickListener { onEdited() }
    }
}
