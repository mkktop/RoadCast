package com.roadcast.app.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.roadcast.app.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val stopRepository = RouteStopRepository(db.routeStopDao())
    private val supermarketRepository = SupermarketRepository(db.supermarketDao())
    private val areaRepository = DeliveryAreaRepository(db.deliveryAreaDao())

    private val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayStops: LiveData<List<RouteStop>> = stopRepository.getByDate(todayDate)
    val allSupermarkets: LiveData<List<Supermarket>> = supermarketRepository.getAll()
    val allAreas: LiveData<List<DeliveryArea>> = areaRepository.getAll()

    fun markCompleted(stop: RouteStop) = viewModelScope.launch {
        stopRepository.update(
            stop.copy(status = StopStatus.COMPLETED, completedAt = System.currentTimeMillis())
        )
    }

    fun markSkipped(stop: RouteStop) = viewModelScope.launch {
        stopRepository.update(stop.copy(status = StopStatus.SKIPPED))
    }
}

class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(application) as T
    }
}
