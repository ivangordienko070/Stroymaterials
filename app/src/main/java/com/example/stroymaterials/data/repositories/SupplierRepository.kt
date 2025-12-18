// data/repositories/SupplierRepository.kt
package com.example.stroymaterials.data.repositories

import com.example.stroymaterials.data.database.daos.SupplierDao
import com.example.stroymaterials.data.database.entities.SupplierEntity
import kotlinx.coroutines.flow.Flow

class SupplierRepository(
    private val supplierDao: SupplierDao
) {
    fun getAllSuppliers(): Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()

    suspend fun getSupplierById(id: Long): SupplierEntity? = supplierDao.getSupplierById(id)

    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insert(supplier)

    suspend fun updateSupplier(supplier: SupplierEntity) = supplierDao.update(supplier)

    suspend fun deleteSupplier(supplier: SupplierEntity) = supplierDao.delete(supplier)

    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> =
        supplierDao.searchSuppliers("%$query%")

    fun getTopSuppliers(limit: Int = 5): Flow<List<SupplierEntity>> = supplierDao.getTopSuppliers(limit)

    suspend fun getSuppliersCount(): Int = supplierDao.getSuppliersCount()

    suspend fun getActiveSuppliersCount(): Int = supplierDao.getActiveSuppliersCount()

    suspend fun updateSupplierStatus(id: Long, isActive: Boolean) =
        supplierDao.updateSupplierStatus(id, isActive)

    fun getActiveSuppliers(): Flow<List<SupplierEntity>> = supplierDao.getActiveSuppliers()

    suspend fun getSupplierStatistics(): SupplierStatistics {
        val totalSuppliers = getSuppliersCount()
        val activeSuppliers = getActiveSuppliersCount()

        return SupplierStatistics(
            totalSuppliers = totalSuppliers,
            activeSuppliers = activeSuppliers,
            inactiveSuppliers = totalSuppliers - activeSuppliers
        )
    }

    data class SupplierStatistics(
        val totalSuppliers: Int = 0,
        val activeSuppliers: Int = 0,
        val inactiveSuppliers: Int = 0
    )
}