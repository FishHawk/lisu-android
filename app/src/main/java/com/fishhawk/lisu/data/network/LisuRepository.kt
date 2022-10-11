package com.fishhawk.lisu.data.network

import com.fishhawk.lisu.data.network.base.*
import com.fishhawk.lisu.data.network.dao.LisuDao
import com.fishhawk.lisu.data.network.model.*
import com.fishhawk.lisu.util.flatten
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

class LisuRepository(
    urlFlow: Flow<String>,
    private val client: HttpClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val daoFlow = urlFlow
        .map {
            if (it.isBlank()) {
                Result.failure(Exception("sdaf"))
            } else {
                val url = if (
                    it.startsWith("https:", ignoreCase = true) ||
                    it.startsWith("http:", ignoreCase = true)
                ) it else "http://$it"
                runCatching { URLBuilder(url).buildString() }
            }
        }
        .distinctUntilChanged()
        .map { it.map { url -> LisuDao(client, url) } }
        .stateIn(scope, SharingStarted.Eagerly, null)


    private suspend inline fun <T> oneshot(crossinline func: suspend (LisuDao) -> T): Result<T> {
        return daoFlow.filterNotNull().first().mapCatching { func(it) }
    }

    private val mangaActionChannels = mutableListOf<RemoteDataActionChannel<MangaDetailDto>>()
    private val providerMangaListActionChannels = mutableListOf<RemoteListActionChannel<MangaDto>>()
    private val libraryMangaListActionChannels = mutableListOf<RemoteListActionChannel<MangaDto>>()

    // Provider API
    val providers =
        daoFlow.filterNotNull().flatMapLatest {
            remoteData(loader = { it.mapCatching { dao -> dao.listProvider() } })
        }.stateIn(scope, SharingStarted.Eagerly, null)

    suspend fun loginByCookies(
        providerId: String,
        cookies: Map<String, String>,
    ): Result<String> =
        oneshot { it.loginByCookies(providerId, cookies) }.onSuccess {
            mutateProvider(providerId = providerId) {
                it.copy(isLogged = true)
            }
        }

    suspend fun loginByPassword(
        providerId: String,
        username: String,
        password: String,
    ): Result<String> =
        oneshot { it.loginByPassword(providerId, username, password) }.onSuccess {
            mutateProvider(providerId = providerId) {
                it.copy(isLogged = true)
            }
        }

    suspend fun logout(providerId: String): Result<String> =
        oneshot { it.logout(providerId) }.onSuccess {
            mutateProvider(providerId = providerId) {
                it.copy(isLogged = false)
            }
        }

    fun getBoard(
        providerId: String,
        boardId: BoardId,
        filterValues: BoardFilterValue,
        keywords: String? = null,
    ) = daoFlow.filterNotNull().flatMapLatest {
        remotePagingList(
            startKey = 0,
            loader = { page ->
                it.mapCatching { dao ->
                    dao.getBoard(
                        providerId = providerId,
                        boardId = boardId.name,
                        page = page,
                        filterValues = filterValues,
                        keywords = keywords,
                    )
                }.map { Page(it, if (it.isEmpty()) null else page + 1) }
            },
            onStart = { providerMangaListActionChannels.add(it) },
            onClose = { providerMangaListActionChannels.remove(it) },
        )
    }

    fun getManga(
        providerId: String,
        mangaId: String,
    ): Flow<RemoteData<MangaDetailDto>> =
        daoFlow.filterNotNull().flatMapLatest {
            remoteData(
                loader = { it.mapCatching { dao -> dao.getManga(providerId, mangaId) } },
                onStart = { mangaActionChannels.add(it) },
                onClose = { mangaActionChannels.remove(it) },
            )
        }

    suspend fun updateMangaMetadata(
        providerId: String,
        mangaId: String,
        metadata: MangaMetadata,
    ): Result<MangaMetadata> =
        oneshot { it.updateMangaMetadata(providerId, mangaId, metadata) }.onSuccess { newMetadata ->
            mutateManga(providerId = providerId, mangaId = mangaId) {
                it.copy(
                    title = newMetadata.title,
                    authors = newMetadata.authors,
                    isFinished = newMetadata.isFinished,
                    description = newMetadata.description,
                    tags = newMetadata.tags,
                )
            }
            mutateMangaInLibraryList(providerId = providerId, mangaId = mangaId) {
                it.copy(
                    title = newMetadata.title,
                    authors = newMetadata.authors,
                    isFinished = newMetadata.isFinished,
                )
            }
            mutateMangaInProviderList(providerId = providerId, mangaId = mangaId) {
                it.copy(
                    title = newMetadata.title,
                    authors = newMetadata.authors,
                    isFinished = newMetadata.isFinished,
                )
            }
        }

    suspend fun updateMangaCover(
        providerId: String,
        mangaId: String,
        cover: ByteArray,
        coverType: String,
    ): Result<String> =
        oneshot { it.updateMangaCover(providerId, mangaId, cover, coverType) }

    fun getComment(
        providerId: String,
        mangaId: String,
    ) = daoFlow.filterNotNull().flatMapLatest {
        remotePagingList(
            startKey = 0,
            loader = { page ->
                it.mapCatching { dao -> dao.getComment(providerId, mangaId, page) }
                    .map { Page(it, if (it.isEmpty()) null else page + 1) }
            },
        )
    }

    suspend fun getContent(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): Result<List<String>> =
        oneshot { it.getContent(providerId, mangaId, collectionId, chapterId) }

    suspend fun getRandomMangaFromLibrary(): Result<MangaDto> =
        oneshot { it.getRandomMangaFromLibrary() }

    // Library API
    suspend fun searchFromLibrary(keywords: String = "") =
        daoFlow.filterNotNull().flatMapLatest {
            remotePagingList(
                startKey = 0,
                loader = { page ->
                    it.mapCatching { dao -> dao.searchFromLibrary(page, keywords) }
                        .map { Page(it, if (it.isEmpty()) null else page + 1) }
                },
                onStart = { libraryMangaListActionChannels.add(it) },
                onClose = { libraryMangaListActionChannels.remove(it) },
            )
        }

    suspend fun addMangaToLibrary(providerId: String, mangaId: String): Result<String> =
        oneshot { it.addMangaToLibrary(providerId, mangaId) }.onSuccess {
            mutateManga(providerId = providerId, mangaId = mangaId) {
                it.copy(state = MangaState.RemoteInLibrary)
            }
            mutateMangaInProviderList(providerId = providerId, mangaId = mangaId) {
                it.copy(state = MangaState.RemoteInLibrary)
            }
            libraryMangaListActionChannels.forEach { ch -> ch.reload() }
        }

    suspend fun removeMangaFromLibrary(providerId: String, mangaId: String): Result<String> =
        oneshot { it.removeMangaFromLibrary(providerId, mangaId) }.onSuccess {
            mutateManga(providerId = providerId, mangaId = mangaId) {
                it.copy(state = MangaState.Remote)
            }
            mutateMangaInProviderList(providerId = providerId, mangaId = mangaId) {
                it.copy(state = MangaState.Remote)
            }
            libraryMangaListActionChannels.forEach { ch ->
                ch.mutate { list ->
                    val removed = list.removeIf { it.providerId == providerId && it.id == mangaId }
                    if (removed) list.toMutableList() else list
                }
            }
        }

    suspend fun removeMultipleMangasFromLibrary(mangas: List<MangaKeyDto>): Result<String> =
        oneshot { it.removeMultipleMangasFromLibrary(mangas) }.onSuccess {
            providerMangaListActionChannels.forEach { ch ->
                ch.mutate { list ->
                    list.map {
                        if (
                            mangas.any { removedManga ->
                                it.providerId == removedManga.providerId &&
                                        it.id == removedManga.id
                            }
                        ) it.copy(state = MangaState.Remote)
                        else it
                    }.toMutableList()
                }
            }
            libraryMangaListActionChannels.forEach { ch ->
                ch.mutate { list ->
                    val removed = list.removeIf {
                        mangas.any { removedManga ->
                            it.providerId == removedManga.providerId &&
                                    it.id == removedManga.id
                        }
                    }
                    if (removed) list.toMutableList() else list
                }
            }
        }

    // Download API
    fun listMangaDownloadTask(): Flow<Result<List<MangaDownloadTask>>?> =
        daoFlow.filterNotNull().flatMapLatest {
            flatten(it.map { dao -> dao.listMangaDownloadTask() })
        }.catch { emit(Result.failure(it)) }

    suspend fun startAllTasks(): Result<String> =
        oneshot { it.startAllTasks() }

    suspend fun startMangaTask(
        providerId: String,
        mangaId: String,
    ): Result<String> =
        oneshot { it.startMangaTask(providerId, mangaId) }

    suspend fun startChapterTask(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): Result<String> =
        oneshot { it.startChapterTask(providerId, mangaId, collectionId, chapterId) }

    suspend fun cancelAllTasks(): Result<String> =
        oneshot { it.cancelAllTasks() }

    suspend fun cancelMangaTask(
        providerId: String,
        mangaId: String,
    ): Result<String> =
        oneshot { it.cancelMangaTask(providerId, mangaId) }

    suspend fun cancelChapterTask(
        providerId: String,
        mangaId: String,
        collectionId: String,
        chapterId: String,
    ): Result<String> =
        oneshot { it.cancelChapterTask(providerId, mangaId, collectionId, chapterId) }

    // Util
    private suspend fun mutateProvider(
        providerId: String,
        transform: (ProviderDto) -> ProviderDto,
    ) {
        providers.value?.mutate { list ->
            list.toMutableList().map {
                if (it.id == providerId) transform(it)
                else it
            }
        }
    }

    private suspend fun mutateManga(
        providerId: String,
        mangaId: String,
        transform: (MangaDetailDto) -> MangaDetailDto,
    ) {
        mangaActionChannels.forEach { ch ->
            ch.mutate {
                if (it.providerId == providerId && it.id == mangaId) transform(it)
                else it
            }
        }
    }

    private suspend fun mutateMangaInProviderList(
        providerId: String,
        mangaId: String,
        transform: (MangaDto) -> MangaDto,
    ) {
        providerMangaListActionChannels.forEach { ch ->
            ch.mutate { list ->
                if (list.firstOrNull()?.providerId != providerId) {
                    list
                } else {
                    list
                        .map { if (it.id == mangaId) transform(it) else it }
                        .toMutableList()
                }
            }
        }
    }

    private suspend fun mutateMangaInLibraryList(
        providerId: String,
        mangaId: String,
        transform: (MangaDto) -> MangaDto,
    ) {
        libraryMangaListActionChannels.forEach { ch ->
            ch.mutate { list ->
                list
                    .map { if (it.providerId == providerId && it.id == mangaId) transform(it) else it }
                    .toMutableList()
            }
        }
    }
}