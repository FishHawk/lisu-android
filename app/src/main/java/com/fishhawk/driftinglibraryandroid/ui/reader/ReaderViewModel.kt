package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.setting.PreferenceStringLiveData
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.launch
import java.util.*

class ReaderViewModel(
    private val id: String,
    private val source: String?,
    private val collectionIndex: Int,
    private var chapterIndex: Int,
    pageIndex: Int,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    // reading direction
    val readingDirection: PreferenceStringLiveData = SettingsHelper.readingDirection
    val isReaderDirectionEqualLeftToRight: LiveData<Boolean> =
        Transformations.map(readingDirection) { it == SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT }
    val isReaderDirectionEqualRightToLeft: LiveData<Boolean> =
        Transformations.map(readingDirection) { it == SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT }
    val isReaderDirectionEqualVertical: LiveData<Boolean> =
        Transformations.map(readingDirection) { it == SettingsHelper.READING_DIRECTION_VERTICAL }

    // menu
    val isMenuVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    // reader content
    private val mangaDetail: MutableLiveData<Result<MangaDetail>> = MutableLiveData()
    val readerContent: MutableLiveData<Result<List<String>>> = MutableLiveData(Result.Loading)

    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val chapterPosition: MutableLiveData<Int> = MutableLiveData(0)
    val chapterSize: MutableLiveData<Int> = MutableLiveData(0)
    val chapterTitle: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch {
            isLoading.value = true
            val detail =
                if (source == null) remoteLibraryRepository.getMangaFromLibrary(id)
                else remoteLibraryRepository.getMangaFromSource(id, source)
            mangaDetail.value = detail
            when (detail) {
                is Result.Success -> openChapter(chapterIndex, pageIndex)
                is Result.Error -> readerContent.value = Result.Error(detail.exception)
            }
        }
    }

    private fun openChapter(chapterIndex: Int, startPage: Int = 0) {
        isLoading.value = true

        val detail = (mangaDetail.value as? Result.Success)?.data ?: return
        val collection = detail.collections[collectionIndex]

        val self = this
        val chapterId = collection.chapters[chapterIndex].id
        val chapterTitle = collection.chapters[chapterIndex].title

        viewModelScope.launch {
            val result =
                if (source == null)
                    remoteLibraryRepository.getChapterContentFromLibrary(
                        id, collection.title, chapterTitle
                    )
                else
                    remoteLibraryRepository.getChapterContentFromSource(source, chapterId)
            when (result) {
                is Result.Success -> {
                    self.chapterIndex = chapterIndex
                    self.chapterTitle.value = chapterTitle
                    self.chapterSize.value = result.data.size
                    self.chapterPosition.value = when {
                        startPage < 0 || startPage >= result.data.size -> result.data.size - 1
                        else -> startPage
                    }
                }
            }
            self.readerContent.value = result
            isLoading.value = false
        }
    }

    fun openNextChapter(): Boolean {
        val detail = (mangaDetail.value as? Result.Success)?.data
        if (detail != null) {
            val collection = detail.collections[collectionIndex]
            if (chapterIndex < collection.chapters.size - 1) {
                openChapter(chapterIndex + 1)
                return true
            }
        }
        return false
    }

    fun openPrevChapter(): Boolean {
        val detail = (mangaDetail.value as? Result.Success)?.data
        if (detail != null) {
            if (chapterIndex > 0) {
                openChapter(chapterIndex - 1, -1)
                return true
            }
        }
        return false
    }

    suspend fun updateReadingHistory() {
        val detail = (mangaDetail.value as? Result.Success)?.data

        detail?.let {
            val collection = detail.collections[collectionIndex]
            val readingHistory = ReadingHistory(
                it.id,
                it.title,
                it.thumb,
                it.source,
                Calendar.getInstance().time.time,
                collectionIndex,
                collection.title,
                chapterIndex,
                collection.chapters[chapterIndex].name,
                chapterPosition.value ?: 0
            )
            readingHistoryRepository.updateReadingHistory(readingHistory)
        }
    }
}


@Suppress("UNCHECKED_CAST")
class ReaderViewModelFactory(
    private val id: String,
    private val source: String?,
    private val collectionIndex: Int,
    private val chapterIndex: Int,
    private val pageIndex: Int,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(ReaderViewModel::class.java) ->
                ReaderViewModel(
                    id,
                    source,
                    collectionIndex,
                    chapterIndex,
                    pageIndex,
                    remoteLibraryRepository,
                    readingHistoryRepository
                )
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}