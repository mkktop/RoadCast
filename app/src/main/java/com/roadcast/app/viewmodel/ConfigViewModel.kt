package com.roadcast.app.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.roadcast.app.data.*
import kotlinx.coroutines.launch

class ConfigViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val areaRepository = DeliveryAreaRepository(db.deliveryAreaDao())
    private val supermarketRepository = SupermarketRepository(db.supermarketDao())

    val allAreas: LiveData<List<DeliveryArea>> = areaRepository.getAll()
    val allSupermarkets: LiveData<List<Supermarket>> = supermarketRepository.getAll()

    fun addArea(name: String) = viewModelScope.launch {
        areaRepository.insert(DeliveryArea(name = name))
    }

    fun updateArea(area: DeliveryArea) = viewModelScope.launch {
        areaRepository.update(area)
    }

    fun deleteArea(area: DeliveryArea) = viewModelScope.launch {
        areaRepository.delete(area)
    }

    fun addSupermarket(
        name: String,
        areaId: Long,
        contactPerson: String?,
        phone: String?,
        address: String?,
        remark: String?
    ) = viewModelScope.launch {
        supermarketRepository.insert(
            Supermarket(
                name = name,
                areaId = areaId,
                contactPerson = contactPerson,
                phone = phone,
                address = address,
                remark = remark
            )
        )
    }

    fun updateSupermarket(supermarket: Supermarket) = viewModelScope.launch {
        supermarketRepository.update(supermarket)
    }

    fun deleteSupermarket(supermarket: Supermarket) = viewModelScope.launch {
        supermarketRepository.delete(supermarket)
    }
}

class ConfigViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ConfigViewModel(application) as T
    }
}
