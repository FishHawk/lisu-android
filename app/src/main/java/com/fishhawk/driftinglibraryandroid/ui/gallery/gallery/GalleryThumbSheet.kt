package com.fishhawk.driftinglibraryandroid.ui.gallery.gallery

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GalleryThumbSheetBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.ui.base.makeToast
import com.fishhawk.driftinglibraryandroid.ui.extension.saveImageToGallery
import com.fishhawk.driftinglibraryandroid.ui.extension.startImagePickActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class GalleryThumbSheet(
    fragment: GalleryFragment
) : BottomSheetDialog(fragment.requireContext()) {
    private val binding = GalleryThumbSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)
        binding.saveButton.setOnClickListener {
            with(fragment) {
                val detail = (viewModel.detail.value as? Result.Success)?.data
                    ?: return@with makeToast(R.string.toast_manga_not_loaded)
                val url = detail.thumb
                    ?: return@with makeToast(R.string.toast_manga_no_thumb)
                saveImageToGallery(url, "${detail.id}-thumb")
            }
            dismiss()
        }
        binding.editButton.setOnClickListener {
            fragment.startImagePickActivity()
            dismiss()
        }
    }
}
