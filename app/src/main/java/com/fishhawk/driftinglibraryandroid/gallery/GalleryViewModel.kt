package com.fishhawk.driftinglibraryandroid.gallery

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
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

    private var collectionIndex: Int = 0
    private var chapterIndex: Int = 0
    private var selectedCollectionTitle: String = ""
    var selectedChapterTitle: String = ""
    var fromStart: Boolean = true

    private val _openedChapterContent: MutableLiveData<Result<List<String>>> =
        MutableLiveData(Result.Loading)
    val openedChapterContent: MutableLiveData<Result<List<String>>> = _openedChapterContent

    fun openChapter(collectionIndex: Int, chapterIndex: Int) {
        this.collectionIndex = collectionIndex
        this.chapterIndex = chapterIndex
        fromStart = true

        val detail = (mangaDetail.value as Result.Success).data
        val id = detail.id
        selectedCollectionTitle = detail.collections[collectionIndex].title
        selectedChapterTitle = detail.collections[collectionIndex].chapters[chapterIndex]

        GlobalScope.launch(Dispatchers.Main) {
            _openedChapterContent.value =
                Repository.getChapterContent(id, selectedCollectionTitle, selectedChapterTitle)
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
            fromStart = false
            return true
        }
        return false
    }

    val chapterPosition: MutableLiveData<Int> = MutableLiveData(0)
    val chapterSize: LiveData<Int> = Transformations.map(openedChapterContent) {
        when (it) {
            is Result.Success -> it.data.size
            else -> 0
        }
    }

    val readingDirection: MutableLiveData<Int> =
        MutableLiveData(SettingsHelper.getReadingDirection())
    val layoutDirection: LiveData<Int> = Transformations.map(readingDirection) {
        when (it) {
            SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT -> View.LAYOUT_DIRECTION_RTL
            else -> View.LAYOUT_DIRECTION_LTR
        }
    }
    val isLayoutHorizontal: LiveData<Boolean> = Transformations.map(readingDirection) {
        when (it) {
            SettingsHelper.READING_DIRECTION_VERTICAL -> false
            else -> true
        }
    }

    val isMenuVisible: MutableLiveData<Boolean> = MutableLiveData(false)
}