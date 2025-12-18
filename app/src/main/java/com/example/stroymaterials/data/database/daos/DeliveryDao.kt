// data/database/daos/DeliveryDao.kt
package com.example.stroymaterials.data.database.daos

import androidx.room.*
import com.example.stroymaterials.data.database.entities.DeliveryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DeliveryDao {
    @Insert
    suspend fun insert(delivery: DeliveryEntity): Long

    @Update
    suspend fun update(delivery: DeliveryEntity)

    @Delete
    suspend fun delete(delivery: DeliveryEntity)

    @Query("SELECT * FROM deliveries WHERE id = :id")
    suspend fun getDeliveryById(id: Long): DeliveryEntity?

    @Query("SELECT * FROM deliveries ORDER BY deliveryDate DESC")
    fun getAllDeliveries(): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE invoiceNumber LIKE :query OR status LIKE :query OR notes LIKE :query")
    fun searchDeliveries(query: String): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE materialId = :materialId ORDER BY deliveryDate DESC")
    fun getDeliveriesByMaterialId(materialId: Long): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE supplierId = :supplierId ORDER BY deliveryDate DESC")
    fun getDeliveriesBySupplierId(supplierId: Long): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE deliveryDate BETWEEN :startDate AND :endDate ORDER BY deliveryDate DESC")
    fun getDeliveriesByDateRange(startDate: Date, endDate: Date): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE status = 'pending' ORDER BY expectedDate ASC")
    fun getPendingDeliveries(): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE status = 'delivered' ORDER BY deliveryDate DESC")
    fun getDeliveredDeliveries(): Flow<List<DeliveryEntity>>

    @Query("SELECT SUM(totalCost) FROM deliveries WHERE status = 'delivered' AND deliveryDate BETWEEN :startDate AND :endDate")
    suspend fun getTotalCostForPeriod(startDate: Date, endDate: Date): Double?

    @Query("SELECT COUNT(*) FROM deliveries WHERE status = 'pending'")
    suspend fun getPendingDeliveriesCount(): Int

    @Query("SELECT COUNT(*) FROM deliveries WHERE status = 'delivered'")
    suspend fun getDeliveredDeliveriesCount(): Int

    @Query("SELECT COUNT(*) FROM deliveries")
    suspend fun getTotalDeliveriesCount(): Int

    @Query("SELECT * FROM deliveries WHERE status = :status ORDER BY expectedDate ASC")
    fun getDeliveriesByStatus(status: String): Flow<List<DeliveryEntity>>

    @Query("DELETE FROM deliveries")
    suspend fun deleteAll()
}