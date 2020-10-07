package com.fishhawk.driftinglibraryandroid.ui.provider.category

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderCategoryFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.extension.changeMangaListDisplayMode
import com.fishhawk.driftinglibraryandroid.ui.extension.getDisplayModeIcon
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.base.OptionGroupListAdapter
import com.fishhawk.driftinglibraryandroid.ui.provider.base.ProviderOptionSheet
import com.fishhawk.driftinglibraryandroid.ui.provider.createMangaOutlineActionDialog

class CategoryFragment : Fragment() {
    private val providerViewModel: ProviderViewModel by activityViewModels {
        val providerId = requireActivity().intent.extras!!.getString("providerId")!!
        ProviderViewModelFactory(providerId, requireActivity().application as MainApplication)
    }
    private val viewModel: CategoryViewModel by viewModels {
        val providerId = requireActivity().intent.extras!!.getString("providerId")!!
        ProviderViewModelFactory(providerId, requireActivity().application as MainApplication)
    }
    private lateinit var binding: ProviderCategoryFragmentBinding
    private lateinit var optionSheet: ProviderOptionSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProviderCategoryFragmentBinding.inflate(inflater, container, false)
        optionSheet = ProviderOptionSheet(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val providerId = requireActivity().intent.extras!!.getString("providerId")!!
        val adapter = MangaListAdapter(requireActivity(), providerId)
        adapter.onCardLongClicked = { outline ->
            createMangaOutlineActionDialog(providerId, outline, viewModel)
        }
        binding.mangaList.list.adapter = adapter

        val optionGroupListAdapter = OptionGroupListAdapter(requireActivity())
        optionGroupListAdapter.onOptionSelected = { name, index ->
            viewModel.selectOption(name, index)
            viewModel.load()
        }
        optionSheet.setAdapter(optionGroupListAdapter)

        SettingsHelper.displayMode.observe(viewLifecycleOwner, Observer {
            binding.mangaList.list.changeMangaListDisplayMode(adapter)
        })

        bindToListViewModel(
            binding.mangaList.multipleStatusView,
            binding.mangaList.refreshLayout,
            viewModel,
            adapter
        )
        providerViewModel.detail.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Result.Success -> {
                    optionGroupListAdapter.setList(it.data.optionModels.category.toList())
                    it.data.optionModels.category.forEach { (key, _) ->
                        viewModel.selectOption(key, 0)
                    }
                    if (viewModel.list.value == null) viewModel.load()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_provider_normal, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.setQuery("", false)
                findNavController().navigate(
                    R.id.action_category_to_search,
                    bundleOf("keywords" to query)
                )
                return true
            }

            override fun onQueryTextChange(query: String): Boolean = true
        })

        val item = menu.findItem(R.id.action_display_mode)
        item.setIcon(getDisplayModeIcon())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                SettingsHelper.displayMode.setNextValue()
                item.setIcon(getDisplayModeIcon())
                true
            }
            R.id.action_option -> {
                optionSheet.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
