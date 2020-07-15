package com.fishhawk.driftinglibraryandroid.base

abstract class Notification

data class NetworkErrorNotification(val throwable: Throwable) : Notification()
class ListEmptyNotification : Notification()
class ListReachEndNotification : Notification()
class DownloadCreatedNotification : Notification()
class SubscriptionCreatedNotification : Notification()
