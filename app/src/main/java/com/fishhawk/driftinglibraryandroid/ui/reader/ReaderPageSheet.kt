package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageSheetBinding
import com.fishhawk.driftinglibraryandroid.ui.base.makeToast
import com.fishhawk.driftinglibraryandroid.ui.base.saveImage
import com.fishhawk.driftinglibraryandroid.ui.base.shareImage
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ReaderPageSheet(
    context: Context,
    page: Int,
    private val listener: Listener
) : BottomSheetDialog(context) {
    private val binding = ReaderPageSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)

        binding.page.text = (page + 1).toString()

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
