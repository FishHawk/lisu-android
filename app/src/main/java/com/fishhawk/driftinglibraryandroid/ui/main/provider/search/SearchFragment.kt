package com.fishhawk.driftinglibraryandroid.ui.main.provider.search

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ProviderSearchFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.ui.extension.*
import com.fishhawk.driftinglibraryandroid.ui.main.provider.ProviderViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.main.provider.base.ProviderActionSheet

class SearchFragment : Fragment() {
    val viewModel: SearchViewModel by viewModels {
        providerId = requireArguments().getString("providerId")!!
        val application = requireActivity().application as MainApplication
        ProviderViewModelFactory(providerId, application)
    }
    private lateinit var binding: ProviderSearchFragmentBinding

    private lateinit var providerId: String

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
            findNavController().navigate(
                R.id.action_to_gallery,
                bundleOf(
                    "id" to outline.id,
                    "title" to outline.title,
                    "thumb" to outline.thumb,
                    "providerId" to providerId
                )
            )
        }

        override fun onCardLongClick(outline: MangaOutline) {
            ProviderActionSheet(requireContext(), outline, providerId, actionAdapter).show()
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProviderSearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMenu(binding.toolbar.menu)
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.mangaList.list.adapter = mangaAdapter

        SettingsHelper.displayMode.observe(viewLifecycleOwner) {
            binding.mangaList.list.changeMangaListDisplayMode(mangaAdapter)
        }

        bindToListViewModel(
            binding.mangaList.multipleStatusView,
            binding.mangaList.refreshLayout,
            viewModel,
            mangaAdapter
        )
    }

    private fun setupMenu(menu: Menu) {
        with(menu.findItem(R.id.action_search).actionView as SearchView) {
            queryHint = getString(R.string.menu_search_hint)
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.search(query)
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean = true
            })
            val keywords = requireArguments().getString("keywords")!!
            setQuery(keywords, true)
        }
        with(menu.findItem(R.id.action_display_mode)) {
            setIcon(getDisplayModeIcon())
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_display_mode -> {
                SettingsHelper.displayMode.setNextValue()
                item.setIcon(getDisplayModeIcon())
                true
            }
            else -> false
        }
    }
}
