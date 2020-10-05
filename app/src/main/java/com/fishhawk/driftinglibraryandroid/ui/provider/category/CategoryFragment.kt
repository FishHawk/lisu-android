package com.fishhawk.driftinglibraryandroid.ui.provider.category

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderCategoryFragmentBinding
import com.fishhawk.driftinglibraryandroid.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.extension.changeMangaListDisplayMode
import com.fishhawk.driftinglibraryandroid.extension.getDisplayModeIcon
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.base.OptionGroupListAdapter
import com.fishhawk.driftinglibraryandroid.ui.provider.createMangaOutlineActionDialog

class CategoryFragment : Fragment() {
    private val providerViewModel: ProviderViewModel by activityViewModels()
    private val viewModel: CategoryViewModel by viewModels {
        val providerId = requireActivity().intent.extras!!.getString("providerId")!!
        ProviderViewModelFactory(
            providerId,
            requireActivity().application as MainApplication
        )
    }
    private lateinit var binding: ProviderCategoryFragmentBinding

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
        }
        binding.options.adapter = optionGroupListAdapter

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
                    optionGroupListAdapter.changeList(
                        it.data.optionModels.category.toList().toMutableList()
                    )
                    it.data.optionModels.category.forEach { (key, _value) ->
                        viewModel.selectOption(key, 0)
                    }
                    viewModel.load()
                }
                else -> {
                    // TODO: set error page
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_provider_normal, menu)

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
            else -> super.onOptionsItemSelected(item)
        }
    }
}
