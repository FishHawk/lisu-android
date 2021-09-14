package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.get
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.Chapter
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class ReaderChapter(
    val collectionIndex: Int,
    val collectionId: String,
    val index: Int,
    chapter: Chapter
) {
    val id = chapter.id
    val title = chapter.title
    val name = chapter.name
    var state: ViewState = ViewState.Loading
    var images: List<String> = listOf()
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val savedStateHandle: SavedStateHandle
) : FeedbackViewModel() {

    private val mangaId =
        savedStateHandle.get<MangaDetail>("detail")?.id
            ?: savedStateHandle.get<String>("id")!!

    private val providerId =
        savedStateHandle.get<MangaDetail>("detail")?.provider?.name
            ?: savedStateHandle.get<String>("providerId")

    private val mangaDetail = MutableStateFlow(
        savedStateHandle.get<MangaDetail>("detail")?.let { Result.success(it) }
    )

    val mangaLoadState = mangaDetail
        .map { detail ->
            detail?.fold({ ViewState.Loaded }, { ViewState.Failure(it) }) ?: ViewState.Loading
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ViewState.Loading)

    val mangaTitle = mangaDetail
        .map { it?.getOrNull() }
        .filterNotNull()
        .map { it.title }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mangaId)

    private val chapterList = mangaDetail
        .map { it?.getOrNull() }
        .filterNotNull()
        .map {
            val collectionIndex = savedStateHandle.get<Int>("collectionIndex") ?: 0
            val collection = it.collections[collectionIndex]
            collection.chapters.mapIndexed { index, chapter ->
                ReaderChapter(collectionIndex, collection.id, index, chapter)
            }
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

    val chapterPointer = MutableStateFlow(
        ReaderChapterPointer(
            index = savedStateHandle.get<Int>("chapterIndex") ?: 0,
            startPage = savedStateHandle.get<Int>("pageIndex") ?: 0
        )
    )

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

        val result =
            if (providerId != null)
                remoteProviderRepository.getChapterContent(providerId, mangaId, chapter.id)
            else
                remoteLibraryRepository.getChapterContent(mangaId, chapter.collectionId, chapter.id)

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
        val result =
            if (providerId == null) remoteLibraryRepository.getManga(mangaId)
            else remoteProviderRepository.getManga(providerId, mangaId)
        if (mangaLoadState.value != ViewState.Loaded) {
            mangaDetail.value = result
        }
    }

    fun openNextChapter() {
        val pointer = chapterPointer.value
        val nextChapter = pointer.nextChapter
            ?: return feed(R.string.toast_no_next_chapter)
        chapterPointer.value =
            ReaderChapterPointer(nextChapter.index, 0)
        loadChapterPointer()
    }

    fun openPrevChapter() {
        val pointer = chapterPointer.value
        val prevChapter = pointer.prevChapter
            ?: return feed(R.string.toast_no_prev_chapter)
        chapterPointer.value =
            ReaderChapterPointer(prevChapter.index, 0)
        loadChapterPointer()
    }

    fun moveToNextChapter() {
        val pointer = chapterPointer.value
        val nextChapter = pointer.nextChapter
            ?: return feed(R.string.toast_no_next_chapter)

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
            ?: return feed(R.string.toast_no_prev_chapter)

        if (prevChapter.state is ViewState.Loaded) {
            chapterPointer.value =
                ReaderChapterPointer(prevChapter.index, Int.MAX_VALUE)
            loadChapterPointer()
        } else {
            viewModelScope.launch { loadChapter(prevChapter) }
        }
    }

    suspend fun updateReadingHistory(page: Int) {
        chapterPointer.value.currChapter.let {
            val readingHistory =
                ReadingHistory(
                    mangaId,
                    PR.selectedServer.get(),
                    mangaDetail.value!!.getOrNull()!!.title,
                    mangaDetail.value!!.getOrNull()!!.cover ?: "",
                    providerId,
                    Calendar.getInstance().time.time,
                    it.collectionId,
                    it.collectionIndex,
                    it.name,
                    it.index,
                    page
                )
            readingHistoryRepository.update(readingHistory)
        }
    }

//    fun makeImageFilenamePrefix(): String? {
//        val pointer = chapterPointer.value
//        return "${mangaDetail.title}-$collectionId-${pointer.currChapter.title}"
//    }
}


