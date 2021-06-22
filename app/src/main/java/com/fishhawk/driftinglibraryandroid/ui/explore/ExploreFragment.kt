package com.fishhawk.driftinglibraryandroid.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ExploreFragmentBinding
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.bindToRemoteList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ExploreFragment : Fragment() {
    private lateinit var binding: ExploreFragmentBinding
    private val viewModel: ExploreViewModel by viewModels {
        MainViewModelFactory(this)
    }

    private val adapter = ProviderInfoListAdapter(object : ProviderInfoListAdapter.Listener {
        override fun onItemClick(providerInfo: ProviderInfo) {
            findNavController().navigate(
                R.id.action_explore_to_provider_pager,
                bundleOf("provider" to providerInfo)
            )
            GlobalPreference.lastUsedProvider.set(providerInfo.id)
        }

        override fun onBrowseClick(providerInfo: ProviderInfo) {
            findNavController().navigate(
                R.id.action_explore_to_provider_pager,
                bundleOf("provider" to providerInfo)
            )
            GlobalPreference.lastUsedProvider.set(providerInfo.id)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExploreFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMenu(binding.toolbar.menu)

        binding.recyclerView.adapter = adapter

        GlobalPreference.lastUsedProvider.asFlow()
            .onEach { adapter.lastUsedProviderId = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.multiStateView.onRetry = { viewModel.providers.reload() }

        viewModel.providers.data.observe(viewLifecycleOwner) {
            adapter.infoList = it
        }
        viewModel.providers.state.observe(viewLifecycleOwner) {
            binding.multiStateView.viewState = it
        }
        bindToRemoteList(binding.refreshLayout, viewModel.providers)
    }

    private fun setupMenu(menu: Menu) {
        with(menu.findItem(R.id.action_search).actionView as SearchView) {
            queryHint = getString(R.string.menu_search_global_hint)
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    isIconified = true
                    isIconified = true
                    binding.root.findNavController().navigate(
                        R.id.action_explore_to_global_search,
                        bundleOf("keywords" to query)
                    )
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean = true
            })
            setOnQueryTextFocusChangeListener { _, b ->
                if (!b && query.isNullOrBlank()) {
                    isIconified = true
                    isIconified = true
                }
            }
        }
    }
}