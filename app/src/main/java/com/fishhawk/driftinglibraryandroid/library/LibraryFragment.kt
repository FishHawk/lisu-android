package com.fishhawk.driftinglibraryandroid.library

import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.hippo.refreshlayout.RefreshLayout

class LibraryFragment : Fragment() {
    private val ARG_COLUMN_COUNT = "column-count"
    private var mColumnCount = 3
    private lateinit var viewModel: LibraryViewModel
    private var filter: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mColumnCount = arguments!!.getInt(ARG_COLUMN_COUNT)
        }
        viewModel = activity?.run { ViewModelProvider(this)[LibraryViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        val refreshLayout = view.findViewById<RefreshLayout>(R.id.refresh_layout)
        val recyclerView = refreshLayout.findViewById<RecyclerView>(R.id.list)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)

        val menuButton = view.findViewById<ImageView>(R.id.menu_button)
        val clearButton = view.findViewById<ImageView>(R.id.clear_button)
        val searchBar = view.findViewById<EditText>(R.id.search_bar)

        menuButton.apply {
            setOnClickListener {
                val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
                drawerLayout?.openDrawer(GravityCompat.START)
            }
        }

        clearButton.apply {
            setOnClickListener {
                filter = ""
                searchBar.setText("")
            }
        }

        searchBar.apply {
            setOnEditorActionListener { _, _, _ ->
                filter = searchBar.text.toString()
                viewModel.refresh(filter)

                searchBar.clearFocus()
                (activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                true
            }
        }

        fab.apply {
            setOnClickListener {
                val input = EditText(context)
                input.setText(Repository.getUrl())

                AlertDialog.Builder(context!!, R.style.AlertDialogTheme)
                    .setTitle("Set Library Address")
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        val url = input.text.toString()
                        if (URLUtil.isNetworkUrl(url)) {
                            Repository.setRemoteLibraryService(url)
                            filter = ""
                            searchBar.setText("")
                            viewModel.refresh()
                        } else {
                            Snackbar.make(
                                view, "Illegal url", Snackbar.LENGTH_LONG
                            ).setAction("Action", null).show()
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel();
                    }
                    .show()
            }
        }

        refreshLayout.apply {
            setOnRefreshListener(object : RefreshLayout.OnRefreshListener {
                override fun onHeaderRefresh() {
                    viewModel.refresh(filter)
                }

                override fun onFooterRefresh() {
                    viewModel.fetchMore(filter)
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
            Observer<List<MangaSummary>> { data ->
                refreshLayout.isHeaderRefreshing = false
                refreshLayout.isFooterRefreshing = false
                recyclerView.adapter?.let { (it as MangaListAdapter).update(data) }
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