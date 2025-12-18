// data/database/entities/SupplierEntity.kt
package com.example.stroymaterials.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(index = true)
    val name: String,

    val contactPerson: String,
    val phone: String,
    val email: String? = null,
    val address: String,
    val city: String? = null,
    val rating: Int = 5, // от 1 до 5
    val deliveryTimeDays: Int = 7, // среднее время доставки в днях
    val paymentTerms: String? = null, // условия оплаты
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)