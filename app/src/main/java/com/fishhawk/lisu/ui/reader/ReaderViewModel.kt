package com.fishhawk.lisu.ui.reader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.MangaSettingRepository
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.database.model.MangaSetting
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.datastore.next
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.model.ChapterDto
import com.fishhawk.lisu.data.network.model.MangaDetailDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

sealed interface ReaderPage {
    object Empty : ReaderPage
    data class Image(val index: Int, val url: String) : ReaderPage
    data class NextChapterState(
        val currentChapterName: String,
        val currentChapterTitle: String,
        val nextChapterName: String,
        val nextChapterTitle: String,
        val nextChapterState: Result<Unit>?,
    ) : ReaderPage

    data class PrevChapterState(
        val currentChapterName: String,
        val currentChapterTitle: String,
        val prevChapterName: String,
        val prevChapterTitle: String,
        val prevChapterState: Result<Unit>?,
    ) : ReaderPage
}

class ReaderChapter(
    val collectionId: String,
    val index: Int,
    chapter: ChapterDto
) {
    val id = chapter.id
    val title = chapter.title
    val name = chapter.name
    var content: Result<List<String>>? = null
}

sealed interface ReaderEffect : Event {
    data class Message(val redId: Int) : ReaderEffect
}

class ReaderViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val mangaSettingRepository: MangaSettingRepository,
) : BaseViewModel<ReaderEffect>() {

    private val providerId =
        args.getParcelable<MangaDetailDto>("detail")?.providerId
            ?: args.getString("providerId")!!

    private val mangaId =
        args.getParcelable<MangaDetailDto>("detail")?.id
            ?: args.getString("mangaId")!!

    private val mangaData =
        lisuRepository.getManga(providerId, mangaId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val mangaResult =
        mangaData
            .map { it?.value }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                args.getParcelable<MangaDetailDto>("detail")?.let { Result.success(it) })

    val mangaTitleResult = mangaResult
        .map { result -> result?.map { it.title ?: it.id } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val chapterList = mangaResult
        .mapNotNull { it?.getOrNull() }
        .map { detail ->
            val collectionId = args.getString("collectionId")!!
            val chapterId = args.getString("chapterId")!!
            when {
                collectionId.isNotBlank() -> {
                    detail.collections[collectionId]!!.mapIndexed { index, chapter ->
                        ReaderChapter(collectionId, index, chapter)
                    }
                }
                chapterId.isNotBlank() -> {
                    detail.chapters.mapIndexed { index, chapter ->
                        ReaderChapter(collectionId, index, chapter)
                    }
                }
                else -> {
                    listOf(
                        ReaderChapter(
                            collectionId,
                            0,
                            ChapterDto(id = " ", name = "", title = "")
                        )
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        chapterList
            .filterNotNull()
            .onEach { chapters ->
                val chapterId = args.getString("chapterId")
                val chapterIndex = chapters.indexOfFirst { it.id == chapterId }
                chapterPointer.value = ReaderChapterPointer(
                    index = if (chapterIndex < 0) 0 else chapterIndex,
                    startPage = args.getInt("page", 0),
                )
            }
            .launchIn(viewModelScope)
    }

    inner class ReaderChapterPointer(
        val index: Int,
        val startPage: Int,
    ) {
        val chapterName get() = currChapter.name
        val chapterTitle get() = currChapter.title
        val pages: Result<List<ReaderPage>>?

        init {
            val prevChapterStatePage = prevChapter?.let {
                ReaderPage.PrevChapterState(
                    currentChapterName = currChapter.name,
                    currentChapterTitle = currChapter.title,
                    prevChapterName = it.name,
                    prevChapterTitle = it.title,
                    prevChapterState = it.content?.map { },
                )
            }
            val nextChapterStatePage = nextChapter?.let {
                ReaderPage.NextChapterState(
                    currentChapterName = currChapter.name,
                    currentChapterTitle = currChapter.title,
                    nextChapterName = it.name,
                    nextChapterTitle = it.title,
                    nextChapterState = it.content?.map { },
                )
            }
            pages = currChapter.content?.map {
                if (it.isEmpty()) listOfNotNull(
//                    prevChapterStatePage,
                    ReaderPage.Empty,
//                    nextChapterStatePage
                )
                else listOfNotNull(
//                    prevChapterStatePage,
                    *it.mapIndexed { index, url -> ReaderPage.Image(index = index, url = url) }
                        .toTypedArray(),
//                    nextChapterStatePage
                )
            }
        }
    }

    private val ReaderChapterPointer.currChapter get() = chapterList.value!![index]
    private val ReaderChapterPointer.nextChapter get() = chapterList.value!!.getOrNull(index + 1)
    private val ReaderChapterPointer.prevChapter get() = chapterList.value!!.getOrNull(index - 1)

    val chapterPointer = MutableStateFlow<ReaderChapterPointer?>(null)

    val isOnlyOneChapter = chapterList
        .map { it?.size == 1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val mangaSetting = mangaSettingRepository
        .select(providerId, mangaId, null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val readerMode = mangaSetting
        .map { it?.readerMode }
        .transformLatest {
            if (it != null) emit(it)
            else emitAll(PR.readerMode.flow)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val readerOrientation = mangaSetting
        .map { it?.readerOrientation }
        .transformLatest {
            if (it != null) emit(it)
            else emitAll(PR.readerOrientation.flow)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        reloadManga()
        chapterList
            .filterNotNull()
            .onEach { loadChapterPointer() }
            .launchIn(viewModelScope)
    }

    fun reloadManga() {
        viewModelScope.launch {
            mangaData.value?.reload()
        }
    }

    fun loadChapterPointer() {
        viewModelScope.launch {
            val pointer = chapterPointer.value ?: return@launch
            loadChapter(pointer.currChapter)
            pointer.prevChapter?.let { loadChapter(it) }
            pointer.nextChapter?.let { loadChapter(it) }
        }
    }

    private suspend fun loadChapter(chapter: ReaderChapter) {
        if (chapter.content?.isSuccess == true) return
        chapter.content = null

        fun refreshPointerIfNeed() {
            chapterPointer.value?.let { pointer ->
                if (chapter.index - pointer.index <= 1) {
                    chapterPointer.value = ReaderChapterPointer(pointer.index, pointer.startPage)
                }
            }
        }

        val result = lisuRepository.getContent(
            providerId,
            mangaId,
            chapter.collectionId,
            chapter.id
        )

        if (chapter.content?.isSuccess != true) {
            chapter.content = result
            refreshPointerIfNeed()
        }
    }

    private fun sendMessage(resId: Int) {
        viewModelScope.launch {
            sendEvent(ReaderEffect.Message(resId))
        }
    }

    fun openNextChapter() {
        val pointer = chapterPointer.value ?: return
        val nextChapter = pointer.nextChapter
            ?: return sendMessage(R.string.no_next_chapter)
        chapterPointer.value =
            ReaderChapterPointer(nextChapter.index, 0)
        loadChapterPointer()
    }

    fun openPrevChapter() {
        val pointer = chapterPointer.value ?: return
        val prevChapter = pointer.prevChapter
            ?: return sendMessage(R.string.no_prev_chapter)
        chapterPointer.value =
            ReaderChapterPointer(prevChapter.index, 0)
        loadChapterPointer()
    }

    fun moveToNextChapter() {
        val pointer = chapterPointer.value ?: return
        val nextChapter = pointer.nextChapter
            ?: return sendMessage(R.string.no_next_chapter)

        if (nextChapter.content?.isSuccess == true) {
            chapterPointer.value =
                ReaderChapterPointer(nextChapter.index, 0)
            loadChapterPointer()
        } else {
            viewModelScope.launch { loadChapter(nextChapter) }
        }
    }

    fun moveToPrevChapter() {
        val pointer = chapterPointer.value ?: return
        val prevChapter = pointer.prevChapter
            ?: return sendMessage(R.string.no_prev_chapter)

        if (prevChapter.content?.isSuccess == true) {
            chapterPointer.value =
                ReaderChapterPointer(prevChapter.index, Int.MAX_VALUE)
            loadChapterPointer()
        } else {
            viewModelScope.launch { loadChapter(prevChapter) }
        }
    }

    fun updateReadingHistory(page: Int) {
        viewModelScope.launch {
            val detail = mangaResult.value?.getOrNull() ?: return@launch
            val chapter = chapterPointer.value?.currChapter ?: return@launch
            val readingHistory = ReadingHistory(
                providerId = detail.providerId,
                mangaId = mangaId,
                cover = detail.cover,
                title = detail.title,
                authors = detail.authors.joinToString(separator = ";"),
                collectionId = chapter.collectionId,
                chapterId = chapter.id,
                chapterName = chapter.name,
                page = page
            )
            readingHistoryRepository.update(readingHistory)
        }
    }

    fun updateCover(drawable: Drawable) {
        viewModelScope.launch {
            val stream = ByteArrayOutputStream()
            drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            lisuRepository.updateMangaCover(
                providerId,
                mangaId,
                byteArray,
                "image/png",
            ).onSuccess { sendEvent(ReaderEffect.Message(R.string.cover_updated)) }
                .onFailure { sendEvent(ReaderEffect.Message(R.string.cover_update_failed)) }
        }
    }

    fun toggleReaderMode() {
        viewModelScope.launch {
            val value = readerMode.value?.next()
            val setting = mangaSetting.value?.copy(readerMode = value)
                ?: MangaSetting(
                    providerId = providerId,
                    mangaId = mangaId,
                    title = null,
                    readerMode = value,
                    readerOrientation = null,
                )
            mangaSettingRepository.update(setting)
        }
    }

    fun toggleReaderOrientation() {
        viewModelScope.launch {
            val value = readerOrientation.value?.next()
            val setting = mangaSetting.value?.copy(readerOrientation = value)
                ?: MangaSetting(
                    providerId = providerId,
                    mangaId = mangaId,
                    title = null,
                    readerMode = null,
                    readerOrientation = value,
                )
            mangaSettingRepository.update(setting)
        }
    }
}