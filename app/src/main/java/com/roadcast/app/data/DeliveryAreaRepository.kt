package com.roadcast.app.data

import androidx.lifecycle.LiveData

class DeliveryAreaRepository(private val dao: DeliveryAreaDao) {
    fun getAll(): LiveData<List<DeliveryArea>> = dao.getAll()
    suspend fun getById(id: Long): DeliveryArea? = dao.getById(id)
    suspend fun insert(area: DeliveryArea): Long = dao.insert(area)
    suspend fun update(area: DeliveryArea) = dao.update(area)
    suspend fun delete(area: DeliveryArea) = dao.delete(area)
}
