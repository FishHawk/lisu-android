package com.fishhawk.driftinglibraryandroid.ui.provider.search

import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.remote.model.OptionModels
import com.fishhawk.driftinglibraryandroid.ui.provider.base.ProviderBaseFragment

class SearchFragment : ProviderBaseFragment() {
    override val viewModel: SearchViewModel by viewModels { getViewModelFactory() }

    override fun getOptionModel(optionModels: OptionModels): Map<String, List<String>> {
        return mapOf()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.search(query)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean = true
        })
        val keywords = arguments?.getString("keywords")!!
        searchView.setQuery(keywords, true)
    }
}
