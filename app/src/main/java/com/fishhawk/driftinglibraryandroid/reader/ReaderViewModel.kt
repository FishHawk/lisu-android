package com.fishhawk.driftinglibraryandroid.reader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.setting.PreferenceStringLiveData
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReaderViewModel : ViewModel() {
    private lateinit var id: String
    private lateinit var collection: String
    private lateinit var chapters: List<String>

    fun setup(id: String, collection: String, chapters: List<String>) {
        this.id = id
        this.collection = collection
        this.chapters = chapters
    }

    // reading direction
    val readingDirection: PreferenceStringLiveData = SettingsHelper.readingDirection
    val isReaderDirectionEqualLeftToRight: LiveData<Boolean> =
        Transformations.map(readingDirection) { it == SettingsHelper.READING_DIRECTION_LEFT_TO_RIGHT }
    val isReaderDirectionEqualRightToLeft: LiveData<Boolean> =
        Transformations.map(readingDirection) { it == SettingsHelper.READING_DIRECTION_RIGHT_TO_LEFT }
    val isReaderDirectionEqualVertical: LiveData<Boolean> =
        Transformations.map(readingDirection) { it == SettingsHelper.READING_DIRECTION_VERTICAL }

    // menu
    val isMenuVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    // reader content
    val readerContent: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var startPage: Int = 0

    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val chapterPosition: MutableLiveData<Int> = MutableLiveData(0)
    val chapterSize: MutableLiveData<Int> = MutableLiveData(0)
    val chapterTitle: MutableLiveData<String> = MutableLiveData("")
    private var chapterIndex: Int = 0


    fun openChapter(chapterIndex: Int, isFromStart: Boolean = true) {
        this.chapterIndex = chapterIndex
        isLoading.value = true
        val chapter = chapters[chapterIndex]
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = Repository.getChapterContent(id, collection, chapter)) {
                is Result.Success -> {
                    startPage = if (isFromStart) 0 else result.data.size - 1
                    readerContent.value = result.data
                    chapterTitle.value = chapter
                }
            }
            isLoading.value = false
        }
    }

    fun openNextChapter(): Boolean {
        if (chapterIndex < chapters.size - 1) {
            openChapter(chapterIndex + 1)
            return true
        }
        return false
    }

    fun openPrevChapter(): Boolean {
        if (chapterIndex > 0) {
            openChapter(chapterIndex - 1, isFromStart = false)
            return true
        }
        return false
    }
}

