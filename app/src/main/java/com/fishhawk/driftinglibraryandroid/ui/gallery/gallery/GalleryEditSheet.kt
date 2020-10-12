package com.fishhawk.driftinglibraryandroid.ui.gallery.gallery

import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.GalleryEditSheetBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.google.android.material.bottomsheet.BottomSheetDialog

class GalleryEditSheet(
    fragment: GalleryFragment
) : BottomSheetDialog(fragment.requireContext()) {
    private val binding = GalleryEditSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)

        val result = fragment.viewModel.detail.value
        if (result is Result.Success) {
            val detail = result.data
            binding.id.text = detail.id
            binding.title.setText(detail.title)
            binding.description.setText(detail.metadata.description)
        }
    }
}