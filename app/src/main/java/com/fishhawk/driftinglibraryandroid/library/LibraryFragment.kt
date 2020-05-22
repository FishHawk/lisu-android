package com.fishhawk.driftinglibraryandroid.library

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.LibraryFragmentBinding
import com.fishhawk.driftinglibraryandroid.gallery.GalleryActivity
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.util.EventObserver
import com.fishhawk.driftinglibraryandroid.util.SpacingItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.hippo.refreshlayout.RefreshLayout


class LibraryFragment : Fragment() {
    private val viewModel: LibraryViewModel by viewModels {
        val application = requireContext().applicationContext as MainApplication
        val remoteLibraryRepository = application.remoteLibraryRepository
        LibraryViewModelFactory(remoteLibraryRepository)
    }
    private lateinit var binding: LibraryFragmentBinding

    private var mColumnCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LibraryFragmentBinding.inflate(inflater, container, false)
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
            // set layout manager
            layoutManager =
                if (mColumnCount <= 1) LinearLayoutManager(context)
                else GridLayoutManager(context, mColumnCount)

            addItemDecoration(SpacingItemDecoration(mColumnCount, 16, true))

            // set adapter
            adapter = LibraryListAdapter(context, mutableListOf()) { item, imageView ->
                val bundle = bundleOf(
                    "id" to item.id,
                    "title" to item.title,
                    "thumb" to item.thumb
                )

                val intent = Intent(activity, GalleryActivity::class.java)
                intent.putExtras(bundle)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    imageView,
                    ViewCompat.getTransitionName(imageView)!!
                )
//                startActivity(intent, options.toBundle())
                startActivity(intent)
            }

            // set transition
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        val filter: String? = arguments?.getString("filter")
        viewModel.reloadIfNeed(filter ?: "")

        viewModel.mangaList.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    (binding.list.adapter!! as LibraryListAdapter).update(result.data.toMutableList())
                    if (binding.list.adapter!!.itemCount == 0) binding.multipleStatusView.showEmpty()
                    else binding.multipleStatusView.showContent()
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                is Result.Loading -> binding.multipleStatusView.showLoading()
            }
        })

        viewModel.refreshFinish.observe(viewLifecycleOwner, EventObserver { exception ->
            binding.refreshLayout.isHeaderRefreshing = false
            exception?.apply {
                when (this) {
                    is EmptyListException -> makeSnakeBar(getString(R.string.library_empty_hint))
                    else -> makeSnakeBar(message ?: getString(R.string.library_unknown_error_hint))
                }
            }
        })

        viewModel.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver { exception ->
            binding.refreshLayout.isFooterRefreshing = false
            exception?.apply {
                when (this) {
                    is EmptyListException -> makeSnakeBar(getString(R.string.library_reach_end_hint))
                    else -> makeSnakeBar(message ?: getString(R.string.library_unknown_error_hint))
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_library, menu)
        val searchView: SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.library_search_hint)
        if (viewModel.filter != "") searchView.setQuery(viewModel.filter, false)
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

    private fun makeSnakeBar(content: String) {
        view?.let { Snackbar.make(it, content, Snackbar.LENGTH_SHORT).show() }
    }
}