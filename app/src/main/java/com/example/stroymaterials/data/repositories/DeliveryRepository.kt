// data/repositories/DeliveryRepository.kt
package com.example.stroymaterials.data.repositories

import com.example.stroymaterials.data.database.daos.DeliveryDao
import com.example.stroymaterials.data.database.entities.DeliveryEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

class DeliveryRepository(
    private val deliveryDao: DeliveryDao
) {
    fun getAllDeliveries(): Flow<List<DeliveryEntity>> = deliveryDao.getAllDeliveries()

    suspend fun getDeliveryById(id: Long): DeliveryEntity? = deliveryDao.getDeliveryById(id)

    suspend fun insertDelivery(delivery: DeliveryEntity): Long = deliveryDao.insert(delivery)

    suspend fun updateDelivery(delivery: DeliveryEntity) = deliveryDao.update(delivery)

    suspend fun deleteDelivery(delivery: DeliveryEntity) = deliveryDao.delete(delivery)

    fun searchDeliveries(query: String): Flow<List<DeliveryEntity>> =
        deliveryDao.searchDeliveries("%$query%")

    fun getDeliveriesByMaterialId(materialId: Long): Flow<List<DeliveryEntity>> =
        deliveryDao.getDeliveriesByMaterialId(materialId)

    fun getDeliveriesBySupplierId(supplierId: Long): Flow<List<DeliveryEntity>> =
        deliveryDao.getDeliveriesBySupplierId(supplierId)

    fun getDeliveriesByDateRange(startDate: Date, endDate: Date): Flow<List<DeliveryEntity>> =
        deliveryDao.getDeliveriesByDateRange(startDate, endDate)

    fun getPendingDeliveries(): Flow<List<DeliveryEntity>> = deliveryDao.getPendingDeliveries()

    fun getDeliveredDeliveries(): Flow<List<DeliveryEntity>> = deliveryDao.getDeliveredDeliveries()

    suspend fun getTotalCostForPeriod(startDate: Date, endDate: Date): Double? =
        deliveryDao.getTotalCostForPeriod(startDate, endDate)

    suspend fun getPendingDeliveriesCount(): Int = deliveryDao.getPendingDeliveriesCount()

    suspend fun getDeliveredDeliveriesCount(): Int = deliveryDao.getDeliveredDeliveriesCount()

    suspend fun getTotalDeliveriesCount(): Int = deliveryDao.getTotalDeliveriesCount()

    fun getDeliveriesByStatus(status: String): Flow<List<DeliveryEntity>> =
        deliveryDao.getDeliveriesByStatus(status)

    suspend fun getDeliveryStatistics(): DeliveryStatistics {
        val calendar = Calendar.getInstance()
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfMonth = Calendar.getInstance().time

        val pendingCount = getPendingDeliveriesCount()
        val deliveredCount = getDeliveredDeliveriesCount()
        val totalCount = getTotalDeliveriesCount()
        val totalCostThisMonth = getTotalCostForPeriod(startOfMonth, endOfMonth) ?: 0.0

        return DeliveryStatistics(
            pendingCount = pendingCount,
            deliveredCount = deliveredCount,
            totalCount = totalCount,
            totalCostThisMonth = totalCostThisMonth
        )
    }

    data class DeliveryStatistics(
        val pendingCount: Int = 0,
        val deliveredCount: Int = 0,
        val totalCount: Int = 0,
        val totalCostThisMonth: Double = 0.0
    )
}