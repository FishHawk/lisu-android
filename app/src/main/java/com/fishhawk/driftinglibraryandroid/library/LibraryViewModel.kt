package com.fishhawk.driftinglibraryandroid.library

import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class LibraryViewModel : ViewModel() {
    private var address: String = "http://192.168.0.103:8080/api/"
    private var repository: Repository? = Repository(address)

    fun getLibraryAddress(): String = address
    fun setLibraryAddress(inputAddress: String) {
        var newAddress = inputAddress
        newAddress = if (URLUtil.isNetworkUrl(newAddress)) newAddress else "http://${inputAddress}"
        newAddress = if (newAddress.last() == '/') newAddress else "$newAddress/"

        val newRepository = try {
            Repository(newAddress)
        } catch (e: IllegalArgumentException) {
            null
        }

        address = newAddress
        repository = newRepository
        reload()
    }

    private val _mangaList: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val mangaList: LiveData<Result<List<MangaSummary>>> = _mangaList

    fun reload(filter: String = "") {
        _mangaList.value = Result.Loading
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = repository?.getMangaList("", filter)) {
                is Result.Success -> _mangaList.value = result
                is Result.Error -> println(result.exception.message)
            }
        }
    }

    suspend fun refresh(filter: String = ""): String? {
        return repository?.getMangaList("", filter).let {
            when (it) {
                is Result.Success -> {
                    _mangaList.value = it
                    null
                }
                is Result.Error -> it.exception.message
                else -> null
            }
        }
    }

    suspend fun fetchMore(filter: String = ""): String? {
        val lastId =
            (_mangaList.value as Result.Success).data.let { if (it.isEmpty()) "" else it.last().id }

        return repository?.getMangaList(lastId, filter).let {
            when (it) {
                is Result.Success -> {
                    if (it.data.isNotEmpty()) {
                        _mangaList.value = (_mangaList.value as Result.Success).also { old ->
                            old.data.plus(it.data)
                        }
                        null
                    } else {
                        "没有更多了"
                    }
                }
                is Result.Error -> it.exception.message
                else -> null
            }
        }
    }

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


    fun openManga(selectedManga: MangaSummary) {
        _selectedMangaSummary.value = selectedManga

        GlobalScope.launch(Dispatchers.Main) {
            repository?.getMangaDetail(selectedManga.id).let {
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
            repository?.getChapterContent(id, selectedCollectionTitle, selectedChapterTitle).let {
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