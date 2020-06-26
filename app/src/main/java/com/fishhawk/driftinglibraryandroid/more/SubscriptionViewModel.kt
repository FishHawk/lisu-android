package com.fishhawk.driftinglibraryandroid.more

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.library.EmptyListException
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    private val _subscriptions: MutableLiveData<Result<List<Subscription>>> =
        MutableLiveData(Result.Loading)
    val subscriptions: LiveData<Result<List<Subscription>>> = _subscriptions

    private val _refreshFinish: MutableLiveData<Event<Throwable?>> = MutableLiveData()
    val refreshFinish: LiveData<Event<Throwable?>> = _refreshFinish

    fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.getSubscriptions()
            _subscriptions.value = result
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
