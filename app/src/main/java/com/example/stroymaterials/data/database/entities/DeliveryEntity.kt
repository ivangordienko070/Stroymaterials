// data/database/entities/DeliveryEntity.kt
package com.example.stroymaterials.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(
    tableName = "deliveries",
    foreignKeys = [
        ForeignKey(
            entity = MaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DeliveryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(index = true)
    val materialId: Long,

    @ColumnInfo(index = true)
    val supplierId: Long,

    val quantity: Double,
    val deliveryDate: Date,
    val expectedDate: Date,
    val status: String, // "delivered", "pending", "cancelled", "in_transit"
    val invoiceNumber: String,
    val totalCost: Double,
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)