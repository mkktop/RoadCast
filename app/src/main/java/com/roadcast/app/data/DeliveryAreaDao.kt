package com.roadcast.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DeliveryAreaDao {
    @Query("SELECT * FROM delivery_areas ORDER BY createdAt ASC")
    fun getAll(): LiveData<List<DeliveryArea>>

    @Query("SELECT * FROM delivery_areas WHERE id = :id")
    suspend fun getById(id: Long): DeliveryArea?

    @Insert
    suspend fun insert(area: DeliveryArea): Long

    @Update
    suspend fun update(area: DeliveryArea)

    @Delete
    suspend fun delete(area: DeliveryArea)
}
