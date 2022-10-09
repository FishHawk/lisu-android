package com.fishhawk.lisu.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.model.ChapterDownloadTask
import com.fishhawk.lisu.data.network.model.MangaDownloadTask
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val lisuRepository: LisuRepository,
) : ViewModel() {
    val tasks = lisuRepository.listMangaDownloadTask()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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