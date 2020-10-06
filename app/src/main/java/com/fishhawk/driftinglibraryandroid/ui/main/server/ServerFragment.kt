package com.fishhawk.driftinglibraryandroid.ui.main.server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.databinding.ServerFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.main.MainViewModelFactory

class ServerFragment : Fragment() {
    private lateinit var binding: ServerFragmentBinding
    private val viewModel: ServerViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

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
        adapter.onEdited = {
            createServerInfoDialog(it) { name, address ->
                it.name = name
                it.address = address
                viewModel.updateServer(it)
            }
        }
        adapter.onDeleted = { viewModel.deleteServer(it) }
        adapter.onItemClicked = { SettingsHelper.selectedServer.setValue(it.id) }
        binding.list.adapter = adapter

        SettingsHelper.selectedServer.observe(viewLifecycleOwner, Observer { id ->
            adapter.selectedId = id
        })

        viewModel.serverInfoList.observe(viewLifecycleOwner, Observer { data ->
            if (data.size == 1) SettingsHelper.selectedServer.setValue(data.first().id)
            adapter.setList(data.toMutableList())
            if (data.isEmpty()) binding.multipleStatusView.showEmpty()
            else binding.multipleStatusView.showContent()
        })

        binding.addButton.setOnClickListener {
            createServerInfoDialog { name, address ->
                viewModel.addServer(ServerInfo(name, address))
            }
        }
    }
}