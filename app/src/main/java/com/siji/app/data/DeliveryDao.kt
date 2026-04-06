package com.siji.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries ORDER BY deliveryTime ASC")
    fun getAll(): LiveData<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE status = :status ORDER BY deliveryTime ASC")
    fun getByStatus(status: DeliveryStatus): LiveData<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE id = :id")
    suspend fun getById(id: Long): Delivery?

    @Insert
    suspend fun insert(delivery: Delivery): Long

    @Update
    suspend fun update(delivery: Delivery)

    @Delete
    suspend fun delete(delivery: Delivery)

    @Query("DELETE FROM deliveries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
