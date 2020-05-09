package com.fishhawk.driftinglibraryandroid.gallery

import android.view.View
import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val _mangaDetail: MutableLiveData<Result<MangaDetail>> = MutableLiveData()
    val mangaDetail: LiveData<Result<MangaDetail>> = _mangaDetail

    init {
        _mangaDetail.value = Result.Loading
    }

    fun openManga(id: String) {
        _mangaDetail.value = Result.Loading
        GlobalScope.launch(Dispatchers.Main) {
            _mangaDetail.value = Repository.getMangaDetail(id)
        }
    }


    // reader
    val readingDirection: MutableLiveData<Int> =
        MutableLiveData(SettingsHelper.getReadingDirection())
    val layoutDirection: LiveData<Int> = Transformations.map(readingDirection) {
        when (it) {
            SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT -> View.LAYOUT_DIRECTION_RTL
            else -> View.LAYOUT_DIRECTION_LTR
        }
    }

    val isMenuVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    // for horizontal reader
    val horizontalReaderContent: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    val isHorizontalReaderEnable: LiveData<Boolean> = Transformations.map(readingDirection) {
        it != SettingsHelper.READING_DIRECTION_VERTICAL
    }
    var startPage: Int = 0

    // for vertical reader
    val verticalReaderContent: MutableLiveData<List<String>> = MutableLiveData(emptyList())

    // info for hint
    val chapterPosition: MutableLiveData<Int> = MutableLiveData(0)
    val chapterSize: MutableLiveData<Int> = MutableLiveData(0)
    val chapterTitle: MutableLiveData<String> = MutableLiveData("")
    val collectionTitle: MutableLiveData<String> = MutableLiveData("")


    // loading chapter content
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    private var collectionIndex: Int = 0
    private var chapterIndex: Int = 0

    fun openChapter(collectionIndex: Int, chapterIndex: Int, isFromStart: Boolean = true) {
        this.collectionIndex = collectionIndex
        this.chapterIndex = chapterIndex

        val detail = (mangaDetail.value as Result.Success).data
        val id = detail.id
        val collection = detail.collections[collectionIndex].title
        val chapter = detail.collections[collectionIndex].chapters[chapterIndex]

        GlobalScope.launch(Dispatchers.Main) {
            isLoading.value = true
            when (val result = Repository.getChapterContent(id, collection, chapter)) {
                is Result.Success -> {
                    if (readingDirection.value == SettingsHelper.READING_DIRECTION_VERTICAL)
                        verticalReaderContent.value = result.data
                    else {
                        horizontalReaderContent.value = result.data
                        startPage = if (isFromStart) 0 else result.data.size - 1
                    }

                    collectionTitle.value = collection
                    chapterTitle.value = chapter
                }
            }
            isLoading.value = false
        }
    }

    fun openNextChapter(): Boolean {
        val detail = (mangaDetail.value as Result.Success).data
        if (chapterIndex < detail.collections[collectionIndex].chapters.size - 1) {
            openChapter(collectionIndex, chapterIndex + 1)
            return true
        }
        return false
    }

    fun openPrevChapter(): Boolean {
        if (chapterIndex > 0) {
            openChapter(collectionIndex, chapterIndex - 1)
            return true
        }
        return false
    }
}