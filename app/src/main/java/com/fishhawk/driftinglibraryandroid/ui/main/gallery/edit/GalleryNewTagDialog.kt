package com.fishhawk.driftinglibraryandroid.ui.main.gallery.edit

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.fishhawk.driftinglibraryandroid.databinding.GalleryEditNewTagDialogBinding

fun GalleryEditFragment.createNewTagDialog(
    onAccept: (tag: String) -> Unit
) {
    val binding = GalleryEditNewTagDialogBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    val title = "Add tag"

    AlertDialog.Builder(requireActivity())
        .setTitle(title)
        .setView(binding.root)
        .setPositiveButton("OK") { _, _ ->
            val key = binding.key.text.toString()
            val value = binding.value.text.toString()
            val tag = if (key.isBlank()) value else "$key:$value"
            onAccept(tag)
        }
        .setNegativeButton("Cancel") { _, _ -> }
        .show()
}