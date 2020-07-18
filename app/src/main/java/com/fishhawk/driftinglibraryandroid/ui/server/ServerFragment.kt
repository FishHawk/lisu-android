package com.fishhawk.driftinglibraryandroid.ui.server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.databinding.ServerFragmentBinding
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

        val adapter = ServerInfoListAdapter(requireActivity())
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
}