package com.fishhawk.driftinglibraryandroid.ui.main.gallery.edit

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.fishhawk.driftinglibraryandroid.databinding.GalleryEditNewAuthorDialogBinding

fun GalleryEditFragment.createNewAuthorDialog(
    onAccept: (author: String) -> Unit
) {
    val binding = GalleryEditNewAuthorDialogBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    val title = "Add author"

    AlertDialog.Builder(requireActivity())
        .setTitle(title)
        .setView(binding.root)
        .setPositiveButton("OK") { _, _ ->
            onAccept(binding.author.text.toString())
        }
        .setNegativeButton("Cancel") { _, _ -> }
        .show()
}