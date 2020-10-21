package com.fishhawk.driftinglibraryandroid.repository.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ServerInfo(
    var name: String,
    var address: String,
    var position: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
