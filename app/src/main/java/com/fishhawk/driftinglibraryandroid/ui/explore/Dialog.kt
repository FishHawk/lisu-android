package com.fishhawk.driftinglibraryandroid.ui.explore

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fishhawk.driftinglibraryandroid.databinding.DialogMangaOutlineBinding
import com.fishhawk.driftinglibraryandroid.extension.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListFromSourceViewModel


fun Fragment.createMangaOutlineActionDialog(
    source: String,
    outline: MangaOutline,
    viewModel: MangaListFromSourceViewModel
) {
    val dialogBinding =
        DialogMangaOutlineBinding
            .inflate(LayoutInflater.from(context), null, false)

    val dialog = AlertDialog.Builder(requireActivity())
        .setTitle(outline.title)
        .setView(dialogBinding.root)
        .create()

    dialogBinding.readButton.setOnClickListener {
        (requireActivity() as AppCompatActivity).navToReaderActivity(
            outline.id, source, 0, 0, 0
        )
        dialog.dismiss()
    }
    dialogBinding.downloadButton.setOnClickListener {
        viewModel.download(outline.id, outline.title)
        dialog.dismiss()
    }
    dialogBinding.subscribeButton.setOnClickListener {
        viewModel.subscribe(outline.id, outline.title)
        dialog.dismiss()
    }
    dialog.show()
}
