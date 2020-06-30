package com.fishhawk.driftinglibraryandroid.more

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.library.EmptyListException
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.*
import okhttp3.internal.wait

class SubscriptionViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    private val _subscriptions: MutableLiveData<Result<MutableList<Subscription>>> =
        MutableLiveData(Result.Loading)
    val subscriptions: LiveData<Result<MutableList<Subscription>>> = _subscriptions

    private val _refreshFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Throwable?>> = _refreshFinish

    fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = remoteLibraryRepository.getSubscriptions()) {
                is Result.Success -> {
                    if (result.data.isEmpty()) _refreshFinish.value = Event(EmptyListException())
                    else _refreshFinish.value = Event(null)
                    _subscriptions.value = Result.Success(result.data.toMutableList())
                }
                is Result.Error -> _refreshFinish.value = Event(result.exception)
            }
        }
    }

    fun enable(position: Int) {
        viewModelScope.launch {
            val id = (subscriptions.value as Result.Success).data[position].id
            when (val result = remoteLibraryRepository.enableSubscription(id)) {
                is Result.Success -> {
                }
                is Result.Error -> _refreshFinish.value = Event(result.exception)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class SubscriptionViewModelFactory(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(SubscriptionViewModel::class.java) ->
                SubscriptionViewModel(remoteLibraryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
