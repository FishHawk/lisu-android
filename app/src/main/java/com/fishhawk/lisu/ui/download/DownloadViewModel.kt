package com.fishhawk.lisu.ui.download

import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.model.ChapterDownloadTask
import com.fishhawk.lisu.data.network.model.MangaDownloadTask
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DownloadEvent : Event {
    object Reload : DownloadEvent
}

class DownloadViewModel(
    private val lisuRepository: LisuRepository,
) : BaseViewModel<DownloadEvent>() {
    private val tasksRemoteData = lisuRepository.listMangaDownloadTask()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val tasksResult = tasksRemoteData
        .map { it?.value }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun reload() {
        viewModelScope.launch {
            tasksRemoteData.value?.reload()
        }
    }

    fun startAllTasks() {
        viewModelScope.launch {
            lisuRepository.startAllTasks()
        }
    }

    fun cancelAllTasks() {
        viewModelScope.launch {
            lisuRepository.cancelAllTasks()
        }
    }

    fun startMangaTask(mangaTask: MangaDownloadTask) {
        viewModelScope.launch {
            lisuRepository.startMangaTask(
                providerId = mangaTask.providerId,
                mangaId = mangaTask.mangaId,
            )
        }
    }

    fun cancelMangaTask(mangaTask: MangaDownloadTask) {
        viewModelScope.launch {
            lisuRepository.cancelMangaTask(
                providerId = mangaTask.providerId,
                mangaId = mangaTask.mangaId,
            )
        }
    }

    fun startChapterTask(mangaTask: MangaDownloadTask, chapterTask: ChapterDownloadTask) {
        viewModelScope.launch {
            lisuRepository.startChapterTask(
                providerId = mangaTask.providerId,
                mangaId = mangaTask.mangaId,
                collectionId = chapterTask.collectionId,
                chapterId = chapterTask.chapterId,
            )
        }
    }

    fun cancelChapterTask(mangaTask: MangaDownloadTask, chapterTask: ChapterDownloadTask) {
        viewModelScope.launch {
            lisuRepository.cancelChapterTask(
                providerId = mangaTask.providerId,
                mangaId = mangaTask.mangaId,
                collectionId = chapterTask.collectionId,
                chapterId = chapterTask.chapterId,
            )
        }
    }
}