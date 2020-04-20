package com.fishhawk.driftinglibraryandroid.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary

class LibraryViewModel : ViewModel() {
    private val _mangaList: MutableLiveData<List<MangaSummary>> = MutableLiveData()
    val mangaList: LiveData<List<MangaSummary>> = _mangaList

    private val _selectedMangaSummary: MutableLiveData<MangaSummary> = MutableLiveData()
    val selectedMangaSummary: MutableLiveData<MangaSummary> = _selectedMangaSummary
    private val _selectedMangaDetail: MutableLiveData<MangaDetail> = MutableLiveData()
    val selectedMangaDetail: LiveData<MangaDetail> = _selectedMangaDetail

    private var collectionIndex: Int = 0
    private var chapterIndex: Int = 0
    private var selectedCollectionTitle: String = ""
    private var selectedChapterTitle: String = ""
    var fromStart: Boolean = true
    private val _selectedChapterContent: MutableLiveData<List<String>> = MutableLiveData()
    val selectedChapterContent: MutableLiveData<List<String>> = _selectedChapterContent

    fun getSelectedCollectionTitle() = selectedCollectionTitle
    fun getSelectedChapterTitle() = selectedChapterTitle

    fun refresh(filter: String = "") {
        Repository.getMangaList({ result ->
            when (result) {
                is Result.Success -> _mangaList.value = result.data
                is Result.Error -> println(result.exception.message)
            }
        }, "", filter)
    }

    fun fetchMore(filter: String = "") {
        val lastId = _mangaList.value?.last()?.id ?: ""
        Repository.getMangaList({ result ->
            when (result) {
                is Result.Success -> {
                    _mangaList.value = _mangaList.value?.plus(result.data) ?: result.data
                }
                is Result.Error -> println(result.exception.message)
            }
        }, lastId, filter)
    }

    init {
        refresh()
    }

    fun openManga(selectedManga: MangaSummary) {
        _selectedMangaSummary.value = selectedManga
        Repository.getMangaDetail({ result ->
            when (result) {
                is Result.Success -> _selectedMangaDetail.value = result.data
                is Result.Error -> println(result.exception.message)
            }
        }, selectedManga.id)
    }

    fun openChapter(collectionIndex: Int, chapterIndex: Int) {
        this.collectionIndex = collectionIndex
        this.chapterIndex = chapterIndex
        fromStart = true

        val detail = selectedMangaDetail.value!!
        val id = detail.id
        selectedCollectionTitle = detail.collections[collectionIndex].title
        selectedChapterTitle = detail.collections[collectionIndex].chapters[chapterIndex]

        Repository.getChapterContent({ result ->
            when (result) {
                is Result.Success -> _selectedChapterContent.value = result.data
                is Result.Error -> println(result.exception.message)
            }
        }, id, selectedCollectionTitle, selectedChapterTitle)
    }

    fun openNextChapter(): Boolean {
        val detail = selectedMangaDetail.value!!
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