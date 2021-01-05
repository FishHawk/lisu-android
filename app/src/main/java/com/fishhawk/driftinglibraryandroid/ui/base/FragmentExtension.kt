package com.fishhawk.driftinglibraryandroid.ui.base

import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.ui.activity.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.activity.ReaderActivity
import com.fishhawk.driftinglibraryandroid.widget.MultiStateView
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

fun <T> Fragment.bindToListViewModel(
    multiStateView: MultiStateView,
    refreshLayout: RefreshLayout,
    viewModel: RefreshableListViewModel<T>,
    adapter: BaseAdapter<T>
) {
    bindToFeedbackViewModel(viewModel)

    viewModel.list.observe(viewLifecycleOwner) { result ->
        if (result is Result.Success) adapter.setList(result.data)
        multiStateView.viewState = when (result) {
            is Result.Success -> {
                if (result.data.isEmpty()) ViewState.Empty
                else ViewState.Content
            }
            is Result.Error -> ViewState.Error(result.exception)
            null -> ViewState.Loading
        }
    }

    viewModel.refreshFinish.observe(viewLifecycleOwner, EventObserver {
        refreshLayout.isHeaderRefreshing = false
    })

    if (viewModel is RefreshableListViewModelWithFetchMore) {
        viewModel.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver {
            refreshLayout.isFooterRefreshing = false
        })
    }

    with(refreshLayout) {
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
        val listener = when (viewModel) {
            is RefreshableListViewModelWithFetchMore<*> -> {
                object : RefreshLayout.OnRefreshListener {
                    override fun onHeaderRefresh() = viewModel.refresh()
                    override fun onFooterRefresh() = viewModel.fetchMore()
                }
            }
            else -> {
                object : RefreshLayout.OnRefreshListener {
                    override fun onHeaderRefresh() = viewModel.refresh()
                    override fun onFooterRefresh() {
                        isFooterRefreshing = false
                    }
                }
            }
        }
        setOnRefreshListener(listener)
    }
}

fun RecyclerView.changeMangaListDisplayMode(adapter: MangaListAdapter) {
    val displayMode = GlobalPreference.displayMode.get()
    if (displayMode == GlobalPreference.DisplayMode.GRID &&
        (adapter.viewMode != MangaListAdapter.ViewMode.GRID || layoutManager == null)
    ) {
        adapter.viewMode = MangaListAdapter.ViewMode.GRID
        layoutManager = GridLayoutManager(context, 3)
        this.adapter = adapter
    } else if (displayMode == GlobalPreference.DisplayMode.LINEAR &&
        (adapter.viewMode != MangaListAdapter.ViewMode.LINEAR || layoutManager == null)
    ) {
        adapter.viewMode = MangaListAdapter.ViewMode.LINEAR
        layoutManager = LinearLayoutManager(context)
        this.adapter = adapter
    }
}

fun getDisplayModeIcon(): Int {
    return when (GlobalPreference.displayMode.get()) {
        GlobalPreference.DisplayMode.GRID -> R.drawable.ic_baseline_view_module_24
        GlobalPreference.DisplayMode.LINEAR -> R.drawable.ic_baseline_view_list_24
    }
}

fun getChapterDisplayModeIcon(): Int {
    return when (GlobalPreference.chapterDisplayMode.get()) {
        GlobalPreference.ChapterDisplayMode.GRID -> R.drawable.ic_baseline_view_module_24
        GlobalPreference.ChapterDisplayMode.LINEAR -> R.drawable.ic_baseline_view_list_24
    }
}

fun Fragment.navToReaderActivity(
    id: String,
    providerId: String?,
    collectionIndex: Int = 0,
    chapterIndex: Int = 0,
    pageIndex: Int = 0
) {
    val bundle = bundleOf(
        "id" to id,
        "providerId" to providerId,
        "collectionIndex" to collectionIndex,
        "chapterIndex" to chapterIndex,
        "pageIndex" to pageIndex
    )

    val intent = Intent(requireActivity(), ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun Fragment.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.ensurePermission(permission: String): Boolean {
    return checkPermission(permission).also { isGrant ->
        if (!isGrant) (requireActivity() as BaseActivity).requestPermission(permission)
    }
}

fun Fragment.saveImage(url: String, filename: String) {
    if (!ensurePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) return
    lifecycleScope.launch(Dispatchers.Main) {
        try {
            val resolver: ContentResolver = requireContext().contentResolver
            val uri = resolver.run {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_PICTURES}/DriftingLibrary/"
                        )
                    } else {
                        put(
                            MediaStore.MediaColumns.DATA,
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                .toString() + "/DriftingLibrary/${filename}.png"
                        )
                    }
                }

                insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )
            } ?: return@launch makeToast(R.string.toast_image_already_exist)

            withContext(Dispatchers.IO) {
                val outputStream = resolver.openOutputStream(uri)!!
                val bitmap = Glide.with(requireContext()).asBitmap().load(url).submit().get()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            }

            makeToast(R.string.toast_image_saved)
        } catch (e: Throwable) {
            makeToast(e)
        }
    }
}

fun Fragment.shareImage(url: String, filename: String) {
    lifecycleScope.launch(Dispatchers.IO) {
        try {
            val srcFile = Glide.with(requireContext()).asFile().load(url).submit().get()

            val dir = File(requireContext().cacheDir, "shared_image")
            dir.mkdirs()
            val destFile = File(dir, "$filename.${srcFile.extension}")
            srcFile.copyTo(destFile, overwrite = true)

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireActivity().packageName}.fileprovider",
                destFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
            }
            startActivity(Intent.createChooser(shareIntent, "Share image via"))
        } catch (e: Throwable) {
            makeToast(e)
        }
    }
}

val Fragment.clipboardManager
    get() = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

val Fragment.inputMethodManager
    get() = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager


fun Fragment.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText("simple text", text)
    clipboardManager.setPrimaryClip(clip)
}

fun Fragment.closeInputMethod() {
    inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
}

