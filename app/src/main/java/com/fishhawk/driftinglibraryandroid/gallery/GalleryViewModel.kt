package com.fishhawk.driftinglibraryandroid.gallery

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val _mangaDetail: MutableLiveData<Result<MangaDetail>> = MutableLiveData(Result.Loading)
    val mangaDetail: LiveData<Result<MangaDetail>> = _mangaDetail

    fun openManga(id: String) {
        _mangaDetail.value = Result.Loading
        GlobalScope.launch(Dispatchers.Main) {
            _mangaDetail.value = Repository.getMangaDetail(id)
        }
    }
}