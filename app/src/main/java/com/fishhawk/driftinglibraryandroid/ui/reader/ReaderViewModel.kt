package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.setting.PreferenceEnumLiveData
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
    private val readingDirection: PreferenceEnumLiveData<*> = SettingsHelper.readingDirection
    val isReaderDirectionEqualRightToLeft: LiveData<Boolean> =
        Transformations.map(readingDirection) { it == SettingsHelper.ReadingDirection.RTL }

    // menu
    val isMenuVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    // reader content
    private val mangaDetail: MutableLiveData<Result<MangaDetail>> = MutableLiveData()
    val readerContent: MutableLiveData<Result<List<String>>> = MutableLiveData(Result.Loading)

    val mangaTitle: String?
        get() = (mangaDetail.value as? Result.Success)?.data?.title

    var isLoading: Boolean = true
    val chapterPosition: MutableLiveData<Int> = MutableLiveData(0)
    val chapterSize: MutableLiveData<Int> = MutableLiveData(0)
    val chapterName: MutableLiveData<String> = MutableLiveData("")
    val chapterTitle: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch {
            val detail =
                if (source == null) remoteLibraryRepository.getMangaFromLibrary(id)
                else remoteLibraryRepository.getMangaFromSource(source, id)
            mangaDetail.value = detail
            when (detail) {
                is Result.Success -> openChapter(chapterIndex, pageIndex)
                is Result.Error -> readerContent.value = Result.Error(detail.exception)
            }
        }
    }

    private fun openChapter(chapterIndex: Int, startPage: Int = 0) {
        isLoading = true

        val detail = (mangaDetail.value as? Result.Success)?.data ?: return
        val collection = detail.collections[collectionIndex]

        val self = this
        val chapterId = collection.chapters[chapterIndex].id
        val chapterName = collection.chapters[chapterIndex].name
        val chapterTitle = collection.chapters[chapterIndex].title

        viewModelScope.launch {
            val result =
                if (source == null)
                    remoteLibraryRepository.getChapterContentFromLibrary(
                        id, collection.title, chapterId
                    )
                else
                    remoteLibraryRepository.getChapterContentFromSource(source, chapterId)
            when (result) {
                is Result.Success -> {
                    self.chapterIndex = chapterIndex
                    self.chapterName.value = chapterName
                    self.chapterTitle.value = chapterTitle
                    self.chapterSize.value = result.data.size
                    self.chapterPosition.value = when {
                        startPage < 0 || startPage >= result.data.size -> result.data.size - 1
                        else -> startPage
                    }
                }
            }
            self.readerContent.value = result
            isLoading = false
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
                SettingsHelper.selectedServer.getValueDirectly(),

                it.title,
                it.thumb,
                it.source,
                Calendar.getInstance().time.time,

                collection.title,
                collectionIndex,
                collection.chapters[chapterIndex].name,
                chapterIndex,
                chapterPosition.value ?: 0
            )
            readingHistoryRepository.updateReadingHistory(readingHistory)
        }
    }

    fun makeImageFilenamePrefix(): String? {
        val detail = (mangaDetail.value as? Result.Success)?.data ?: return null
        val collection = detail.collections[collectionIndex]

        val mangaTitle = detail.title
        val collectionTitle = collection.title
        val chapterTitle = collection.chapters[chapterIndex].title

        return "$mangaTitle-$collectionTitle-$chapterTitle"
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