package com.fishhawk.driftinglibraryandroid.ui.server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.databinding.ServerFragmentBinding
import com.fishhawk.driftinglibraryandroid.databinding.ServerInfoDialogBinding
import com.fishhawk.driftinglibraryandroid.repository.data.ServerInfo
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.ViewModelFactory


class ServerFragment : Fragment() {
    private val viewModel: ServerViewModel by viewModels {
        ViewModelFactory(requireActivity().application as MainApplication)
    }
    private lateinit var binding: ServerFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ServerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            ServerInfoListAdapter(
                requireActivity()
            )
        adapter.onEdit = {
            createServerInfoDialog(it) { name, address ->
                it.name = name
                it.address = address
                viewModel.updateServer(it)
            }
        }
        adapter.onDelete = { viewModel.deleteServer(it) }
        adapter.onCardClicked = { SettingsHelper.selectedServer.setValue(it.id) }
        binding.list.adapter = adapter

        SettingsHelper.selectedServer.observe(viewLifecycleOwner, Observer { id ->
            adapter.selectedId = id
        })

        viewModel.serverInfoList.observe(viewLifecycleOwner, Observer { data ->
            if (data.size == 1) SettingsHelper.selectedServer.setValue(data.first().id)
            adapter.changeList(data.toMutableList())
            if (data.isEmpty()) binding.multipleStatusView.showEmpty()
            else binding.multipleStatusView.showContent()
        })

        binding.addButton.setOnClickListener {
            createServerInfoDialog { name, address ->
                viewModel.addServer(ServerInfo(name, address))
            }
        }
    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.menu_subscribe, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_enable_all -> viewModel.enableAllSubscription()
//            R.id.action_disable_all -> viewModel.disableAllSubscription()
//            else -> return super.onOptionsItemSelected(item)
//        }
//        return true
//    }


    private fun createServerInfoDialog(
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
}