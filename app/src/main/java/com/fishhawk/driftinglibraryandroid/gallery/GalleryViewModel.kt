package com.fishhawk.driftinglibraryandroid.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
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
    private var selectedChapterTitle: String = ""
    var fromStart: Boolean = true
    private val _selectedChapterContent: MutableLiveData<List<String>> = MutableLiveData()
    val selectedChapterContent: MutableLiveData<List<String>> = _selectedChapterContent

    fun getSelectedCollectionTitle() = selectedCollectionTitle
    fun getSelectedChapterTitle() = selectedChapterTitle


    fun openChapter(collectionIndex: Int, chapterIndex: Int) {
        this.collectionIndex = collectionIndex
        this.chapterIndex = chapterIndex
        fromStart = true
        _selectedChapterContent.value = emptyList()

        val detail = (mangaDetail.value as Result.Success).data
        val id = detail.id
        selectedCollectionTitle = detail.collections[collectionIndex].title
        selectedChapterTitle = detail.collections[collectionIndex].chapters[chapterIndex]

        GlobalScope.launch(Dispatchers.Main) {
            Repository.getChapterContent(id, selectedCollectionTitle, selectedChapterTitle).let {
                when (it) {
                    is Result.Success -> _selectedChapterContent.value = it.data
                    is Result.Error -> println(it.exception.message)
                }
            }
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
}