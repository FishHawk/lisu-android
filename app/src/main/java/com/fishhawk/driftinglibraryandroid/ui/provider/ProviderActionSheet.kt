package com.fishhawk.driftinglibraryandroid.ui.provider

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.ProviderActionSheetBinding
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProviderActionSheet(
    context: Context,
    outline: MangaOutline,
    providerId: String,
    private val listener: Listener
) : BottomSheetDialog(context) {
    private val binding = ProviderActionSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)

        binding.title.text = outline.title

        binding.readButton.setOnClickListener {
            listener.onReadClick(outline, providerId)
            dismiss()
        }
        binding.libraryAddButton.setOnClickListener {
            listener.onLibraryAddClick(outline, providerId)
            dismiss()
        }
    }

    interface Listener {
        fun onReadClick(outline: MangaOutline, provider: String)
        fun onLibraryAddClick(outline: MangaOutline, provider: String)
    }
}