package com.roadcast.app.data

import androidx.lifecycle.LiveData

class SupermarketRepository(private val dao: SupermarketDao) {
    fun getByAreaId(areaId: Long): LiveData<List<Supermarket>> = dao.getByAreaId(areaId)
    fun getAll(): LiveData<List<Supermarket>> = dao.getAll()
    suspend fun getAllOnce(): List<Supermarket> = dao.getAllOnce()
    suspend fun getById(id: Long): Supermarket? = dao.getById(id)
    suspend fun insert(supermarket: Supermarket): Long = dao.insert(supermarket)
    suspend fun update(supermarket: Supermarket) = dao.update(supermarket)
    suspend fun delete(supermarket: Supermarket) = dao.delete(supermarket)
}
