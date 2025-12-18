// data/database/entities/MaterialEntity.kt
package com.example.stroymaterials.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(index = true)
    val name: String,

    val type: String, // "цемент", "песок", "кирпич", "арматура", "дерево", "металл", "краска"
    val unit: String, // "кг", "т", "шт", "л", "м³", "м²", "м"
    val quantity: Double,
    val price: Double, // цена за единицу
    val supplierId: Long,
    val lastDeliveryDate: Date? = null,
    val minStockLevel: Double = 0.0,
    val maxStockLevel: Double? = null,
    val warehouseLocation: String? = null,
    val imageUri: String? = null,
    val description: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isActive: Boolean = true
)