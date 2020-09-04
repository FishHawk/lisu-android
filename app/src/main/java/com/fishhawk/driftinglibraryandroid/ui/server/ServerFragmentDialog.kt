package com.fishhawk.driftinglibraryandroid.ui.server

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.fishhawk.driftinglibraryandroid.databinding.ServerInfoDialogBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo

fun ServerFragment.createServerInfoDialog(
    serverInfo: ServerInfo? = null,
    onAccept: (name: String, address: String) -> Unit
) {
    val dialogBinding =
        ServerInfoDialogBinding
            .inflate(LayoutInflater.from(context), null, false)

    val title = if (serverInfo == null) "Add server" else "Edit server"

    if (serverInfo != null) {
        dialogBinding.name.setText(serverInfo.name)
        dialogBinding.address.setText(serverInfo.address)
    }

    AlertDialog.Builder(requireActivity())
        .setTitle(title)
        .setView(dialogBinding.root)
        .setPositiveButton("OK") { _, _ ->
            onAccept(
                dialogBinding.name.text.toString(),
                dialogBinding.address.text.toString()
            )
        }
        .setNegativeButton("Cancel") { _, _ -> }
        .show()
}