package com.fishhawk.driftinglibraryandroid.gallery

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryViewModel(private val id: String) : ViewModel() {
    private val _mangaDetail: MutableLiveData<Result<MangaDetail>> = MutableLiveData(Result.Loading)
    val mangaDetail: LiveData<Result<MangaDetail>> = _mangaDetail

    init {
        GlobalScope.launch(Dispatchers.Main) {
            _mangaDetail.value = Repository.getMangaDetail(id)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class GalleryViewModelFactory(private val id: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(GalleryViewModel::class.java) ->
                GalleryViewModel(id)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}