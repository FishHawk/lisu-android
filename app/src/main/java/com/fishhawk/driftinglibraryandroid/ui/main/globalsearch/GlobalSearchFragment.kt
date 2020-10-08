package com.fishhawk.driftinglibraryandroid.ui.main.globalsearch

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.GlobalSearchFragmentBinding
import com.fishhawk.driftinglibraryandroid.ui.extension.navToGalleryActivity
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.ui.main.MainViewModelFactory
import kotlinx.coroutines.launch

class GlobalSearchFragment : Fragment() {
    private lateinit var binding: GlobalSearchFragmentBinding
    private val viewModel: GlobalSearchViewModel by viewModels {
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
        binding = GlobalSearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = GlobalSearchGroupListAdapter(requireContext())
        adapter.onItemClicked = { provider, outline ->
            requireActivity().navToGalleryActivity(outline, provider.id)
        }
        binding.list.adapter = adapter

        viewModel.providerList.observe(viewLifecycleOwner, Observer { result ->
            if (result is Result.Success) {
                adapter.setListWithEmptyItem(result.data)
                viewModel.keywords.value = viewModel.keywords.value
            }
        })
        viewModel.keywords.observe(viewLifecycleOwner, Observer { keywords ->
            (viewModel.providerList.value as? Result.Success)?.data?.forEach {
                viewLifecycleOwner.lifecycleScope.launch {
                    adapter.setSearchGroupResult(it, Result.Loading)
                    val result = viewModel.search(it.id, keywords)
                    if (viewModel.keywords.value == keywords)
                        adapter.setSearchGroupResult(it, result)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_global_search, menu)

        val searchView = menu.findItem(R.id.action_search_global).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_global_hint)
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.keywords.value = query
                return true
            }

            override fun onQueryTextChange(query: String): Boolean = true
        })
        searchView.setQuery(arguments?.getString("keywords")!!, true)
    }
}