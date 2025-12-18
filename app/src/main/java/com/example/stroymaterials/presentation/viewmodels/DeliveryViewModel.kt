// presentation/viewmodels/DeliveryViewModel.kt
package com.example.stroymaterials.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stroymaterials.data.database.entities.DeliveryEntity
import com.example.stroymaterials.data.repositories.DeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class DeliveryViewModel(
    private val repository: DeliveryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Используем Flow напрямую, он будет автоматически обновляться при изменениях в БД
    val deliveries: StateFlow<List<DeliveryEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllDeliveries()
            } else {
                repository.searchDeliveries(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _statistics = MutableStateFlow(DeliveryStatistics())
    val statistics: StateFlow<DeliveryStatistics> = _statistics.asStateFlow()

    init {
        // Обновляем статистику при изменении списка поставок
        viewModelScope.launch {
            deliveries.collect {
                updateStatistics()
            }
        }
    }

    fun loadDeliveries() {
        // Flow обновляется автоматически, просто сбрасываем поиск
        _searchQuery.value = ""
    }

    fun searchDeliveries(query: String) {
        _searchQuery.value = query
    }

    fun filterByStatus(status: String?) {
        viewModelScope.launch {
            if (status == null) {
                _searchQuery.value = ""
            } else {
                repository.getDeliveriesByStatus(status).collect { deliveriesList ->
                    // Можно добавить отдельный StateFlow для фильтрации по статусу
                }
            }
        }
    }

    suspend fun getDeliveryById(id: Long): DeliveryEntity? {
        return repository.getDeliveryById(id)
    }

    suspend fun addDelivery(delivery: DeliveryEntity): Long {
        // Список обновится автоматически через Flow при изменении БД
        return repository.insertDelivery(delivery)
    }

    suspend fun updateDelivery(delivery: DeliveryEntity) {
        // Список обновится автоматически через Flow при изменении БД
        repository.updateDelivery(delivery)
    }

    suspend fun updateDeliveryStatus(deliveryId: Long, newStatus: String) {
        // Реализуйте обновление статуса
    }

    suspend fun deleteDelivery(delivery: DeliveryEntity) {
        // Список обновится автоматически через Flow при изменении БД
        repository.deleteDelivery(delivery)
    }

    private suspend fun updateStatistics() {
        val stats = repository.getDeliveryStatistics()
        _statistics.value = DeliveryStatistics(
            pendingDeliveriesCount = stats.pendingCount,
            deliveredDeliveriesCount = stats.deliveredCount,
            totalDeliveriesCount = stats.totalCount,
            totalCostThisMonth = stats.totalCostThisMonth
        )
    }

    data class DeliveryStatistics(
        val pendingDeliveriesCount: Int = 0,
        val deliveredDeliveriesCount: Int = 0,
        val totalDeliveriesCount: Int = 0,
        val totalCostThisMonth: Double = 0.0
    )
}
