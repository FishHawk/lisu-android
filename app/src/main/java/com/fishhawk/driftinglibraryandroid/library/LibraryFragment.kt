package com.fishhawk.driftinglibraryandroid.library

import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.databinding.FragmentLibraryBinding
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.google.android.material.snackbar.Snackbar
import com.hippo.refreshlayout.RefreshLayout

class LibraryFragment : Fragment() {
    private lateinit var viewModel: LibraryViewModel
    private lateinit var binding: FragmentLibraryBinding

    private var mColumnCount = 3

    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "library_address") {
                viewModel.setLibraryAddress(sharedPreferences.getString(key, null) ?: "")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentLibraryBinding.inflate(layoutInflater)
        viewModel = activity?.run { ViewModelProvider(this)[LibraryViewModel::class.java] }
            ?: throw Exception("Invalid Activity")

        setHasOptionsMenu(true)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val libraryAddress = sharedPreferences.getString("library_address", "") ?: ""
        viewModel.setLibraryAddress(libraryAddress)
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

            addItemDecoration(GridSpacingItemDecoration(mColumnCount, 16, true))

            // set adapter
            adapter = MangaListAdapter(context, emptyList()) { item, imageView ->
                val extras = FragmentNavigatorExtras(imageView to item.id)
                val bundle = bundleOf(
                    "id" to item.id,
                    "title" to item.title,
                    "thumb" to item.thumb
                )
                findNavController().navigate(R.id.action_library_to_gallery, bundle, null, extras)
            }

            // set transition
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        viewModel.mangaList.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Success -> {
                    binding.list.adapter!!.let { (it as MangaListAdapter).update(result.data) }
                    if (binding.list.adapter!!.itemCount == 0) binding.multipleStatusView.showEmpty()
                    else binding.multipleStatusView.showContent()
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                is Result.Loading -> binding.multipleStatusView.showLoading()
            }
        })

        viewModel.refreshResult.observe(viewLifecycleOwner, Observer { result ->
            binding.refreshLayout.isHeaderRefreshing = false
            when (result) {
                is Result.Success -> if (result.data.isEmpty()) {
                    Snackbar.make(view, "空的", Snackbar.LENGTH_LONG).show()
                }
                is Result.Error -> result.exception.message?.let {
                    Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
                }
            }
        })

        viewModel.fetchMoreResult.observe(viewLifecycleOwner, Observer { result ->
            binding.refreshLayout.isFooterRefreshing = false
            when (result) {
                is Result.Success -> if (result.data.isEmpty()) {
                    Snackbar.make(view, "没有了", Snackbar.LENGTH_LONG).show()
                }
                is Result.Error -> result.exception.message?.let {
                    Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_library, menu)
        val searchView: SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filter = query ?: ""
                viewModel.reload()
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return true
            }
        })
    }

    inner class GridSpacingItemDecoration(
        private var spanCount: Int,
        private var spacing: Int,
        private var includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) outRect.top = spacing
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) outRect.top = spacing
            }
        }
    }
}