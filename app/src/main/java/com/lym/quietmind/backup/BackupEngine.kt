package com.lym.quietmind.backup

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lym.quietmind.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class BackupEngine(private val db: AppDatabase) {
    private val focusDao = db.focusSessionDao()
    private val entertainDao = db.entertainmentDao()
    private val gson = Gson()

    suspend fun exportData(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val focusHistory = focusDao.getAllHistoryRaw()
            val entertainHistory = entertainDao.getAllHistoryRaw()
            
            val backupData = BackupData(
                focusSessions = focusHistory,
                entertainmentRecords = entertainHistory
            )
            
            val jsonString = gson.toJson(backupData)
            
            outputStream.bufferedWriter().use { writer ->
                writer.write(jsonString)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(inputStream: InputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            // Check if JSON is empty or blank
            if (jsonString.isBlank()) {
                return@withContext Result.failure(Exception("Backup file is empty."))
            }

            val importedData: BackupData = gson.fromJson(jsonString, BackupData::class.java)

            // Validate the imported data structure roughly
            if (importedData == null || importedData.focusSessions == null || importedData.entertainmentRecords == null) {
                return@withContext Result.failure(Exception("Cannot parse JSON into complete BackupData structure. Old schema is unsupported."))
            }

            // Perform truncation and insertion
            db.runInTransaction {
                kotlinx.coroutines.runBlocking {
                    focusDao.clearAll()
                    entertainDao.clearAll()
                    
                    importedData.focusSessions.forEach { session ->
                        focusDao.insertSession(session)
                    }
                    
                    importedData.entertainmentRecords.forEach { record ->
                        entertainDao.insertRecord(record)
                    }
                }
            }

            Result.success(importedData.focusSessions.size + importedData.entertainmentRecords.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
