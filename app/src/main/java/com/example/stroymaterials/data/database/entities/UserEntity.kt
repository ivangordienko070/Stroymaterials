// data/database/entities/UserEntity.kt
package com.example.stroymaterials.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val username: String,

    val password: String, // В реальном приложении должен быть хеширован
    val role: String, // "admin" или "guest"
    val createdAt: Date = Date(),
    val isActive: Boolean = true
)


