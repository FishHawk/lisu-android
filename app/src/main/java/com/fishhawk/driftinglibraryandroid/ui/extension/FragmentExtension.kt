package com.fishhawk.driftinglibraryandroid.ui.extension

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.classic.common.MultipleStatusView
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.gallery.GalleryActivity
import com.fishhawk.driftinglibraryandroid.ui.main.MainActivity
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderActivity
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderActivity
import com.fishhawk.driftinglibraryandroid.util.FileUtil
import com.hippo.refreshlayout.RefreshLayout
import kotlinx.coroutines.launch
import java.io.File

fun <T> Fragment.bindToListViewModel(
    multipleStatusView: MultipleStatusView,
    refreshLayout: RefreshLayout,
    viewModel: RefreshableListViewModel<T>,
    adapter: BaseRecyclerViewAdapter<T, *>
) {
    setupFeedbackModule(viewModel)

    viewModel.list.observe(viewLifecycleOwner, Observer { result ->
        when (result) {
            is Result.Success -> {
                adapter.setList(result.data)
                if (result.data.isEmpty()) multipleStatusView.showEmpty()
                else multipleStatusView.showContent()
            }
            is Result.Error -> multipleStatusView.showError(result.exception.message)
            is Result.Loading -> multipleStatusView.showLoading()
        }
    })

    viewModel.refreshFinish.observe(viewLifecycleOwner,
        EventObserver {
            refreshLayout.isHeaderRefreshing = false
        })

    if (viewModel is RefreshableListViewModelWithFetchMore) {
        viewModel.fetchMoreFinish.observe(viewLifecycleOwner, EventObserver {
            refreshLayout.isFooterRefreshing = false
        })
    }

    refreshLayout.bindingToViewModel(viewModel)
}

fun RecyclerView.changeMangaListDisplayMode(adapter: MangaListAdapter) {
    val displayMode = SettingsHelper.displayMode.getValueDirectly()
    if (displayMode == SettingsHelper.DisplayMode.GRID &&
        (adapter.viewMode != MangaListAdapter.ViewMode.GRID || layoutManager == null)
    ) {
        adapter.viewMode = MangaListAdapter.ViewMode.GRID
        layoutManager = GridLayoutManager(context, 3)
        this.adapter = adapter
    } else if (displayMode == SettingsHelper.DisplayMode.LINEAR &&
        (adapter.viewMode != MangaListAdapter.ViewMode.LINEAR || layoutManager == null)
    ) {
        adapter.viewMode = MangaListAdapter.ViewMode.LINEAR
        layoutManager = LinearLayoutManager(context)
        this.adapter = adapter
    }
}

fun getDisplayModeIcon(): Int {
    return when (SettingsHelper.displayMode.getValueDirectly()) {
        SettingsHelper.DisplayMode.GRID -> R.drawable.ic_baseline_view_module_24
        SettingsHelper.DisplayMode.LINEAR -> R.drawable.ic_baseline_view_list_24
    }
}

fun getChapterDisplayModeIcon(): Int {
    return when (SettingsHelper.chapterDisplayMode.getValueDirectly()) {
        SettingsHelper.ChapterDisplayMode.GRID -> R.drawable.ic_baseline_view_module_24
        SettingsHelper.ChapterDisplayMode.LINEAR -> R.drawable.ic_baseline_view_list_24
    }
}


fun Fragment.navToGalleryActivity(
    outline: MangaOutline,
    providerId: String?
) {
    navToGalleryActivity(
        outline.id,
        (outline.metadata.title ?: outline.id),
        outline.thumb,
        providerId
    )
}

fun Fragment.navToGalleryActivity(
    id: String,
    title: String,
    thumb: String?,
    providerId: String?
) {
    val bundle = bundleOf(
        "id" to id,
        "title" to title,
        "thumb" to thumb,
        "providerId" to providerId
    )

    val intent = Intent(requireActivity(), GalleryActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
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

fun Fragment.navToProviderActivity(provider: ProviderInfo) {
    val bundle = bundleOf("providerId" to provider.id)
    val intent = Intent(requireActivity(), ProviderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun Fragment.navToMainActivity(
    filter: String
) {
    val bundle = bundleOf("filter" to filter)
    val intent = Intent(requireActivity(), MainActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}


fun Fragment.saveImageToGallery(url: String, filename: String) {
    val uri = FileUtil.createImageInGallery(requireContext(), filename)
        ?: return makeToast(R.string.toast_image_already_exist)

    lifecycleScope.launch {
        try {
            FileUtil.saveImage(requireContext(), url, uri)
            makeToast(R.string.toast_image_saved)
        } catch (e: Throwable) {
            makeToast(e)
        }
    }
}

fun Fragment.startImagePickActivity() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, 1000)
}

fun Fragment.startImageShareActivity(file: File) {
    val uri = FileProvider.getUriForFile(
        requireContext(), "${requireActivity().packageName}.fileprovider", file
    )

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    startActivity(Intent.createChooser(shareIntent, "Share image"))
}
