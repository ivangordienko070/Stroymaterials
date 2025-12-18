// data/backup/BackupManager.kt
package com.example.stroymaterials.data.backup

import android.content.Context
import android.os.Build
import android.os.Environment
import com.example.stroymaterials.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {
    suspend fun createBackup(context: Context): String = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context)
        val dbFile = context.getDatabasePath("construction_materials.db")
        
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val backupFileName = "stroymaterials_backup_${dateFormat.format(Date())}.db"
        
        val backupFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ используем внутреннее хранилище приложения
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), backupFileName)
        } else {
            // Для старых версий используем публичную папку Downloads
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, backupFileName)
        }

        FileInputStream(dbFile).use { input ->
            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
            }
        }

        backupFile.absolutePath
    }
}

