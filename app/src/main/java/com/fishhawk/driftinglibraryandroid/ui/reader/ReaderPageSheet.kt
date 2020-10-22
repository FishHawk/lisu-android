package com.fishhawk.driftinglibraryandroid.ui.reader

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ReaderPageSheetBinding
import com.fishhawk.driftinglibraryandroid.ui.base.makeToast
import com.fishhawk.driftinglibraryandroid.ui.base.saveImageToGallery
import com.fishhawk.driftinglibraryandroid.ui.base.startImageShareActivity
import com.fishhawk.driftinglibraryandroid.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ReaderPageSheet(
    fragment: ReaderFragment,
    page: Int,
    url: String
) : BottomSheetDialog(fragment.requireContext()) {
    private val binding = ReaderPageSheetBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    init {
        setContentView(binding.root)

        binding.page.text = (page + 1).toString()
        binding.refreshButton.setOnClickListener {
            fragment.binding.reader.refreshPage(page)
            dismiss()
        }
        binding.shareButton.setOnClickListener {
            fragment.lifecycleScope.launch {
                val file = FileUtil.downloadImage(context, url)
                fragment.startImageShareActivity(file)
            }
            dismiss()
        }
        binding.saveButton.setOnClickListener {
            with(fragment) {
                val prefix = viewModel.makeImageFilenamePrefix()
                    ?: return@with makeToast(R.string.toast_chapter_not_loaded)
                saveImageToGallery(url, "$prefix-$page")
            }
            dismiss()
        }
    }
}
