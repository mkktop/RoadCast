package com.roadcast.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SupermarketDao {
    @Query("SELECT * FROM supermarkets WHERE areaId = :areaId ORDER BY name ASC")
    fun getByAreaId(areaId: Long): LiveData<List<Supermarket>>

    @Query("SELECT * FROM supermarkets ORDER BY name ASC")
    fun getAll(): LiveData<List<Supermarket>>

    @Query("SELECT * FROM supermarkets ORDER BY name ASC")
    suspend fun getAllOnce(): List<Supermarket>

    @Query("SELECT * FROM supermarkets WHERE id = :id")
    suspend fun getById(id: Long): Supermarket?

    @Insert
    suspend fun insert(supermarket: Supermarket): Long

    @Update
    suspend fun update(supermarket: Supermarket)

    @Delete
    suspend fun delete(supermarket: Supermarket)
}
