// presentation/viewmodels/MaterialViewModel.kt
package com.example.stroymaterials.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stroymaterials.data.database.entities.MaterialEntity
import com.example.stroymaterials.data.repositories.MaterialRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class MaterialViewModel(
    private val repository: MaterialRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Используем Flow напрямую, он будет автоматически обновляться при изменениях в БД
    val materials: StateFlow<List<MaterialEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllMaterials()
            } else {
                repository.searchMaterials(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadMaterials() {
        // Flow обновляется автоматически, просто сбрасываем поиск
        _searchQuery.value = ""
    }

    fun searchMaterials(query: String) {
        _searchQuery.value = query
    }

    suspend fun getMaterialById(id: Long): MaterialEntity? {
        return repository.getMaterialById(id)
    }

    suspend fun addMaterial(material: MaterialEntity): Long {
        // Список обновится автоматически через Flow при изменении БД
        return repository.insertMaterial(material)
    }

    suspend fun updateMaterial(material: MaterialEntity) {
        // Список обновится автоматически через Flow при изменении БД
        repository.updateMaterial(material.copy(updatedAt = Date()))
    }

    suspend fun deleteMaterial(material: MaterialEntity) {
        // Список обновится автоматически через Flow при изменении БД
        repository.deleteMaterial(material)
    }
}