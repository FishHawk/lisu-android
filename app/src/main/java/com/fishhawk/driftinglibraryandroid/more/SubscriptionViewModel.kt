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
    override suspend fun loadResult(): Result<List<Subscription>> {
        return remoteLibraryRepository.getSubscriptions()
    }

    suspend fun enable(id: Int): Result<Subscription> =
        remoteLibraryRepository.enableSubscription(id)

    suspend fun disable(id: Int): Result<Subscription> =
        remoteLibraryRepository.disableSubscription(id)

    suspend fun delete(id: Int): Result<Subscription> =
        remoteLibraryRepository.deleteSubscription(id)
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
