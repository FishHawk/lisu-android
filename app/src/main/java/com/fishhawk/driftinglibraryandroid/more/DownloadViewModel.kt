package com.fishhawk.driftinglibraryandroid.more

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.library.EmptyListException
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Order
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    private val _downloadTasks: MutableLiveData<Result<List<Order>>> =
        MutableLiveData(Result.Loading)
    val downloadTasks: LiveData<Result<List<Order>>> = _downloadTasks

    private val _refreshFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Throwable?>> = _refreshFinish

    fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getOrders()
            _downloadTasks.value = result
            when (result) {
                is Result.Success -> {
                    if (result.data.isEmpty()) _refreshFinish.value = Event(EmptyListException())
                    else _refreshFinish.value = Event(null)
                }
                is Result.Error -> _refreshFinish.value = Event(result.exception)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DownloadViewModelFactory(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(DownloadViewModel::class.java) ->
                DownloadViewModel(remoteLibraryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
