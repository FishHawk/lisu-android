package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.ProviderActionSheetBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.extension.navToReaderActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProviderActionSheet(
    fragment: ProviderBaseFragment,
    providerId: String,
    outline: MangaOutline
) : BottomSheetDialog(fragment.requireContext()) {
    private val binding = ProviderActionSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)

        binding.title.text = outline.title

        binding.readButton.setOnClickListener {
            fragment.navToReaderActivity(outline.id, providerId, 0, 0, 0)
            dismiss()
        }
        binding.downloadButton.setOnClickListener {
            fragment.viewModel.download(outline.id, outline.title)
            dismiss()
        }
        binding.subscribeButton.setOnClickListener {
            fragment.viewModel.subscribe(outline.id, outline.title)
            dismiss()
        }
    }
}