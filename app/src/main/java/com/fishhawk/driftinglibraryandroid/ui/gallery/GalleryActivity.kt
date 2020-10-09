package com.fishhawk.driftinglibraryandroid.ui.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fishhawk.driftinglibraryandroid.MainApplication
import com.fishhawk.driftinglibraryandroid.databinding.GalleryActivityBinding
import com.fishhawk.driftinglibraryandroid.repository.EventObserver
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.extension.*
import com.fishhawk.driftinglibraryandroid.util.FileUtil
import kotlinx.android.synthetic.main.gallery_activity.view.*
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class GalleryActivity : AppCompatActivity() {
    private val viewModel: GalleryViewModel by viewModels {
        val application = applicationContext as MainApplication
        GalleryViewModelFactory(application)
    }
    private lateinit var binding: GalleryActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeWithTranslucentStatus()
        super.onCreate(savedInstanceState)
        setupFullScreen()

        binding = GalleryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val arguments = intent.extras!!
        val id: String = arguments.getString("id")!!
        val title: String = arguments.getString("title")!!
        val providerId: String? = arguments.getString("providerId")
        val thumb: String = arguments.getString("thumb")!!

        if (providerId == null) viewModel.openMangaFromLibrary(id)
        else viewModel.openMangaFromProvider(providerId, id)

        binding.info = GalleryInfo(providerId, title)
        setupThumb(thumb)
        setupActionButton()

        binding.thumbCard.setOnLongClickListener {
            GalleryThumbSheet(
                this,
                { saveThumb() },
                {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 1000)
                }
            ).show()
            true
        }

        val tagAdapter = TagGroupListAdapter(this)
        tagAdapter.onTagClicked = { key, value ->
            val keywords = if (key.isBlank()) value else "${key}:$value"
            navToMainActivity(keywords)
        }
        binding.tags.adapter = tagAdapter

        val contentAdapter = ContentAdapter(this, id, providerId)
        binding.chapters.adapter = contentAdapter

        binding.displayModeButton.setOnClickListener {
            SettingsHelper.chapterDisplayMode.setNextValue()
        }
        binding.displayOrderButton.setOnClickListener {
            SettingsHelper.chapterDisplayOrder.setNextValue()
        }

        SettingsHelper.chapterDisplayMode.observe(this, Observer {
            binding.displayModeButton.setIconResource(getChapterDisplayModeIcon())
            binding.chapters.viewMode = when (it) {
                SettingsHelper.ChapterDisplayMode.GRID -> ContentView.ViewMode.GRID
                SettingsHelper.ChapterDisplayMode.LINEAR -> ContentView.ViewMode.LINEAR
            }
        })

        SettingsHelper.chapterDisplayOrder.observe(this, Observer {
            binding.chapters.viewOrder = when (it) {
                SettingsHelper.ChapterDisplayOrder.ASCEND -> ContentView.ViewOrder.ASCEND
                SettingsHelper.ChapterDisplayOrder.DESCEND -> ContentView.ViewOrder.DESCEND
            }
        })

        viewModel.notification.observe(this, EventObserver {
            binding.root.makeToast(getNotificationMessage(it))
        })

        viewModel.detail.observe(this, Observer { result ->
            when (result) {
                is Result.Success -> {
                    binding.multipleStatusView.showContent()

                    val detail = result.data
                    binding.info = GalleryInfo(detail)
                    binding.description.setOnClickListener {
                        binding.description.maxLines =
                            if (binding.description.maxLines < Int.MAX_VALUE) Int.MAX_VALUE else 3
                    }
                    if (!detail.metadata.tags.isNullOrEmpty())
                        tagAdapter.setList(detail.metadata.tags)
                    if (detail.collections.isNotEmpty())
                        binding.chapters.collections = detail.collections
                }
                is Result.Error -> binding.multipleStatusView.showError(result.exception.message)
                is Result.Loading -> binding.multipleStatusView.showLoading()
            }
        })

        viewModel.history.observe(this, Observer { history ->
            binding.contentView.chapters.markedPosition = history?.let {
                MarkedPosition(it.collectionIndex, it.chapterIndex, it.pageIndex)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            binding.thumb.setImageURI(data?.data)
            val uri = data?.data
            val content = uri?.let { contentResolver.openInputStream(it)?.readBytes() }
            val type = uri?.let { contentResolver.getType(uri)?.toMediaTypeOrNull() }
            print(content?.size)
            println(type)
            if (content != null && type != null)
                viewModel.updateThumb(content.toRequestBody(type))
        }
    }

    private fun setupThumb(thumb: String) {
        Glide.with(this)
            .load(thumb)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.thumb)

        Glide.with(this)
            .load(thumb)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(binding.backdrop)
    }

    private fun saveThumb() {
        val detail = (viewModel.detail.value as? Result.Success)?.data
        if (detail == null) {
            binding.root.makeToast("Manga not loaded")
        } else {
            val url = detail.thumb
            if (url == null) {
                binding.root.makeToast("No cover")
            } else {
                val filename = "${detail.id}-thumb"
                val activity = this
                lifecycleScope.launch {
                    try {
                        FileUtil.saveImage(activity, url, filename)
                        binding.root.makeToast("Image saved")
                    } catch (e: Throwable) {
                        binding.root.makeToast(e.message ?: "Unknown error")
                    }
                }
            }
        }
    }

    private fun setupActionButton() {
        binding.readButton.setOnClickListener {
            (viewModel.detail.value as? Result.Success)?.let { result ->
                val detail = result.data
                viewModel.history.value?.let { history ->
                    navToReaderActivity(
                        detail.id,
                        detail.providerId,
                        history.collectionIndex,
                        history.chapterIndex,
                        history.pageIndex
                    )
                } ?: navToReaderActivity(detail.id, detail.providerId)
            }
        }
        binding.subscribeButton.setOnClickListener { viewModel.subscribe() }
        binding.downloadButton.setOnClickListener { viewModel.download() }
    }
}
