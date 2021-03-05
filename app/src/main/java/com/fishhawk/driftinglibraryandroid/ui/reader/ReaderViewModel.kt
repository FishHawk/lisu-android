package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.Chapter
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.ui.base.Event
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import kotlinx.coroutines.launch
import java.util.*

class ReaderChapter(val index: Int, chapter: Chapter) {
    val id = chapter.id
    val title = chapter.title
    val name = chapter.name
    var state: ViewState = ViewState.Loading
    var images: List<String> = listOf()
}

class ReaderChapterPointer(
    private val list: List<ReaderChapter>,
    var startPage: Int,
    var index: Int
) {
    val currChapter get() = list[index]
    val nextChapter get() = list.getOrNull(index + 1)
    val prevChapter get() = list.getOrNull(index - 1)
}

class ReaderViewModel(
    private val id: String,
    private val providerId: String?,
    private val collectionIndex: Int,
    private val chapterIndex: Int,
    private val pageIndex: Int,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : FeedbackViewModel() {

    private val _readerState: MutableLiveData<ViewState> = MutableLiveData(ViewState.Loading)
    val readerState: LiveData<ViewState> = _readerState

    private lateinit var mangaDetail: MangaDetail
    private val _mangaTitle = MutableLiveData(id)
    val mangaTitle: LiveData<String> = _mangaTitle

    private val _isOnlyOneChapter: MutableLiveData<Boolean> = MutableLiveData()
    val isOnlyOneChapter: LiveData<Boolean> = _isOnlyOneChapter

    private lateinit var collectionId: String
    private lateinit var chapters: List<ReaderChapter>

    val chapterPointer: MutableLiveData<ReaderChapterPointer?> = MutableLiveData(null)
    val prevChapterStateChanged: MutableLiveData<Event<ViewState>> = MutableLiveData()
    val nextChapterStateChanged: MutableLiveData<Event<ViewState>> = MutableLiveData()

    val chapterSize = chapterPointer.map { it?.currChapter?.images?.size ?: 0 }
    val chapterName = chapterPointer.map { it?.currChapter?.name ?: "" }
    val chapterTitle = chapterPointer.map { it?.currChapter?.title ?: "" }
    val chapterPosition: MutableLiveData<Int> = MutableLiveData(0)

    init {
        initReader()
    }

    fun initReader() {
        _readerState.value = ViewState.Loading

        viewModelScope.launch {
            // load manga
            val result =
                if (providerId == null) remoteLibraryRepository.getManga(id)
                else remoteProviderRepository.getManga(providerId, id)

            result.onSuccess {
                mangaDetail = it
                _mangaTitle.value = it.title
            }.onFailure {
                _readerState.value = ViewState.Error(it)
            }

            // load chapter
            result.onSuccess {
                try {
                    val collection = it.collections[collectionIndex]
                    collectionId = collection.id
                    chapters = collection.chapters.mapIndexed { index, chapter ->
                        ReaderChapter(index, chapter)
                    }
                    val chapter = chapters[chapterIndex]
                    _isOnlyOneChapter.value = chapters.size <= 1
                    openChapter(chapter, pageIndex)
                    _readerState.value = ViewState.Content

                } catch (e: Throwable) {
                    _readerState.value = ViewState.Error(e)
                }
            }
        }
    }

    fun openChapter(chapter: ReaderChapter, pageIndex: Int) {
        if (chapter.state !is ViewState.Content) chapter.state = ViewState.Loading

        val pointer = ReaderChapterPointer(chapters, pageIndex, chapter.index)
        chapterPointer.value = pointer

        viewModelScope.launch {
            if (chapter.state !is ViewState.Content) loadChapter(chapter)
            pointer.nextChapter?.let { if (it.state !is ViewState.Content) loadChapter(it) }
            pointer.prevChapter?.let { if (it.state !is ViewState.Content) loadChapter(it) }
        }
    }

    private suspend fun loadChapter(chapter: ReaderChapter) {
        fun refreshPointerIfNeed() {
            chapterPointer.value?.let { pointer ->
                when (chapter.index - pointer.index) {
                    -1 -> prevChapterStateChanged.value = Event(chapter.state)
                    1 -> nextChapterStateChanged.value = Event(chapter.state)
                    0 -> chapterPointer.value = pointer
                }
            }
        }

        if (chapter.state is ViewState.Content) return

        val providerId = mangaDetail.providerId
        val mangaId = mangaDetail.id
        val chapterId = chapter.id

        val result =
            if (providerId == null)
                remoteLibraryRepository.getChapterContent(mangaId, collectionId, chapterId)
            else remoteProviderRepository.getChapterContent(providerId, mangaId, chapterId)

        result.onSuccess {
            chapter.state = ViewState.Content
            chapter.images = it
            refreshPointerIfNeed()
        }.onFailure {
            if (chapter.state != ViewState.Content) {
                chapter.state = ViewState.Error(it)
                refreshPointerIfNeed()
            }
        }
    }

    fun openNextChapter() {
        val pointer = chapterPointer.value!!
        val nextChapter = pointer.nextChapter
            ?: return feed(R.string.toast_no_next_chapter)
        openChapter(nextChapter, 0)
    }

    fun openPrevChapter() {
        val pointer = chapterPointer.value!!
        val prevChapter = pointer.prevChapter
            ?: return feed(R.string.toast_no_prev_chapter)
        openChapter(prevChapter, 0)
    }

    fun moveToNextChapter() {
        val pointer = chapterPointer.value!!
        val nextChapter = pointer.nextChapter
            ?: return feed(R.string.toast_no_next_chapter)

        if (nextChapter.state is ViewState.Content) {
            openChapter(nextChapter, 0)
        } else {
            viewModelScope.launch { loadChapter(nextChapter) }
        }
    }

    fun moveToPrevChapter() {
        val pointer = chapterPointer.value!!
        val prevChapter = pointer.prevChapter
            ?: return feed(R.string.toast_no_prev_chapter)

        if (prevChapter.state is ViewState.Content) {
            openChapter(prevChapter, Int.MAX_VALUE)
        } else {
            viewModelScope.launch { loadChapter(prevChapter) }
        }
    }

    suspend fun updateReadingHistory() {
        chapterPointer.value?.let {
            val readingHistory =
                ReadingHistory(
                    mangaDetail.id,
                    GlobalPreference.selectedServer.get(),
                    mangaDetail.title,
                    mangaDetail.thumb ?: "",
                    mangaDetail.providerId,
                    Calendar.getInstance().time.time,
                    collectionId,
                    collectionIndex,
                    it.currChapter.name,
                    it.currChapter.index,
                    chapterPosition.value ?: 0
                )
            readingHistoryRepository.updateReadingHistory(readingHistory)
        }
    }

    fun makeImageFilenamePrefix(): String? {
        val pointer = chapterPointer.value ?: return null
        return "${mangaDetail.title}-$collectionId-${pointer.currChapter.title}"
    }
}


