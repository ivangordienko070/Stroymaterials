// data/database/daos/MaterialDao.kt
package com.example.stroymaterials.data.database.daos

import androidx.room.*
import com.example.stroymaterials.data.database.entities.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Insert
    suspend fun insert(material: MaterialEntity): Long

    @Update
    suspend fun update(material: MaterialEntity)

    @Delete
    suspend fun delete(material: MaterialEntity)

    @Query("SELECT * FROM materials WHERE id = :id")
    suspend fun getMaterialById(id: Long): MaterialEntity?

    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE name LIKE :query OR type LIKE :query OR description LIKE :query")
    fun searchMaterials(query: String): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE quantity <= minStockLevel")
    fun getLowStockMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE type = :type ORDER BY name ASC")
    fun getMaterialsByType(type: String): Flow<List<MaterialEntity>>

    @Query("SELECT DISTINCT type FROM materials WHERE type IS NOT NULL AND type != ''")
    fun getAllMaterialTypes(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM materials")
    suspend fun getMaterialsCount(): Int

    @Query("SELECT SUM(quantity * price) FROM materials")
    suspend fun getTotalInventoryValue(): Double?

    @Query("SELECT * FROM materials WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMaterials(): Flow<List<MaterialEntity>>

    @Query("UPDATE materials SET quantity = quantity + :amount WHERE id = :materialId")
    suspend fun updateMaterialQuantity(materialId: Long, amount: Double)

    @Query("DELETE FROM materials")
    suspend fun deleteAll()
}