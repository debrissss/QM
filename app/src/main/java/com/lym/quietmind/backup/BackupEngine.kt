package com.lym.quietmind.backup

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lym.quietmind.data.dao.FocusSessionDao
import com.lym.quietmind.data.entity.FocusSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class BackupEngine(private val dao: FocusSessionDao) {
    private val gson = Gson()

    suspend fun exportData(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val allHistory = dao.getAllHistoryRaw()
            val jsonString = gson.toJson(allHistory)
            
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

            val listType = object : TypeToken<List<FocusSessionEntity>>() {}.type
            val importedData: List<FocusSessionEntity> = gson.fromJson(jsonString, listType)

            // Validate the imported data structure roughly
            if (importedData == null) {
                return@withContext Result.failure(Exception("Cannot parse JSON into FocusSession data."))
            }

            // Perform truncation and insertion
            dao.clearAll()
            importedData.forEach { session ->
                dao.insertSession(session)
            }

            Result.success(importedData.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
