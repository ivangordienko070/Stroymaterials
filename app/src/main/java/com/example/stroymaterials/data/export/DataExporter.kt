// data/export/DataExporter.kt
package com.example.stroymaterials.data.export

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.stroymaterials.data.database.AppDatabase
import com.example.stroymaterials.data.repositories.DeliveryRepository
import com.example.stroymaterials.data.repositories.MaterialRepository
import com.example.stroymaterials.data.repositories.SupplierRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object DataExporter {
    suspend fun exportToCSV(context: Context): String = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context)
        val materialRepo = MaterialRepository(database.materialDao())
        val supplierRepo = SupplierRepository(database.supplierDao())
        val deliveryRepo = DeliveryRepository(database.deliveryDao())

        val materials = materialRepo.getAllMaterials().first()
        val suppliers = supplierRepo.getAllSuppliers().first()
        val deliveries = deliveryRepo.getAllDeliveries().first()

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "stroymaterials_export_${dateFormat.format(Date())}.csv"
        
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ используем внутреннее хранилище приложения
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        } else {
            // Для старых версий используем публичную папку Downloads
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, fileName)
        }

        FileWriter(file).use { writer ->
            // Заголовки
            writer.append("Тип,ID,Название,Тип материала,Единица измерения,Количество,Цена,Поставщик ID,Склад,Описание,Активен\n")
            
            // Материалы
            materials.forEach { material ->
                writer.append("Материал,")
                writer.append("${material.id},")
                writer.append("\"${material.name}\",")
                writer.append("\"${material.type}\",")
                writer.append("\"${material.unit}\",")
                writer.append("${material.quantity},")
                writer.append("${material.price},")
                writer.append("${material.supplierId},")
                writer.append("\"${material.warehouseLocation ?: ""}\",")
                writer.append("\"${material.description ?: ""}\",")
                writer.append("${if (material.isActive) "Да" else "Нет"}\n")
            }

            // Поставщики
            writer.append("\n")
            writer.append("Тип,ID,Название,Контактное лицо,Телефон,Email,Адрес,Город,Рейтинг,Срок доставки,Условия оплаты,Активен\n")
            suppliers.forEach { supplier ->
                writer.append("Поставщик,")
                writer.append("${supplier.id},")
                writer.append("\"${supplier.name}\",")
                writer.append("\"${supplier.contactPerson}\",")
                writer.append("\"${supplier.phone}\",")
                writer.append("\"${supplier.email ?: ""}\",")
                writer.append("\"${supplier.address}\",")
                writer.append("\"${supplier.city ?: ""}\",")
                writer.append("${supplier.rating},")
                writer.append("${supplier.deliveryTimeDays},")
                writer.append("\"${supplier.paymentTerms ?: ""}\",")
                writer.append("${if (supplier.isActive) "Да" else "Нет"}\n")
            }

            // Поставки
            val deliveryDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            writer.append("\n")
            writer.append("Тип,ID,Номер накладной,Материал ID,Поставщик ID,Количество,Статус,Дата доставки,Ожидаемая дата,Стоимость,Примечания\n")
            deliveries.forEach { delivery ->
                writer.append("Поставка,")
                writer.append("${delivery.id},")
                writer.append("\"${delivery.invoiceNumber}\",")
                writer.append("${delivery.materialId},")
                writer.append("${delivery.supplierId},")
                writer.append("${delivery.quantity},")
                writer.append("\"${delivery.status}\",")
                writer.append("\"${deliveryDateFormat.format(delivery.deliveryDate)}\",")
                writer.append("\"${deliveryDateFormat.format(delivery.expectedDate)}\",")
                writer.append("${delivery.totalCost},")
                writer.append("\"${delivery.notes ?: ""}\"\n")
            }
        }

        file.absolutePath
    }
}

