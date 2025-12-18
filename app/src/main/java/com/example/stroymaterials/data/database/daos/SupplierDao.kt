// data/database/daos/SupplierDao.kt
package com.example.stroymaterials.data.database.daos

import androidx.room.*
import com.example.stroymaterials.data.database.entities.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert
    suspend fun insert(supplier: SupplierEntity): Long

    @Update
    suspend fun update(supplier: SupplierEntity)

    @Delete
    suspend fun delete(supplier: SupplierEntity)

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): SupplierEntity?

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE name LIKE :query OR contactPerson LIKE :query OR phone LIKE :query OR email LIKE :query")
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY rating DESC LIMIT :limit")
    fun getTopSuppliers(limit: Int): Flow<List<SupplierEntity>>

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getSuppliersCount(): Int

    @Query("SELECT COUNT(*) FROM suppliers WHERE isActive = 1")
    suspend fun getActiveSuppliersCount(): Int

    @Query("UPDATE suppliers SET isActive = :isActive WHERE id = :id")
    suspend fun updateSupplierStatus(id: Long, isActive: Boolean)

    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveSuppliers(): Flow<List<SupplierEntity>>

    @Query("DELETE FROM suppliers")
    suspend fun deleteAll()
}