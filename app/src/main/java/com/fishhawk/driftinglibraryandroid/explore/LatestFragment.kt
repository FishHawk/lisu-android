package com.fishhawk.driftinglibraryandroid.explore

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.MangaListAdapter
import com.fishhawk.driftinglibraryandroid.databinding.ExploreLatestFragmentBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.SpacingItemDecoration
import com.hippo.refreshlayout.RefreshLayout

class LatestFragment : Fragment() {
    private val viewModel: LatestViewModel by viewModels {
        val source = arguments?.getString("source")!!
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        ExploreViewModelFactory(source, remoteLibraryRepository)
    }
    private lateinit var binding: ExploreLatestFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ExploreLatestFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refreshLayout.apply {
            setOnRefreshListener(object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() = viewModel.refresh()
                override fun onFooterRefresh() = viewModel.fetchMore()
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
            layoutManager = LinearLayoutManager(context)
            adapter =
                MangaListAdapter(requireActivity(), mutableListOf()).apply { setViewTypeLinear() }
        }

        viewModel.load()

        viewModel.mangaList.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    (binding.list.adapter!! as MangaListAdapter).update(result.data.toMutableList())
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
    }
}