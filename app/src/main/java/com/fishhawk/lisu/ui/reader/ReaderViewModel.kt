package com.fishhawk.lisu.ui.reader

import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.MangaSettingRepository
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.database.model.MangaSetting
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.datastore.next
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.model.Chapter
import com.fishhawk.lisu.data.network.model.MangaDetailDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class ReaderChapter(
    val collectionId: String,
    val index: Int,
    chapter: Chapter,
) {
    val id = chapter.id
    val title = chapter.title
    val name = chapter.name
    var hasInitialed = false
    var urls: Result<List<String>>? = null
}

sealed interface ReaderPage {
    data class Image(
        val chapter: ReaderChapter,
        val index: Int,
    ) : ReaderPage {
        private val urls = chapter.urls!!.getOrNull()!!
        val url = urls[index]
        val size = urls.size
    }

    data class NextChapterState(
        private val currChapter: ReaderChapter,
        private val nextChapter: ReaderChapter?,
    ) : ReaderPage {
        val currentChapterName = currChapter.name
        val currentChapterTitle = currChapter.title
        val nextChapterName = nextChapter?.name
        val nextChapterTitle = nextChapter?.title
        val nextChapterState = nextChapter?.urls?.map { }
    }

    data class PrevChapterState(
        val currChapter: ReaderChapter,
        val prevChapter: ReaderChapter?,
    ) : ReaderPage {
        val currentChapterName = currChapter.name
        val currentChapterTitle = currChapter.title
        val prevChapterName = prevChapter?.name
        val prevChapterTitle = prevChapter?.title
        val prevChapterState = prevChapter?.urls?.map { }
    }
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
        args.getString("detail")
            ?.let { Json.decodeFromString(MangaDetailDto.serializer(), it) }
            ?.providerId
            ?: args.getString("providerId")!!

    private val mangaId =
        args.getString("detail")
            ?.let { Json.decodeFromString(MangaDetailDto.serializer(), it) }
            ?.id
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
                args.getString("detail")
                    ?.let { Json.decodeFromString(MangaDetailDto.serializer(), it) }
                    ?.let { Result.success(it) },
            )

    val mangaTitleResult = mangaResult
        .map { result -> result?.map { it.title ?: it.id } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun reloadManga() {
        viewModelScope.launch {
            mangaData.value?.reload()
        }
    }

    private lateinit var chapterList: List<ReaderChapter>

    var startPage = args.getInt("page", 0)
    private val initialChapterIndex = MutableStateFlow<Int?>(null)
    private val currentPage = MutableStateFlow<ReaderPage.Image?>(null)
    private val chapterListUpdated = MutableStateFlow(0)

    fun notifyCurrentPage(page: ReaderPage.Image) {
        currentPage.value = page
    }

    val pages = combine(
        initialChapterIndex.filterNotNull(),
        currentPage.map { it?.chapter },
        chapterListUpdated,
    ) { initialChapterIndex, currentChapter, s ->
        val initialChapter = chapterList.getOrNull(initialChapterIndex)
            ?: chapterList.first()

        if (currentChapter == null) {
            loadChapterFirst(initialChapter)
            initialChapter.urls?.map { initialChapterUrls ->
                initialChapterUrls.indices.map { ReaderPage.Image(initialChapter, it) }
            }
        } else {
            val dynamicPages = listOfNotNull(
                chapterList.getOrNull(currentChapter.index - 1),
                currentChapter,
                chapterList.getOrNull(currentChapter.index + 1),
            ).flatMap { chapter ->
                loadChapterFirst(chapter)
                chapter.urls?.getOrNull()?.indices?.map { ReaderPage.Image(chapter, it) }
                    ?: emptyList()
            }
            val totalPages = chapterList.flatMap { chapter ->
                chapter.urls?.getOrNull()?.indices?.map { ReaderPage.Image(chapter, it) }
                    ?: emptyList()
            }
            Result.success(totalPages)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        val collectionId = args.getString("collectionId")!!
        viewModelScope.launch {
            val detail = mangaResult.map { it?.getOrNull() }.filterNotNull().first()

            chapterList = detail.collections[collectionId]!!
                .mapIndexed { index, chapter -> ReaderChapter(collectionId, index, chapter) }
            initialChapterIndex.value =
                chapterList.indexOfFirst { it.id == args.getString("chapterId") }
        }
    }

    private fun loadChapterFirst(chapter: ReaderChapter) {
        if (chapter.hasInitialed) return
        chapter.hasInitialed = true

        viewModelScope.launch {
            val result = lisuRepository.getContent(
                providerId,
                mangaId,
                chapter.collectionId,
                chapter.id
            )
            if (chapter.urls?.isSuccess != true) {
                chapter.urls = result
                chapterListUpdated.value += 1
            }
        }
    }

    fun reloadChapter(chapterIndex: Int? = null) {
        val chapter = chapterList[chapterIndex ?: initialChapterIndex.value!!]
        if (chapter.urls?.isFailure == true) {
            chapter.urls = null
            viewModelScope.launch {
                val result = lisuRepository.getContent(
                    providerId,
                    mangaId,
                    chapter.collectionId,
                    chapter.id
                )
                if (chapter.urls?.isSuccess != true) {
                    chapter.urls = result
                    chapterListUpdated.value += 1
                }
            }
        }
    }

//    val isOnlyOneChapter = chapterList
//        .map { it?.size == 1 }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

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

    private fun sendMessage(resId: Int) {
        viewModelScope.launch {
            sendEvent(ReaderEffect.Message(resId))
        }
    }

    fun openPrevChapter() {
        val currentChapterIndex = currentPage.value?.chapter?.index ?: return
        if (currentChapterIndex == 0)
            return sendMessage(R.string.no_prev_chapter)
        startPage = 0
        currentPage.value = null
        initialChapterIndex.value = currentChapterIndex - 1
    }

    fun openNextChapter() {
        val currentChapterIndex = currentPage.value?.chapter?.index ?: return
        if (currentChapterIndex == chapterList.size - 1)
            return sendMessage(R.string.no_next_chapter)
        startPage = 0
        currentPage.value = null
        initialChapterIndex.value = currentChapterIndex + 1
    }

    private fun updateReadingHistory(page: ReaderPage.Image) {
        val detail = mangaResult.value?.getOrNull() ?: return
        val readingHistory = ReadingHistory(
            state = detail.state,
            providerId = detail.providerId,
            mangaId = mangaId,
            cover = detail.cover,
            title = detail.title,
            authors = detail.authors.joinToString(separator = ";"),
            collectionId = page.chapter.collectionId,
            chapterId = page.chapter.id,
            chapterName = page.chapter.name ?: page.chapter.id,
            page = page.index,
        )
        viewModelScope.launch {
            readingHistoryRepository.update(readingHistory)
        }
    }

    fun updateCover(bitmap: Bitmap) {
        viewModelScope.launch {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            lisuRepository.updateMangaCover(
                providerId = providerId,
                mangaId = mangaId,
                cover = byteArray,
                coverType = "image/png",
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