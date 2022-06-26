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
import com.fishhawk.lisu.data.datastore.ReaderMode
import com.fishhawk.lisu.data.datastore.ReaderOrientation
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.model.ChapterDto
import com.fishhawk.lisu.data.network.model.MangaDetailDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

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
        .onEach { chapters ->
            val chapterId = args.getString("chapterId")
            val chapterIndex = chapters.indexOfFirst { it.id == chapterId }
            chapterPointer = MutableStateFlow(
                ReaderChapterPointer(
                    index = if (chapterIndex < 0) 0 else chapterIndex,
                    startPage = args.getInt("page", 0)
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    inner class ReaderChapterPointer(
        val index: Int,
        val startPage: Int
    ) {
        val currChapter get() = chapterList.value!![index]
        val nextChapter get() = chapterList.value!!.getOrNull(index + 1)
        val prevChapter get() = chapterList.value!!.getOrNull(index - 1)
    }

    lateinit var chapterPointer: MutableStateFlow<ReaderChapterPointer>

    val isMenuOpened = MutableStateFlow(false)

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

    private fun loadChapterPointer() = viewModelScope.launch {
        val pointer = chapterPointer.value
        loadChapter(pointer.currChapter)
        pointer.prevChapter?.let { loadChapter(it) }
        pointer.nextChapter?.let { loadChapter(it) }
    }

    private suspend fun loadChapter(chapter: ReaderChapter) {
        if (chapter.content?.isSuccess == true) return
        chapter.content = null

        fun refreshPointerIfNeed() {
            chapterPointer.value.let { pointer ->
                if (chapter.index == pointer.index) {
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

    fun reloadManga() {
        viewModelScope.launch {
            mangaData.value?.reload()
        }
    }

    private fun sendMessage(resId: Int) {
        viewModelScope.launch {
            sendEvent(ReaderEffect.Message(resId))
        }
    }

    fun openNextChapter() {
        val pointer = chapterPointer.value
        val nextChapter = pointer.nextChapter
            ?: return sendMessage(R.string.no_next_chapter)
        chapterPointer.value =
            ReaderChapterPointer(nextChapter.index, 0)
        loadChapterPointer()
    }

    fun openPrevChapter() {
        val pointer = chapterPointer.value
        val prevChapter = pointer.prevChapter
            ?: return sendMessage(R.string.no_prev_chapter)
        chapterPointer.value =
            ReaderChapterPointer(prevChapter.index, 0)
        loadChapterPointer()
    }

    fun moveToNextChapter() {
        val pointer = chapterPointer.value
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
        val pointer = chapterPointer.value
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

    fun updateReadingHistory(page: Int) = viewModelScope.launch {
        val detail = mangaResult.value?.getOrNull() ?: return@launch
        val chapter = chapterPointer.value.currChapter
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

    fun updateCover(drawable: Drawable) = viewModelScope.launch {
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

    fun setReaderMode(value: ReaderMode) {
        viewModelScope.launch {
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

    fun setReaderOrientation(value: ReaderOrientation) {
        viewModelScope.launch {
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