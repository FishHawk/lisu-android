package com.fishhawk.driftinglibraryandroid.data.database

import com.fishhawk.driftinglibraryandroid.data.database.dao.ServerInfoDao
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo

class ServerInfoRepository(private val dao: ServerInfoDao) {

    fun list() = dao.list()

    fun select(id: Int) = dao.select(id)

    suspend fun insert(serverInfo: ServerInfo) = dao.insert(serverInfo)

    suspend fun update(serverInfo: ServerInfo) = dao.update(serverInfo)

    suspend fun delete(serverInfo: ServerInfo) = dao.delete(serverInfo)
}