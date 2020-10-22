package com.fishhawk.driftinglibraryandroid.ui.main.library

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.LibraryFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.ui.extension.bindToListViewModel
import com.fishhawk.driftinglibraryandroid.ui.extension.changeMangaListDisplayMode
import com.fishhawk.driftinglibraryandroid.ui.extension.getDisplayModeIcon
import com.fishhawk.driftinglibraryandroid.ui.extension.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.ui.main.MainViewModelFactory

class LibraryFragment : Fragment() {
    private lateinit var binding: LibraryFragmentBinding
    private val viewModel: LibraryViewModel by viewModels {
        MainViewModelFactory(requireActivity().application as MainApplication)
    }

    val adapter = MangaListAdapter(object : MangaListAdapter.Listener {
        override fun onCardClick(outline: MangaOutline) {
            navToGalleryActivity(outline, null)
        }

        override fun onCardLongClick(outline: MangaOutline) {
            AlertDialog.Builder(requireActivity())
                .setTitle("Confirm to delete manga?")
                .setPositiveButton("OK") { _, _ -> viewModel.deleteManga(outline.id) }
                .setNegativeButton("cancel") { _, _ -> }
                .show()
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LibraryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMenu(binding.toolbar.menu)
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemSelected)

        binding.mangaList.list.adapter = adapter

        SettingsHelper.displayMode.observe(viewLifecycleOwner) {
            binding.mangaList.list.changeMangaListDisplayMode(adapter)
        }

        bindToListViewModel(
            binding.mangaList.multipleStatusView,
            binding.mangaList.refreshLayout,
            viewModel,
            adapter
        )

        val keywords = requireActivity().intent.extras?.getString("keywords")
        viewModel.reloadIfNeed(keywords ?: "")
    }

    private fun setupMenu(menu: Menu) {
        with(menu.findItem(R.id.action_search).actionView as SearchView) {
            queryHint = getString(R.string.menu_search_hint)
            maxWidth = Int.MAX_VALUE
            if (viewModel.filter != "") setQuery(viewModel.filter, false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.reload(query)
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean = false
            })
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