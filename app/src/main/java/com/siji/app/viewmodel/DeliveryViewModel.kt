package com.siji.app.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.siji.app.data.*
import kotlinx.coroutines.launch

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeliveryRepository
    val allDeliveries: LiveData<List<Delivery>>
    val pendingDeliveries: LiveData<List<Delivery>>
    val deliveringList: LiveData<List<Delivery>>
    val completedList: LiveData<List<Delivery>>

    init {
        val dao = DeliveryDatabase.getDatabase(application).deliveryDao()
        repository = DeliveryRepository(dao)
        allDeliveries = repository.getAll()
        pendingDeliveries = repository.getByStatus(DeliveryStatus.PENDING)
        deliveringList = repository.getByStatus(DeliveryStatus.DELIVERING)
        completedList = repository.getByStatus(DeliveryStatus.COMPLETED)
    }

    fun insert(delivery: Delivery) = viewModelScope.launch {
        repository.insert(delivery)
    }

    fun update(delivery: Delivery) = viewModelScope.launch {
        repository.update(delivery)
    }

    fun delete(delivery: Delivery) = viewModelScope.launch {
        repository.delete(delivery)
    }

    fun markAsDelivering(id: Long) = viewModelScope.launch {
        repository.markAsDelivering(id)
    }

    fun markAsCompleted(id: Long) = viewModelScope.launch {
        repository.markAsCompleted(id)
    }
}

class DeliveryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DeliveryViewModel(application) as T
    }
}
