package com.fishhawk.lisu.ui.reader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.ChapterDto
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import com.fishhawk.lisu.ui.widget.ViewState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ReaderChapter(
    val collectionId: String,
    val index: Int,
    chapter: ChapterDto
) {
    val id = chapter.id
    val title = chapter.title
    val name = chapter.name
    var state: ViewState = ViewState.Loading
    var images: List<String> = listOf()
}

sealed interface ReaderEffect : Event {
    data class Message(val redId: Int) : ReaderEffect
}

class ReaderViewModel(
    args: Bundle,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
) : BaseViewModel<ReaderEffect>() {

    private val mangaId =
        args.getParcelable<MangaDetailDto>("detail")?.id
            ?: args.getString("mangaId")!!

    private val providerId =
        args.getParcelable<MangaDetailDto>("detail")?.providerId
            ?: args.getString("providerId")!!

    private val mangaDetail = MutableStateFlow(
        args.getParcelable<MangaDetailDto>("detail")?.let { Result.success(it) }
    )

    val mangaLoadState = mangaDetail
        .map { detail ->
            detail?.fold({ ViewState.Loaded }, { ViewState.Failure(it) }) ?: ViewState.Loading
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ViewState.Loading)

    val mangaTitle = mangaDetail
        .map { it?.getOrNull() }
        .filterNotNull()
        .map { it.title ?: it.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mangaId)

    private val chapterList = mangaDetail
        .map { it?.getOrNull() }
        .filterNotNull()
        .map { detail ->
            val collectionId = args.getString("collectionId")!!
            val chapterId = args.getString("chapterId")!!
            when {
                collectionId.isNotBlank() -> {
                    detail.collections[collectionId]!!.mapIndexed { index, chapter ->
                        ReaderChapter(collectionId, index, chapter)
                    }
                }
                chapterId.isNotBlank() -> {
                    detail.chapters.mapIndexed { index, chapter ->
                        ReaderChapter(collectionId, index, chapter)
                    }
                }
                else -> {
                    listOf(
                        ReaderChapter(
                            collectionId,
                            0,
                            ChapterDto(id = " ", name = "", title = "")
                        )
                    )
                }
            }
        }
        .onEach { chapters ->
            val chapterId = args.getString("chapterId")
            val chapterIndex = chapters.indexOfFirst { it.id == chapterId }
            chapterPointer = MutableStateFlow(
                ReaderChapterPointer(
                    index = if (chapterIndex < 0) 0 else chapterIndex,
                    startPage = args.getInt("page", 0)
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    inner class ReaderChapterPointer(
        val index: Int,
        val startPage: Int
    ) {
        val currChapter get() = chapterList.value!![index]
        val nextChapter get() = chapterList.value!!.getOrNull(index + 1)
        val prevChapter get() = chapterList.value!!.getOrNull(index - 1)
    }

    lateinit var chapterPointer: MutableStateFlow<ReaderChapterPointer>

    init {
        refreshReader()
        chapterList
            .filterNotNull()
            .onEach { loadChapterPointer() }
            .launchIn(viewModelScope)
    }

    private fun loadChapterPointer() = viewModelScope.launch {
        val pointer = chapterPointer.value
        loadChapter(pointer.currChapter)
        pointer.prevChapter?.let { loadChapter(it) }
        pointer.nextChapter?.let { loadChapter(it) }
    }

    private suspend fun loadChapter(chapter: ReaderChapter) {
        if (chapter.state == ViewState.Loaded) return
        chapter.state = ViewState.Loading

        val result = remoteProviderRepository.getContent(
            providerId,
            mangaId,
            chapter.collectionId,
            chapter.id
        )

        fun refreshPointerIfNeed() {
            chapterPointer.value.let { pointer ->
                if (chapter.index == pointer.index) {
                    chapterPointer.value = ReaderChapterPointer(pointer.index, pointer.startPage)
                }
            }
        }

        result.fold({
            chapter.state = ViewState.Loaded
            chapter.images = it
            refreshPointerIfNeed()
        }, {
            if (chapter.state != ViewState.Loaded) {
                chapter.state = ViewState.Failure(it)
                refreshPointerIfNeed()
            }
        })
    }

    val isMenuOpened = MutableStateFlow(false)

    val isOnlyOneChapter = chapterList
        .map { it?.size == 1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun refreshReader() = viewModelScope.launch {
        val result = remoteProviderRepository.getManga(providerId, mangaId)
        if (mangaLoadState.value != ViewState.Loaded) {
            mangaDetail.value = result
        }
    }

    private fun sendMessage(resId: Int) {
        viewModelScope.launch {
            sendEvent(ReaderEffect.Message(resId))
        }
    }

    fun openNextChapter() {
        val pointer = chapterPointer.value
        val nextChapter = pointer.nextChapter
            ?: return sendMessage(R.string.no_next_chapter)
        chapterPointer.value =
            ReaderChapterPointer(nextChapter.index, 0)
        loadChapterPointer()
    }

    fun openPrevChapter() {
        val pointer = chapterPointer.value
        val prevChapter = pointer.prevChapter
            ?: return sendMessage(R.string.no_prev_chapter)
        chapterPointer.value =
            ReaderChapterPointer(prevChapter.index, 0)
        loadChapterPointer()
    }

    fun moveToNextChapter() {
        val pointer = chapterPointer.value
        val nextChapter = pointer.nextChapter
            ?: return sendMessage(R.string.no_next_chapter)

        if (nextChapter.state is ViewState.Loaded) {
            chapterPointer.value =
                ReaderChapterPointer(nextChapter.index, 0)
            loadChapterPointer()
        } else {
            viewModelScope.launch { loadChapter(nextChapter) }
        }
    }

    fun moveToPrevChapter() {
        val pointer = chapterPointer.value
        val prevChapter = pointer.prevChapter
            ?: return sendMessage(R.string.no_prev_chapter)

        if (prevChapter.state is ViewState.Loaded) {
            chapterPointer.value =
                ReaderChapterPointer(prevChapter.index, Int.MAX_VALUE)
            loadChapterPointer()
        } else {
            viewModelScope.launch { loadChapter(prevChapter) }
        }
    }

    fun updateReadingHistory(page: Int) = viewModelScope.launch {
        val detail = mangaDetail.value!!.getOrNull()!!
        val chapter = chapterPointer.value.currChapter
        val readingHistory = ReadingHistory(
            providerId = detail.providerId,
            mangaId = mangaId,
            cover = detail.cover,
            title = detail.title,
            authors = detail.authors.joinToString(separator = ";"),
            collectionId = chapter.collectionId,
            chapterId = chapter.id,
            chapterName = chapter.name,
            page = page
        )
        readingHistoryRepository.update(readingHistory)
    }

    fun updateCover(drawable: Drawable) = viewModelScope.launch {
        val stream = ByteArrayOutputStream()
        drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        remoteLibraryRepository.updateMangaCover(
            providerId,
            mangaId,
            byteArray,
            "image/png",
        ).onSuccess { sendEvent(ReaderEffect.Message(R.string.cover_updated)) }
            .onFailure { sendEvent(ReaderEffect.Message(R.string.cover_update_failed)) }
    }
}