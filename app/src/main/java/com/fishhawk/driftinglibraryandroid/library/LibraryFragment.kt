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
import com.classic.common.MultipleStatusView
import com.fishhawk.driftinglibraryandroid.R
import com.google.android.material.snackbar.Snackbar
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {
    private val ARG_COLUMN_COUNT = "column-count"
    private var mColumnCount = 3
    private lateinit var viewModel: LibraryViewModel
    private var filter: String = ""
    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "library_address") {
                viewModel.setLibraryAddress(sharedPreferences.getString(key, null) ?: "")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (arguments != null) {
            mColumnCount = arguments!!.getInt(ARG_COLUMN_COUNT)
        }

        viewModel = activity?.run { ViewModelProvider(this)[LibraryViewModel::class.java] }
            ?: throw Exception("Invalid Activity")

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val libraryAddress = sharedPreferences.getString("library_address", "") ?: ""
        viewModel.setLibraryAddress(libraryAddress)
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_library, menu);
        val searchItem: MenuItem = menu.findItem(R.id.action_search);
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter = query ?: ""
                viewModel.load(filter)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        val multipleStatusView = view.findViewById<MultipleStatusView>(R.id.multiple_status_view)
        val refreshLayout = view.findViewById<RefreshLayout>(R.id.refresh_layout)
        val recyclerView = refreshLayout.findViewById<RecyclerView>(R.id.list)

        multipleStatusView.showLoading()

        refreshLayout.apply {
            setOnRefreshListener(object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() {
                    GlobalScope.launch(Dispatchers.Main) {
                        viewModel.refresh(filter)?.let {
                            Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
                        }
                        isHeaderRefreshing = false
                    }
                }

                override fun onFooterRefresh() {
                    GlobalScope.launch(Dispatchers.Main) {
                        viewModel.fetchMore(filter)?.let {
                            Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
                        }
                        isFooterRefreshing = false
                    }
                }
            })

            // set style
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

        recyclerView.apply {
            val context = recyclerView.context

            // set layout manager
            if (mColumnCount <= 1) {
                recyclerView.layoutManager = LinearLayoutManager(context)
            } else {
                recyclerView.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            addItemDecoration(GridSpacingItemDecoration(mColumnCount, 16, true))

            // set adapter
            adapter = MangaListAdapter(context, emptyList()) { item, imageView ->
                viewModel.openManga(item)
                val extras = FragmentNavigatorExtras(imageView to item.id)
                val bundle = bundleOf("id" to item.id)
                recyclerView.findNavController()
                    .navigate(R.id.action_library_to_gallery, bundle, null, extras)
            }

            // set transition
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        viewModel.mangaList.observe(viewLifecycleOwner,
            Observer { data ->
                refreshLayout.isHeaderRefreshing = false
                recyclerView.adapter?.let { (it as MangaListAdapter).update(data) }
                if (recyclerView.adapter?.itemCount == 0) {
                    multipleStatusView.showEmpty()
                } else {
                    multipleStatusView.showContent()
                }
            }
        )
        return view
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

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }
}