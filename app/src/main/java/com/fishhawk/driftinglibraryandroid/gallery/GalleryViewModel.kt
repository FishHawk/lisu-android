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

class GalleryViewModel: ViewModel() {
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


    fun openManga(id: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Repository.getMangaDetail(id).let {
                when (it) {
                    is Result.Success -> _selectedMangaDetail.value = it.data
                    is Result.Error -> println(it.exception.message)
                }
            }
        }
    }

    fun openChapter(collectionIndex: Int, chapterIndex: Int) {
        this.collectionIndex = collectionIndex
        this.chapterIndex = chapterIndex
        fromStart = true
        _selectedChapterContent.value = emptyList()

        val detail = selectedMangaDetail.value!!
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