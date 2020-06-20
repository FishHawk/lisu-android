package com.fishhawk.driftinglibraryandroid.explore

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.ExploreFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.SpacingItemDecoration
import com.hippo.refreshlayout.RefreshLayout

class ExploreFragment : Fragment() {
    private val viewModel: ExploreViewModel by viewModels {
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        ExploreViewModelFactory(remoteLibraryRepository)
    }
    private lateinit var binding: ExploreFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ExploreFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refreshLayout.apply {
            setOnRefreshListener(object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() {
                    binding.refreshLayout.isHeaderRefreshing = false
                }

                override fun onFooterRefresh() {
                    binding.refreshLayout.isFooterRefreshing = false
                }
            })

            setHeaderColorSchemeResources(
                R.color.loading_indicator_red,
                R.color.loading_indicator_purple,
                R.color.loading_indicator_blue,
                R.color.loading_indicator_cyan,
                R.color.loading_indicator_green,
                R.color.loading_indicator_yellow
            )
            setFooterColorSchemeResources(
                R.color.loading_indicator_red,
                R.color.loading_indicator_blue,
                R.color.loading_indicator_green,
                R.color.loading_indicator_orange
            )
        }

        binding.list.apply {
            addItemDecoration(SpacingItemDecoration(1, 16, true))

            adapter = ExploreListAdapter(requireActivity(), mutableListOf())

            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        val keyword: String? = arguments?.getString("filter")
        viewModel.reloadIfNeed(keyword ?: "")

        viewModel.mangaList.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    (binding.list.adapter!! as ExploreListAdapter).update(result.data.toMutableList())
                    if (binding.list.adapter!!.itemCount == 0) binding.multipleStatusView.showEmpty()
                    else binding.multipleStatusView.showContent()
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                is Result.Loading -> binding.multipleStatusView.showLoading()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_explore, menu)
        val searchView: SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.explore_search_hint)
        if (viewModel.keyword != "") searchView.setQuery(viewModel.keyword, false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.reload(query ?: "")
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return true
            }
        })
    }
}