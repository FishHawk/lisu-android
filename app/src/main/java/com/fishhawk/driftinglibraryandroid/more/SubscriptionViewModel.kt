package com.fishhawk.driftinglibraryandroid.more

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.base.BaseListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BaseListViewModel<Subscription>() {
    override suspend fun loadResult(): Result<List<Subscription>> {
        return remoteLibraryRepository.getAllSubscription()
    }

    suspend fun enableSubscription(id: Int): Result<Subscription> =
        remoteLibraryRepository.enableSubscription(id)

    suspend fun disableSubscription(id: Int): Result<Subscription> =
        remoteLibraryRepository.disableSubscription(id)

    suspend fun deleteSubscription(id: Int): Result<Subscription> =
        remoteLibraryRepository.deleteSubscription(id)

    suspend fun enableAllSubscription(): Result<List<Subscription>> {
        val result = remoteLibraryRepository.enableAllSubscription()
        if (result is Result.Success) _list.value = Result.Success(result.data.toMutableList())
        return result
    }

    suspend fun disableAllSubscription(): Result<List<Subscription>> {
        val result = remoteLibraryRepository.disableAllSubscription()
        if (result is Result.Success) _list.value = Result.Success(result.data.toMutableList())
        return result
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
