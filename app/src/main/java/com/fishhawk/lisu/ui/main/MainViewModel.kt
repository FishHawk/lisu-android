package com.fishhawk.lisu.ui.main

import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.BuildConfig
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.get
import com.fishhawk.lisu.data.network.GitHubRepository
import com.fishhawk.lisu.data.network.model.GitHubRelease
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.Instant

sealed interface MainEvent : Event {
    object NoNewUpdates : MainEvent
    data class CheckUpdateFailure(val exception: Throwable) : MainEvent
    data class ShowUpdateDialog(val release: GitHubRelease) : MainEvent
    object AlreadyDownloading : MainEvent
    object NotifyDownloadStart : MainEvent
    data class NotifyDownloadProgress(val progress: Float) : MainEvent
    data class NotifyDownloadFinish(val file: File) : MainEvent
    data class NotifyDownloadError(val url: String) : MainEvent
}

class MainViewModel(
    private val repo: GitHubRepository,
) : BaseViewModel<MainEvent>() {
    private var isDownloading = false

    fun checkForUpdate(isUserPrompt: Boolean = false) {
        viewModelScope.launch {
            if (isUserPrompt.not() &&
                Duration.between(
                    Instant.ofEpochSecond(PR.lastAppCheckTime.get()),
                    Instant.now()
                ) < Duration.ofDays(1)
            ) {
                return@launch
            }

            if (isDownloading) {
                sendEvent(MainEvent.AlreadyDownloading)
                return@launch
            }

            repo.getLatestRelease(lisuOwner, lisuRepo)
                .onSuccess {
                    if (isNewVersion(it.version)) {
                        sendEvent(MainEvent.ShowUpdateDialog(it))
                    } else if (isUserPrompt) {
                        sendEvent(MainEvent.NoNewUpdates)
                    }
                }
                .onFailure {
                    if (isUserPrompt) {
                        sendEvent(MainEvent.CheckUpdateFailure(it))
                    }
                }
            PR.lastAppCheckTime.set(Instant.now().epochSecond)
        }
    }

    private fun isNewVersion(versionTag: String): Boolean {
        val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")
        return newVersion != BuildConfig.VERSION_NAME
    }

    fun downloadApk(dir: File?, downloadUrl: String) {
        if (isDownloading) return
        viewModelScope.launch {
            isDownloading = true
            val apkFile = File(dir, "update.apk")
            sendEvent(MainEvent.NotifyDownloadStart)

            var lastInstant = Instant.now()

            repo.downloadReleaseFile(
                url = downloadUrl,
                listener = { bytesSentTotal, contentLength ->
                    val progress = bytesSentTotal.toFloat() / contentLength
                    val currentInstant = Instant.now()
                    if (Duration.between(lastInstant, currentInstant) > Duration.ofMillis(200)) {
                        lastInstant = currentInstant
                        sendEventSync(MainEvent.NotifyDownloadProgress(progress))
                    }
                }
            )
                .mapCatching {
                    withContext(Dispatchers.IO) {
                        it.copyTo(apkFile.outputStream())
                    }
                }
                .onSuccess { sendEvent(MainEvent.NotifyDownloadFinish(apkFile)) }
                .onFailure { sendEvent(MainEvent.NotifyDownloadError(downloadUrl)) }
            isDownloading = false
        }
    }

    companion object {
        private const val lisuOwner = "FishHawk"
        private const val lisuRepo = "lisu-android"
    }
}