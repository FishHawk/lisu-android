package com.fishhawk.driftinglibraryandroid.ui.server

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ServerFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory

class ServerFragment : Fragment() {
    private lateinit var binding: ServerFragmentBinding
    private val viewModel: ServerViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

    val adapter = ServerInfoListAdapter(object : ServerInfoListAdapter.Listener {
        override fun onItemClick(info: ServerInfo) {
            SettingsHelper.selectedServer.setValue(info.id)
        }

        override fun onServerDelete(info: ServerInfo) {
            viewModel.deleteServer(info)
        }

        override fun onServerEdit(info: ServerInfo) {
            createServerInfoDialog(info) { name, address ->
                info.name = name
                info.address = address
                viewModel.updateServer(info)
            }
        }

        override fun onDragFinish() {
            // wait LiveData update
            // saveServerListOrder()
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ServerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.list.adapter = adapter

        SettingsHelper.selectedServer.observe(viewLifecycleOwner) { id ->
            adapter.selectedId = id
        }

        viewModel.serverInfoList.observe(viewLifecycleOwner) { data ->
            if (data.size == 1) SettingsHelper.selectedServer.setValue(data.first().id)
            adapter.setList(data)
            if (data.isEmpty()) binding.multipleStatusView.showEmpty()
            else binding.multipleStatusView.showContent()
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                createServerInfoDialog { name, address ->
                    viewModel.addServer(ServerInfo(name, address))
                }
                true
            }
            else -> false
        }
    }
}