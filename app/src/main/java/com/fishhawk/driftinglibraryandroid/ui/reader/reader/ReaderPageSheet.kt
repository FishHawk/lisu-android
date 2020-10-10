package com.fishhawk.driftinglibraryandroid.ui.reader.reader

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class ReaderPageSheet(
    context: Context,
    page: Int,
    private val onRefreshed: () -> Unit,
    private val onShared: () -> Unit,
    private val onSaved: () -> Unit
) : BottomSheetDialog(context) {
    private val binding = ReaderPageSheetBinding.inflate(LayoutInflater.from(context), null, false)

    init {
        setContentView(binding.root)

        binding.page.text = (page + 1).toString()
        binding.refreshButton.setOnClickListener {
            onRefreshed()
            dismiss()
        }
        binding.shareButton.setOnClickListener {
            onShared()
            dismiss()
        }
        binding.saveButton.setOnClickListener {
            onSaved()
            dismiss()
        }
    }
}
