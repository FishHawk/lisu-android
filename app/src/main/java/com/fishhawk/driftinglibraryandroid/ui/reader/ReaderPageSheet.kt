package com.fishhawk.driftinglibraryandroid.ui.reader

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ReaderPageSheet(
    private val activity: ReaderActivity,
    private val page: Int,
    private val url: String
) : BottomSheetDialog(activity) {
    private val binding = ReaderPageSheetBinding.inflate(activity.layoutInflater, null, false)

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.page.text = (page + 1).toString()
        binding.refreshButton.setOnClickListener { refresh() }
        binding.shareButton.setOnClickListener { share() }
        binding.saveButton.setOnClickListener { save() }
    }

    private fun refresh() {
        activity.refreshImage(page)
        dismiss()
    }

    private fun share() {
        activity.lifecycleScope.launch { activity.shareImage(url) }
        dismiss()
    }

    private fun save() {
        activity.lifecycleScope.launch { activity.saveImage(page, url) }
        dismiss()
    }
}
