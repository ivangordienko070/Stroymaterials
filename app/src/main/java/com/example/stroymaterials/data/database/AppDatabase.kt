// data/database/AppDatabase.kt
package com.example.stroymaterials.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stroymaterials.data.database.daos.DeliveryDao
import com.example.stroymaterials.data.database.daos.MaterialDao
import com.example.stroymaterials.data.database.daos.SupplierDao
import com.example.stroymaterials.data.database.daos.UserDao
import com.example.stroymaterials.data.database.entities.DeliveryEntity
import com.example.stroymaterials.data.database.entities.MaterialEntity
import com.example.stroymaterials.data.database.entities.SupplierEntity
import com.example.stroymaterials.data.database.entities.UserEntity

@Database(
    entities = [MaterialEntity::class, SupplierEntity::class, DeliveryEntity::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao
    abstract fun supplierDao(): SupplierDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "construction_materials.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}