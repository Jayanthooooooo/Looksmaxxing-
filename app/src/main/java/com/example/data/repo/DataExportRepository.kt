package com.example.data.repo

import android.content.Context
import com.example.core.AppDispatchers
import com.example.core.Logger
import com.example.core.Outcome
import com.example.data.local.LumoraDatabase
import com.example.data.local.PhotoVault
import com.example.data.local.datastore.PreferencesDataStore
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DataExportRepository(
    private val context: Context,
    private val database: LumoraDatabase,
    private val photoVault: PhotoVault,
    private val preferencesDataStore: PreferencesDataStore,
    private val dispatchers: AppDispatchers
) {

    suspend fun generateExportZipFile(): Outcome<File> = withContext(dispatchers.io) {
        try {
            val exportDir = File(context.cacheDir, "lumora_export")
            if (exportDir.exists()) exportDir.deleteRecursively()
            exportDir.mkdirs()

            // 1. scans.json
            val scans = database.scanDao().getAllActiveScansSync()
            val scansJson = JSONArray()
            scans.forEach { scan ->
                val obj = JSONObject().apply {
                    put("id", scan.id)
                    put("createdAt", scan.createdAt)
                    put("kind", scan.kind)
                    put("engineVersion", scan.engineVersion)
                    put("qualityScore", scan.qualityScore)
                    put("indices", scan.indicesJson)
                }
                scansJson.put(obj)
            }
            File(exportDir, "scans.json").writeText(scansJson.toString(2))

            // 2. chats.json
            val threads = database.chatDao().getLastTenMessages("default_thread")
            val chatsJson = JSONArray()
            threads.forEach { msg ->
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("role", msg.role)
                    put("text", msg.text)
                    put("createdAt", msg.createdAt)
                }
                chatsJson.put(obj)
            }
            File(exportDir, "chats.json").writeText(chatsJson.toString(2))

            // 3. habits.csv
            val habits = database.habitDao().getAllHabits()
            val csvBuilder = StringBuilder("id,label,pillar,active\n")
            habits.forEach { h ->
                csvBuilder.append("${h.id},\"${h.label}\",${h.pillar},${h.active}\n")
            }
            File(exportDir, "habits.csv").writeText(csvBuilder.toString())

            // 4. Zip compilation
            val zipFile = File(context.cacheDir, "lumora_data_export.zip")
            if (zipFile.exists()) zipFile.delete()

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                exportDir.listFiles()?.forEach { file ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    file.inputStream().use { input -> input.copyTo(zos) }
                    zos.closeEntry()
                }
            }

            Outcome.Success(zipFile)
        } catch (e: Exception) {
            Logger.e("Failed to generate export archive", e)
            Outcome.Error(e.localizedMessage ?: "Failed to export data")
        }
    }

    suspend fun deleteAllAccountData(): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            photoVault.clearAllPhotos()
            database.scanDao().clearAll()
            database.habitDao().clearAllHabits()
            database.habitDao().clearAllLogs()
            database.chatDao().clearAllThreads()
            database.chatDao().clearAllMessages()
            preferencesDataStore.clearAll()
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to delete account data", e)
            Outcome.Error(e.localizedMessage ?: "Failed to delete account data")
        }
    }
}
