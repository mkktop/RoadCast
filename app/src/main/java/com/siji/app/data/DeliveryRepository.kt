package com.siji.app.data

import androidx.lifecycle.LiveData

class DeliveryRepository(private val dao: DeliveryDao) {

    fun getAll(): LiveData<List<Delivery>> = dao.getAll()

    fun getByStatus(status: DeliveryStatus): LiveData<List<Delivery>> = dao.getByStatus(status)

    suspend fun insert(delivery: Delivery): Long = dao.insert(delivery)

    suspend fun update(delivery: Delivery) = dao.update(delivery)

    suspend fun delete(delivery: Delivery) = dao.delete(delivery)

    suspend fun markAsDelivering(id: Long) {
        dao.getById(id)?.let {
            dao.update(it.copy(status = DeliveryStatus.DELIVERING))
        }
    }

    suspend fun markAsCompleted(id: Long) {
        dao.getById(id)?.let {
            dao.update(it.copy(
                status = DeliveryStatus.COMPLETED,
                completedAt = System.currentTimeMillis()
            ))
        }
    }
}
