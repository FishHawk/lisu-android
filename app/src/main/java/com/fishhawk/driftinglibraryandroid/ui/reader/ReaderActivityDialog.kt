package com.fishhawk.driftinglibraryandroid.ui.reader

import android.content.Intent
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.databinding.DialogChapterImageBinding
import com.fishhawk.driftinglibraryandroid.extension.makeToast
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.util.FileUtil
import kotlinx.coroutines.launch


fun ReaderActivity.createChapterImageActionDialog(page: Int, url: String) {
    val dialogBinding =
        DialogChapterImageBinding
            .inflate(LayoutInflater.from(this), null, false)

    val dialog = AlertDialog.Builder(this)
        .setTitle("Page ${page + 1}")
        .setView(dialogBinding.root)
        .create()

    dialogBinding.refreshButton.setOnClickListener {
        dialog.dismiss()
        if (SettingsHelper.readingDirection.getValueDirectly() == SettingsHelper.READING_DIRECTION_VERTICAL)
            binding.verticalReader.adapter?.notifyItemChanged(page)
        else
            binding.horizontalReader.adapter?.notifyItemChanged(page)
    }
    dialogBinding.shareButton.setOnClickListener {
        dialog.dismiss()
        lifecycleScope.launch { shareImage(url) }
    }
    dialogBinding.saveButton.setOnClickListener {
        dialog.dismiss()
        lifecycleScope.launch { saveImage(page, url) }
    }
    dialog.show()
}

private suspend fun ReaderActivity.shareImage(url: String) {
    val file = FileUtil.downloadImage(this, url)

    val uri = FileProvider.getUriForFile(
        this, "$packageName.fileprovider", file
    )
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    startActivity(Intent.createChooser(shareIntent, "Share image"))
}

private suspend fun ReaderActivity.saveImage(page: Int, url: String) {
    val prefix = viewModel.makeImageFilenamePrefix()
    if (prefix == null) {
        binding.root.makeToast("Chapter not open")
    } else {
        try {
            FileUtil.saveImage(this, url, "$prefix-$page")
            binding.root.makeToast("Image saved")
        } catch (e: Throwable) {
            binding.root.makeToast(e.message ?: "Unknown error")
        }
    }
}