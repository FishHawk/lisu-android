package com.fishhawk.lisu.data.database

import com.fishhawk.lisu.data.database.dao.ServerHistoryDao
import com.fishhawk.lisu.data.database.model.ServerHistory

class ServerHistoryRepository(private val dao: ServerHistoryDao) {

    fun list() = dao.list()

    suspend fun update(history: ServerHistory) = dao.insert(history)

    suspend fun deleteByAddress(address: String) = dao.deleteByAddress(address)

    suspend fun clear() = dao.clear()
}