package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class ReaderPageSheet(
    context: Context,
    private val listener: Listener
) : BottomSheetDialog(context) {

    private val binding = ReaderPageSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)

        binding.refreshButton.setOnClickListener {
            listener.onRefresh()
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
        fun onRefresh()
        fun onSave()
        fun onShare()
    }
}
