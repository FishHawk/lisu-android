package com.fishhawk.driftinglibraryandroid.ui.provider.base

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderBaseFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.ui.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.extension.changeMangaListDisplayMode
import com.fishhawk.driftinglibraryandroid.ui.extension.getDisplayModeIcon
import com.fishhawk.driftinglibraryandroid.ui.extension.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderActivity
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModel
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderViewModelFactory

abstract class ProviderBaseFragment : Fragment() {
    private val providerViewModel: ProviderViewModel by activityViewModels { getViewModelFactory() }
    abstract val viewModel: ProviderBaseViewModel
    private lateinit var binding: ProviderBaseFragmentBinding

    val providerId = requireActivity().intent.extras!!.getString("providerId")!!

    private var hasOption = false
    private lateinit var optionSheet: ProviderOptionSheet
    private val optionAdapter = OptionGroupListAdapter(object : OptionGroupListAdapter.Listener {
        override fun onOptionSelect(name: String, index: Int) {
            viewModel.selectOption(name, index)
            viewModel.load()
        }
    })

    private val actionAdapter = object : ProviderActionSheet.Listener {
        override fun onReadClick(outline: MangaOutline, provider: String) {
            navToReaderActivity(outline.id, providerId, 0, 0, 0)
        }

        override fun onDownloadClick(outline: MangaOutline, provider: String) {
            viewModel.download(outline.id, outline.title)
        }

        override fun onSubscribeClick(outline: MangaOutline, provider: String) {
            viewModel.subscribe(outline.id, outline.title)
        }
    }

    private val mangaAdapter = MangaListAdapter(object : MangaListAdapter.Listener {
        override fun onCardClick(outline: MangaOutline) {
            ProviderActionSheet(requireContext(), outline, providerId, actionAdapter).show()
        }

        override fun onCardLongClick(outline: MangaOutline) {}
    })

    abstract fun getOptionModel(optionModels: OptionModels): Map<String, List<String>>

    protected fun getViewModelFactory(): ProviderViewModelFactory {
        val providerId = requireActivity().intent.extras!!.getString("providerId")!!
        val application = requireActivity().application as MainApplication
        return ProviderViewModelFactory(providerId, application)
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
        binding = ProviderBaseFragmentBinding.inflate(inflater, container, false)
        optionSheet = ProviderOptionSheet(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.mangaList.list.adapter = mangaAdapter

        optionSheet.setAdapter(optionAdapter)

        SettingsHelper.displayMode.observe(viewLifecycleOwner, Observer {
            binding.mangaList.list.changeMangaListDisplayMode(mangaAdapter)
        })

        bindToListViewModel(
            binding.mangaList.multipleStatusView,
            binding.mangaList.refreshLayout,
            viewModel,
            mangaAdapter
        )
        providerViewModel.detail.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    val model = getOptionModel(result.data.optionModels)
                    if (model.toList().isNotEmpty()) {
                        hasOption = true
                        requireActivity().invalidateOptionsMenu()
                    }
                    optionAdapter.setList(model.toList())
                    model.keys.forEach { key -> viewModel.selectOption(key, 0) }

                    if (viewModel.list.value == null) viewModel.load()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_provider, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_hint)
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.setQuery("", false)
                findNavController().navigate(
                    R.id.action_to_search,
                    bundleOf("keywords" to query)
                )
                return true
            }

            override fun onQueryTextChange(query: String): Boolean = true
        })

        val keywords = (requireActivity() as ProviderActivity).keywords
        if (keywords != null) searchView.setQuery(keywords, true)

        menu.findItem(R.id.action_option).isVisible = hasOption
        menu.findItem(R.id.action_display_mode).setIcon(getDisplayModeIcon())
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
