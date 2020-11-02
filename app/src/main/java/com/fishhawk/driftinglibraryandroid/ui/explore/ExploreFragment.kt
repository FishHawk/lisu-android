package com.fishhawk.driftinglibraryandroid.ui.explore

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ExploreFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.closeInputMethod

class ExploreFragment : Fragment() {
    private lateinit var binding: ExploreFragmentBinding
    private val viewModel: ExploreViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

    private val adapter = ProviderInfoListAdapter(object : ProviderInfoListAdapter.Listener {
        override fun onItemClick(providerInfo: ProviderInfo) {
            findNavController().navigate(
                R.id.action_explore_to_provider_pager,
                bundleOf(
                    "providerId" to providerInfo.id,
                    "providerName" to providerInfo.name
                )
            )
            GlobalPreference.lastUsedProvider.setValue(providerInfo.id)
        }

        override fun onBrowseClick(providerInfo: ProviderInfo) {
            findNavController().navigate(
                R.id.action_explore_to_provider_pager,
                bundleOf(
                    "providerId" to providerInfo.id,
                    "providerName" to providerInfo.name
                )
            )
            GlobalPreference.lastUsedProvider.setValue(providerInfo.id)
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

        binding.list.adapter = adapter

        GlobalPreference.lastUsedProvider.observe(viewLifecycleOwner, Observer {
            adapter.lastUsedProviderId = it
        })

        viewModel.providerList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    adapter.infoList = result.data
                    if (binding.list.adapter!!.itemCount == 0) binding.multipleStatusView.showEmpty()
                    else binding.multipleStatusView.showContent()
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                null -> binding.multipleStatusView.showLoading()
            }
        }
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