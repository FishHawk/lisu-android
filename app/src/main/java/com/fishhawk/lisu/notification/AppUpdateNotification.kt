package com.fishhawk.lisu.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fishhawk.lisu.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object AppUpdateNotification {
    internal const val channel = "app_apk_update_channel"
    private const val id = 1

    private fun Context.notificationBuilder() =
        NotificationCompat.Builder(this, channel)

    private fun NotificationCompat.Builder.show(context: Context) =
        with(NotificationManagerCompat.from(context)) { notify(id, build()) }

    fun onDownloadStart(context: Context) = with(context) {
        notificationBuilder()
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_app_update_in_progress))
            .setOngoing(true)
            .show(this)
    }

    fun onProgressChange(context: Context, progress: Float) = with(context) {
        notificationBuilder()
            .setSmallIcon(R.mipmap.ic_launcher)
            .setProgress(100, (progress * 100).toInt(), false)
            .setOnlyAlertOnce(true)
            .show(this)
    }

    fun onDownloadFinished(context: Context, uri: Uri) = with(context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val installIntent = PendingIntent.getActivity(this, 0, intent, 0)

        notificationBuilder()
            .setContentText(context.getString(R.string.notification_app_update_complete))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setOnlyAlertOnce(false)
            .setProgress(0, 0, false)
            .setContentIntent(installIntent)
            .clearActions()
            .addAction(
                R.drawable.ic_baseline_system_update_alt_24,
                context.getString(R.string.notification_app_update_action_install),
                installIntent
            )
            .addAction(
                R.drawable.ic_baseline_close_24,
                context.getString(R.string.notification_app_update_action_cancel),
                NotificationReceiver.dismissNotificationBroadcast(context, id)
            )
            .show(this)
    }

    fun onDownloadError(context: Context, url: String) = with(context) {
        notificationBuilder()
            .setContentText(context.getString(R.string.notification_app_update_error))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(false)
            .setProgress(0, 0, false)
            .clearActions()
            .addAction(
                R.drawable.ic_baseline_refresh_24,
                context.getString(R.string.notification_app_update_action_retry),
                NotificationReceiver.retryAppUpdateBroadcast(context, url)
            )
            .addAction(
                R.drawable.ic_baseline_close_24,
                context.getString(R.string.notification_app_update_action_cancel),
                NotificationReceiver.dismissNotificationBroadcast(context, id)
            )
            .show(this)
    }

    internal val retryChannel = Channel<String>()
    val retryFlow = retryChannel.receiveAsFlow()
}