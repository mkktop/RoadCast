package com.roadcast.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RouteStopDao {
    @Query("SELECT * FROM route_stops WHERE date = :date ORDER BY orderIndex ASC")
    fun getByDate(date: String): LiveData<List<RouteStop>>

    @Query("SELECT * FROM route_stops WHERE date = :date ORDER BY orderIndex ASC")
    suspend fun getByDateOnce(date: String): List<RouteStop>

    @Insert
    suspend fun insert(stop: RouteStop): Long

    @Insert
    suspend fun insertAll(stops: List<RouteStop>): List<Long>

    @Update
    suspend fun update(stop: RouteStop)

    @Update
    suspend fun updateAll(stops: List<RouteStop>)

    @Delete
    suspend fun delete(stop: RouteStop)

    @Query("DELETE FROM route_stops WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
