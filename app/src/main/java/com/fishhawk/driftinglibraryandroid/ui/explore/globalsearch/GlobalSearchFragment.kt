package com.fishhawk.driftinglibraryandroid.ui.explore.globalsearch

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
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.ui.ViewModelFactory
import kotlinx.coroutines.launch

class GlobalSearchFragment : Fragment() {
    private val viewModel: GlobalSearchViewModel by viewModels {
        ViewModelFactory(requireActivity().application as MainApplication)
    }
    private lateinit var binding: GlobalSearchFragmentBinding

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

        viewModel.keywords = arguments?.getString("keywords")!!
        viewModel.providerList.observe(viewLifecycleOwner, Observer { result ->
            if (result is Result.Success) search()
        })
    }

    private fun search() {
        val keywords = viewModel.keywords
        val providerList = (viewModel.providerList.value as? Result.Success)?.data ?: return
        val adapter =
            GlobalSearchGroupListAdapter(
                requireActivity()
            )
        binding.list.adapter = adapter

        for (provider in providerList) {
            viewLifecycleOwner.lifecycleScope.launch {
                adapter.addResultGroup(provider.name)
                val result = viewModel.search(provider.name, keywords)
                adapter.updateResultFromProvider(provider.name, result)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_global_search, menu)

        val searchView = menu.findItem(R.id.action_search_global).actionView as SearchView
        searchView.queryHint = getString(R.string.menu_search_global_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.keywords = query ?: ""
                search()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean = true
        })
    }
}