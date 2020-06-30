package com.fishhawk.driftinglibraryandroid.more

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.base.BaseListViewModel
import com.fishhawk.driftinglibraryandroid.library.EmptyListException
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.*
import okhttp3.internal.wait

class SubscriptionViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BaseListViewModel<Subscription>() {
    fun enable(position: Int) {
        viewModelScope.launch {
            val id = (list.value as Result.Success).data[position].id
            when (val result = remoteLibraryRepository.enableSubscription(id)) {
                is Result.Success -> {
                }
                is Result.Error -> _refreshFinish.value = Event(result.exception)
            }
        }
    }

    override suspend fun loadResult(): Result<List<Subscription>> {
        return remoteLibraryRepository.getSubscriptions()
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
