// presentation/viewmodels/SupplierViewModel.kt
package com.example.stroymaterials.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stroymaterials.data.repositories.SupplierRepository
import com.example.stroymaterials.data.database.entities.SupplierEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class SupplierViewModel(
    private val repository: SupplierRepository
) : ViewModel() {

    // Состояние для списка поставщиков
    private val _suppliers = MutableStateFlow<List<SupplierEntity>>(emptyList())
    val suppliers: StateFlow<List<SupplierEntity>> = _suppliers.asStateFlow()

    // Состояние для выбранного поставщика
    private val _selectedSupplier = MutableStateFlow<SupplierEntity?>(null)
    val selectedSupplier: StateFlow<SupplierEntity?> = _selectedSupplier.asStateFlow()

    // Состояние для поиска
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Состояние ошибки
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSuppliers()
    }

    fun loadSuppliers() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getAllSuppliers().collect { suppliersList ->
                    _suppliers.value = suppliersList.sortedBy { it.name }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки поставщиков: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun searchSuppliers(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadSuppliers()
            } else {
                _isLoading.value = true
                try {
                    repository.searchSuppliers(query).collect { suppliersList ->
                        _suppliers.value = suppliersList
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    _error.value = "Ошибка поиска поставщиков: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    suspend fun getSupplierById(id: Long): SupplierEntity? {
        return try {
            repository.getSupplierById(id)
        } catch (e: Exception) {
            _error.value = "Ошибка получения поставщика: ${e.message}"
            null
        }
    }

    suspend fun addSupplier(supplier: SupplierEntity): Long {
        return try {
            val id = repository.insertSupplier(supplier)
            loadSuppliers()
            id
        } catch (e: Exception) {
            _error.value = "Ошибка добавления поставщика: ${e.message}"
            -1L
        }
    }

    suspend fun updateSupplier(supplier: SupplierEntity) {
        try {
            repository.updateSupplier(supplier)
            loadSuppliers()
        } catch (e: Exception) {
            _error.value = "Ошибка обновления поставщика: ${e.message}"
        }
    }

    suspend fun deleteSupplier(supplier: SupplierEntity) {
        try {
            repository.deleteSupplier(supplier)
            loadSuppliers()
        } catch (e: Exception) {
            _error.value = "Ошибка удаления поставщика: ${e.message}"
        }
    }

    fun getTopSuppliers(limit: Int = 5): Flow<List<SupplierEntity>> {
        return repository.getTopSuppliers(limit)
    }

    fun toggleSupplierStatus(supplier: SupplierEntity) {
        viewModelScope.launch {
            try {
                repository.updateSupplier(supplier.copy(
                    isActive = !supplier.isActive,
                    updatedAt = Date()
                ))
                loadSuppliers()
            } catch (e: Exception) {
                _error.value = "Ошибка изменения статуса поставщика: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSelectedSupplier() {
        _selectedSupplier.value = null
    }

    fun selectSupplier(supplier: SupplierEntity) {
        _selectedSupplier.value = supplier
    }
}