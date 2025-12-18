// data/repositories/MaterialRepository.kt
package com.example.stroymaterials.data.repositories

import com.example.stroymaterials.data.database.daos.MaterialDao
import com.example.stroymaterials.data.database.entities.MaterialEntity
import kotlinx.coroutines.flow.Flow

class MaterialRepository(
    private val materialDao: MaterialDao
) {
    fun getAllMaterials(): Flow<List<MaterialEntity>> = materialDao.getAllMaterials()

    suspend fun getMaterialById(id: Long): MaterialEntity? = materialDao.getMaterialById(id)

    suspend fun insertMaterial(material: MaterialEntity): Long = materialDao.insert(material)

    suspend fun updateMaterial(material: MaterialEntity) = materialDao.update(material)

    suspend fun deleteMaterial(material: MaterialEntity) = materialDao.delete(material)

    fun searchMaterials(query: String): Flow<List<MaterialEntity>> =
        materialDao.searchMaterials("%$query%")

    fun getLowStockMaterials(): Flow<List<MaterialEntity>> = materialDao.getLowStockMaterials()

    fun getMaterialsByType(type: String): Flow<List<MaterialEntity>> = materialDao.getMaterialsByType(type)

    fun getAllMaterialTypes(): Flow<List<String>> = materialDao.getAllMaterialTypes()

    suspend fun getMaterialsCount(): Int = materialDao.getMaterialsCount()

    suspend fun getTotalInventoryValue(): Double? = materialDao.getTotalInventoryValue()

    fun getActiveMaterials(): Flow<List<MaterialEntity>> = materialDao.getActiveMaterials()

    suspend fun updateMaterialQuantity(materialId: Long, amount: Double) =
        materialDao.updateMaterialQuantity(materialId, amount)

    suspend fun getMaterialStatistics(): MaterialStatistics {
        val totalMaterials = getMaterialsCount()
        val totalValue = getTotalInventoryValue() ?: 0.0
        // Для lowStockCount нам нужно будет дождаться Flow
        // Временно возвращаем 0
        val lowStockCount = 0

        return MaterialStatistics(
            totalMaterials = totalMaterials,
            totalInventoryValue = totalValue,
            lowStockCount = lowStockCount
        )
    }

    data class MaterialStatistics(
        val totalMaterials: Int = 0,
        val totalInventoryValue: Double = 0.0,
        val lowStockCount: Int = 0
    )
}