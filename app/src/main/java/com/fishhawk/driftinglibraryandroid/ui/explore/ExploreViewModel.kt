package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Source
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper


class ExploreViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    val sourceList: LiveData<Result<List<Source>>> =
        SettingsHelper.selectedServer.switchMap {
            liveData {
                emit(Result.Loading)
                emit(remoteLibraryRepository.getSources())
            }
        }
}
