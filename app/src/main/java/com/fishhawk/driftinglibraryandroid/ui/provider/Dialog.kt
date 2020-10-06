package com.fishhawk.driftinglibraryandroid.ui.provider

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fishhawk.driftinglibraryandroid.databinding.DialogMangaOutlineBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.provider.base.MangaListViewModel

fun Fragment.createMangaOutlineActionDialog(
    providerId: String,
    outline: MangaOutline,
    viewModel: MangaListViewModel
) {
    val dialogBinding =
        DialogMangaOutlineBinding
            .inflate(LayoutInflater.from(context), null, false)

    val dialog = AlertDialog.Builder(requireActivity())
        .setTitle(outline.metadata.title ?: outline.id)
        .setView(dialogBinding.root)
        .create()

    dialogBinding.readButton.setOnClickListener {
        (requireActivity() as AppCompatActivity).navToReaderActivity(
            outline.id, providerId, 0, 0, 0
        )
        dialog.dismiss()
    }
    dialogBinding.downloadButton.setOnClickListener {
        viewModel.download(outline.id, outline.metadata.title ?: outline.id)
        dialog.dismiss()
    }
    dialogBinding.subscribeButton.setOnClickListener {
        viewModel.subscribe(outline.id, outline.metadata.title ?: outline.id)
        dialog.dismiss()
    }
    dialog.show()
}
