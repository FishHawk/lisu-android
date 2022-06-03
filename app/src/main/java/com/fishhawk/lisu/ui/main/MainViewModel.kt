package com.fishhawk.lisu.ui.main

import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.BuildConfig
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.get
import com.fishhawk.lisu.data.remote.GitHubRepository
import com.fishhawk.lisu.data.remote.model.GitHubReleaseDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import com.fishhawk.lisu.util.interceptor.ProgressInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.Instant

sealed interface MainEffect : Event {
    data class Message(val messageId: Int) : MainEffect
    data class StringMessage(val message: String) : MainEffect
    data class ShowUpdateDialog(val release: GitHubReleaseDto) : MainEffect
    object NotifyDownloadStart : MainEffect
    data class NotifyDownloadProgress(val progress: Float) : MainEffect
    data class NotifyDownloadFinish(val file: File) : MainEffect
    data class NotifyDownloadError(val url: String) : MainEffect
}

class MainViewModel(
    private val repo: GitHubRepository
) : BaseViewModel<MainEffect>() {
    fun checkForUpdate(isUserPrompt: Boolean = false) = viewModelScope.launch {
        if (isUserPrompt.not() &&
            Duration.between(
                Instant.ofEpochSecond(PR.lastAppCheckTime.get()),
                Instant.now()
            ) < Duration.ofDays(1)
        ) {
            return@launch
        }

        repo.getLatestRelease(lisuOwner, lisuRepo)
            .onSuccess {
                if (isNewVersion(it.version)) {
                    sendEvent(MainEffect.ShowUpdateDialog(it))
                } else if (isUserPrompt) {
                    sendEvent(MainEffect.Message(R.string.update_check_no_new_updates))
                }
            }
            .onFailure {
                val message = it.message
                if (isUserPrompt && message != null) {
                    sendEvent(MainEffect.StringMessage(message))
                }
            }
        PR.lastAppCheckTime.set(Instant.now().epochSecond)
    }

    private fun isNewVersion(versionTag: String): Boolean {
        val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")
        return newVersion != BuildConfig.VERSION_NAME
    }

    fun downloadApk(apkFile: File, downloadUrl: String) = viewModelScope.launch {
        sendEvent(MainEffect.NotifyDownloadStart)

        var lastInstant = Instant.now()
        ProgressInterceptor.addListener(downloadUrl) {
            val currentInstant = Instant.now()
            if (Duration.between(lastInstant, currentInstant) > Duration.ofMillis(200)) {
                lastInstant = currentInstant
                sendEventSync(MainEffect.NotifyDownloadProgress(it))
            }
        }

        repo.downloadReleaseFile(downloadUrl)
            .mapCatching {
                withContext(Dispatchers.IO) {
                    it.copyTo(apkFile.outputStream())
                }
            }
            .onSuccess { sendEvent(MainEffect.NotifyDownloadFinish(apkFile)) }
            .onFailure { sendEvent(MainEffect.NotifyDownloadError(downloadUrl)) }

        ProgressInterceptor.removeListener(downloadUrl)
    }

    companion object {
        private const val lisuOwner = "FishHawk"
        private const val lisuRepo = "lisu-android"
    }
}