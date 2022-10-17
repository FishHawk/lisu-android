package com.fishhawk.lisu.data

import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.fishhawk.lisu.data.network.model.*
import java.time.Instant
import java.time.LocalDate
import kotlin.random.Random

private val LOREM_IPSUM_SOURCE = """
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sodales
laoreet commodo. Phasellus a purus eu risus elementum consequat. Aenean eu
elit ut nunc convallis laoreet non ut libero. Suspendisse interdum placerat
risus vel ornare. Donec vehicula, turpis sed consectetur ullamcorper, ante
nunc egestas quam, ultricies adipiscing velit enim at nunc. Aenean id diam
neque. Praesent ut lacus sed justo viverra fermentum et ut sem. Fusce
convallis gravida lacinia. Integer semper dolor ut elit sagittis lacinia.
Praesent sodales scelerisque eros at rhoncus. Duis posuere sapien vel ipsum
ornare interdum at eu quam. Vestibulum vel massa erat. Aenean quis sagittis
purus. Phasellus arcu purus, rutrum id consectetur non, bibendum at nibh.

Duis nec erat dolor. Nulla vitae consectetur ligula. Quisque nec mi est. Ut
quam ante, rutrum at pellentesque gravida, pretium in dui. Cras eget sapien
velit. Suspendisse ut sem nec tellus vehicula eleifend sit amet quis velit.
Phasellus quis suscipit nisi. Nam elementum malesuada tincidunt. Curabitur
iaculis pretium eros, malesuada faucibus leo eleifend a. Curabitur congue
orci in neque euismod a blandit libero vehicula.
""".trim().split(" ")

object LoremIpsum {
    fun words(size: Int): String {
        var wordsUsed = 0
        val loremIpsumMaxSize = LOREM_IPSUM_SOURCE.size
        return generateSequence {
            LOREM_IPSUM_SOURCE[wordsUsed++ % loremIpsumMaxSize]
        }.take(size).joinToString(" ")
    }

    fun username() = words(2)
    fun cover() = "https://api.lorem.space/image/book"
    fun date() = Instant.now().epochSecond
    fun id() = Random.nextInt(10000, 99999).toString()

    fun mangaDetail() = MangaDetailDto(
        state = MangaState.Remote,
        providerId = words(1),
        id = words(1),
        cover = cover(),
        updateTime = date(),
        title = words(3),
        authors = words(2).split(" "),
        isFinished = false,
        description = words(20),
        tags = mapOf("" to words(4).split(" ")),
        collections = mapOf(),
        chapterPreviews = emptyList(),
    )

    fun comment() = CommentDto(
        username = username(),
        content = words(20),
        createTime = date(),
        vote = (-100..200).random(),
    )

    fun provider() = ProviderDto(
        id = "漫画人" + id(),
        lang = listOf("zh", "en").random(),
        icon = cover(),
        boardModels = emptyMap(),
        isLogged = listOf(true, false, null).random(),
    )

    fun mangaDownloadTask() = MangaDownloadTask(
        providerId = "漫画人",
        mangaId = id(),
        cover = cover(),
        title = "龙珠",
        chapterTasks = emptyList(),
    )
}