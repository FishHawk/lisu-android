package com.fishhawk.driftinglibraryandroid.reader

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.setting.PreferenceStringLiveData
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.launch
import java.util.*

class ReaderViewModel(
    private val detail: MangaDetail,
    private val collectionIndex: Int,
    private var chapterIndex: Int,
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {
    private val id = detail.id
    private val collection = detail.collections[collectionIndex]

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
    val readerContent: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var startPage: Int = 0

    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val chapterPosition: MutableLiveData<Int> = MutableLiveData(0)
    val chapterSize: MutableLiveData<Int> = MutableLiveData(0)
    val chapterTitle: MutableLiveData<String> = MutableLiveData("")

    init {
        openChapter(chapterIndex)
    }

    private fun openChapter(chapterIndex: Int, isFromStart: Boolean = true) {
        isLoading.value = true

        val self = this
        val chapterTitle = collection.chapters[chapterIndex]

        viewModelScope.launch {
            when (val result = Repository.getChapterContent(id, collection.title, chapterTitle)) {
                is Result.Success -> {
                    startPage = if (isFromStart) 0 else result.data.size - 1
                    readerContent.value = result.data
                    self.chapterIndex = chapterIndex
                    self.chapterTitle.value = chapterTitle
                }
            }
            isLoading.value = false
        }
    }

    fun openNextChapter(): Boolean {
        if (chapterIndex < collection.chapters.size - 1) {
            openChapter(chapterIndex + 1)
            return true
        }
        return false
    }

    fun openPrevChapter(): Boolean {
        if (chapterIndex > 0) {
            openChapter(chapterIndex - 1, isFromStart = false)
            return true
        }
        return false
    }

    suspend fun updateReadingHistory() {
        val readingHistory = ReadingHistory(
            detail.id,
            detail.title,
            detail.thumb,
            Calendar.getInstance().time.time,
            collectionIndex,
            chapterIndex,
            chapterPosition.value ?: 0
        )
        readingHistoryRepository.updateReadingHistory(readingHistory)
    }
}


@Suppress("UNCHECKED_CAST")
class ReaderViewModelFactory(
    private val detail: MangaDetail,
    private val collectionIndex: Int,
    private val chapterIndex: Int,
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(ReaderViewModel::class.java) ->
                ReaderViewModel(detail, collectionIndex, chapterIndex, readingHistoryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}