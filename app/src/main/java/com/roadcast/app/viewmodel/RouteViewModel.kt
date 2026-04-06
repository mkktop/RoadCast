package com.roadcast.app.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.roadcast.app.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RouteViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val stopRepository = RouteStopRepository(db.routeStopDao())
    private val supermarketRepository = SupermarketRepository(db.supermarketDao())
    private val areaRepository = DeliveryAreaRepository(db.deliveryAreaDao())

    private val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayStops: LiveData<List<RouteStop>> = stopRepository.getByDate(todayDate)
    val allSupermarkets: LiveData<List<Supermarket>> = supermarketRepository.getAll()
    val allAreas: LiveData<List<DeliveryArea>> = areaRepository.getAll()

    fun addStop(supermarketId: Long) = viewModelScope.launch {
        val currentStops = stopRepository.getByDateOnce(todayDate)
        val maxIndex = currentStops.maxOfOrNull { it.orderIndex } ?: -1
        stopRepository.insert(
            RouteStop(
                date = todayDate,
                supermarketId = supermarketId,
                orderIndex = maxIndex + 1
            )
        )
    }

    fun addStops(supermarketIds: List<Long>) = viewModelScope.launch {
        val currentStops = stopRepository.getByDateOnce(todayDate)
        var maxIndex = currentStops.maxOfOrNull { it.orderIndex } ?: -1
        val newStops = supermarketIds.map { supermarketId ->
            maxIndex++
            RouteStop(
                date = todayDate,
                supermarketId = supermarketId,
                orderIndex = maxIndex
            )
        }
        if (newStops.isNotEmpty()) {
            stopRepository.insertAll(newStops)
        }
    }

    fun removeStop(stop: RouteStop) = viewModelScope.launch {
        stopRepository.delete(stop)
    }

    fun moveUp(stop: RouteStop) = viewModelScope.launch {
        val stops = stopRepository.getByDateOnce(todayDate).sortedBy { it.orderIndex }
        val currentIndex = stops.indexOfFirst { it.id == stop.id }
        if (currentIndex > 0) {
            val prev = stops[currentIndex - 1]
            stopRepository.updateAll(
                listOf(
                    stop.copy(orderIndex = prev.orderIndex),
                    prev.copy(orderIndex = stop.orderIndex)
                )
            )
        }
    }

    fun moveDown(stop: RouteStop) = viewModelScope.launch {
        val stops = stopRepository.getByDateOnce(todayDate).sortedBy { it.orderIndex }
        val currentIndex = stops.indexOfFirst { it.id == stop.id }
        if (currentIndex < stops.size - 1) {
            val next = stops[currentIndex + 1]
            stopRepository.updateAll(
                listOf(
                    stop.copy(orderIndex = next.orderIndex),
                    next.copy(orderIndex = stop.orderIndex)
                )
            )
        }
    }

    fun markCompleted(stop: RouteStop) = viewModelScope.launch {
        stopRepository.update(
            stop.copy(status = StopStatus.COMPLETED, completedAt = System.currentTimeMillis())
        )
    }

    fun markSkipped(stop: RouteStop) = viewModelScope.launch {
        stopRepository.update(stop.copy(status = StopStatus.SKIPPED))
    }

    fun markAsPending(stop: RouteStop) = viewModelScope.launch {
        stopRepository.update(stop.copy(status = StopStatus.PENDING, completedAt = null))
    }

    fun clearTodayRoute() = viewModelScope.launch {
        stopRepository.deleteByDate(todayDate)
    }

    fun updateStopsOrder(reorderedStops: List<RouteStop>) = viewModelScope.launch {
        stopRepository.updateAll(reorderedStops)
    }

    fun updateDeliveryItems(stop: RouteStop, items: String?) = viewModelScope.launch {
        val trimmed = items?.trim()
        stopRepository.update(stop.copy(deliveryItems = if (trimmed.isNullOrEmpty()) null else trimmed))
    }
}

class RouteViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RouteViewModel(application) as T
    }
}
