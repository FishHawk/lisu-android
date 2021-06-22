package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchFragmentBinding
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.closeInputMethod

class GlobalSearchFragment : Fragment() {
    private lateinit var binding: GlobalSearchFragmentBinding
    private val viewModel: GlobalSearchViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication, requireArguments())
    }

    private val adapter = GlobalSearchGroupListAdapter(
        object : GlobalSearchGroupListAdapter.Listener {
            override fun onItemClicked(info: ProviderInfo, outline: MangaOutline) {
                findNavController().navigate(
                    R.id.action_to_gallery_detail,
                    bundleOf(
                        "outline" to outline,
                        "providerId" to info.id
                    )
                )
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GlobalSearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMenu(binding.toolbar.menu)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.recyclerView.adapter = adapter

        viewModel.searchGroupList.observe(viewLifecycleOwner) { adapter.setList(it) }
    }

    private fun setupMenu(menu: Menu) {
        with(menu.findItem(R.id.action_search_global).actionView as SearchView) {
            queryHint = getString(R.string.menu_search_global_hint)
            maxWidth = Int.MAX_VALUE
            setQuery(viewModel.keywords.value, false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.keywords.value = query
                    closeInputMethod()
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean = true
            })
        }
    }
}