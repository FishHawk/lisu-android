package com.fishhawk.driftinglibraryandroid.more

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ServerAddDialogBinding
import com.fishhawk.driftinglibraryandroid.databinding.ServerFragmentBinding
import com.fishhawk.driftinglibraryandroid.history.HistoryListAdapter
import com.fishhawk.driftinglibraryandroid.repository.data.ServerInfo
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper

class ServerFragment : Fragment() {
    private val viewModel: ServerViewModel by viewModels {
        val application = requireContext().applicationContext as MainApplication
        val serverInfoRepository = application.serverInfoRepository
        ServerViewModelFactory(serverInfoRepository)
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

        val adapter = ServerInfoListAdapter(requireActivity())
        adapter.onEdit = {
            val dialogBinding =
                ServerAddDialogBinding.inflate(LayoutInflater.from(context), null, false)
            dialogBinding.name.setText(it.name)
            dialogBinding.address.setText(it.address)
            AlertDialog.Builder(requireActivity())
                .setTitle("Edit server")
                .setView(dialogBinding.root)
                .setPositiveButton("OK") { _, _ ->
                    it.name = dialogBinding.name.text.toString()
                    it.address = dialogBinding.address.text.toString()
                    viewModel.updateServer(it)
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
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
            val dialogBinding =
                ServerAddDialogBinding.inflate(LayoutInflater.from(context), null, false)
            AlertDialog.Builder(requireActivity())
                .setTitle("Add server")
                .setView(dialogBinding.root)
                .setPositiveButton("OK") { _, _ ->
                    viewModel.addServer(
                        ServerInfo(
                            dialogBinding.name.text.toString(),
                            dialogBinding.address.text.toString()
                        )
                    )
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
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
}