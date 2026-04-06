package com.roadcast.app.data

import androidx.lifecycle.LiveData

class RouteStopRepository(private val dao: RouteStopDao) {
    fun getByDate(date: String): LiveData<List<RouteStop>> = dao.getByDate(date)
    suspend fun getByDateOnce(date: String): List<RouteStop> = dao.getByDateOnce(date)
    suspend fun insert(stop: RouteStop): Long = dao.insert(stop)
    suspend fun insertAll(stops: List<RouteStop>): List<Long> = dao.insertAll(stops)
    suspend fun update(stop: RouteStop) = dao.update(stop)
    suspend fun updateAll(stops: List<RouteStop>) = dao.updateAll(stops)
    suspend fun delete(stop: RouteStop) = dao.delete(stop)
    suspend fun deleteByDate(date: String) = dao.deleteByDate(date)
}
