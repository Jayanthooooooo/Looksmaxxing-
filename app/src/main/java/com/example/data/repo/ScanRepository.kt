package com.example.data.repo

import android.graphics.Bitmap
import com.example.core.AppDispatchers
import com.example.core.Logger
import com.example.core.Outcome
import com.example.data.local.PhotoVault
import com.example.data.local.dao.MetricNoiseDao
import com.example.data.local.dao.ScanDao
import com.example.data.local.entity.MetricNoiseEntity
import com.example.data.local.entity.ScanEntity
import com.example.domain.model.ChangeabilityTag
import com.example.domain.model.MetricValue
import com.example.domain.model.QualityReport
import com.example.domain.model.RangePosition
import com.example.domain.model.ScanResult
import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SkinDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ScanRepository(
    private val scanDao: ScanDao,
    private val metricNoiseDao: MetricNoiseDao,
    private val photoVault: PhotoVault,
    private val dispatchers: AppDispatchers
) {

    fun observeAllActiveScans(): Flow<List<ScanResult>> {
        return scanDao.observeAllActiveScans().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun observeActiveScansByKind(kind: String): Flow<List<ScanResult>> {
        return scanDao.observeActiveScansByKind(kind).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun observeScanById(id: String): Flow<ScanResult?> {
        return scanDao.observeScanById(id).map { it?.toDomainModel() }
    }

    suspend fun getScanById(id: String): ScanResult? = withContext(dispatchers.io) {
        scanDao.getScanById(id)?.toDomainModel()
    }

    fun observeLatestActiveScan(): Flow<ScanResult?> {
        return scanDao.observeLatestActiveScan().map { it?.toDomainModel() }
    }

    suspend fun saveScan(scan: ScanResult, photoBitmap: Bitmap? = null): Outcome<String> = withContext(dispatchers.io) {
        try {
            var fileId = scan.photoFileId
            if (photoBitmap != null) {
                val newFileId = fileId ?: UUID.randomUUID().toString()
                val savedFile = photoVault.saveEncryptedPhoto(newFileId, photoBitmap)
                if (savedFile != null) {
                    fileId = newFileId
                } else {
                    Logger.w("PhotoVault failed to encrypt photo, proceeding without photo")
                    fileId = null
                }
            }

            val updatedScan = scan.copy(photoFileId = fileId)
            val entity = updatedScan.toEntity()
            scanDao.insertOrUpdateScan(entity)

            // Update metric noise tracking
            updatedScan.metrics.forEach { (metricId, metricVal) ->
                if (metricVal.sd > 0f) {
                    metricNoiseDao.setNoise(
                        MetricNoiseEntity(
                            metricId = metricId,
                            sd = metricVal.sd,
                            n = 1
                        )
                    )
                }
            }

            Outcome.Success(scan.id)
        } catch (e: Exception) {
            Logger.e("Failed to save scan", e)
            Outcome.Error(e.localizedMessage ?: "Failed to save scan")
        }
    }

    suspend fun deleteScan(id: String, soft: Boolean = true): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            val existing = scanDao.getScanById(id)
            if (existing?.photoFileId != null) {
                photoVault.deletePhoto(existing.photoFileId)
            }
            if (soft) {
                scanDao.markDeleted(id, System.currentTimeMillis())
            } else {
                scanDao.deletePermanently(id)
            }
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Outcome.Error(e.localizedMessage ?: "Failed to delete scan")
        }
    }

    suspend fun deletePhotoOnly(id: String): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            val existing = scanDao.getScanById(id)
            if (existing?.photoFileId != null) {
                photoVault.deletePhoto(existing.photoFileId)
                scanDao.clearPhoto(id, System.currentTimeMillis())
            }
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Outcome.Error(e.localizedMessage ?: "Failed to delete photo")
        }
    }

    suspend fun deleteAllScans(): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            photoVault.clearAllPhotos()
            scanDao.clearAll()
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Outcome.Error(e.localizedMessage ?: "Failed to delete all scans")
        }
    }

    suspend fun loadPhoto(photoFileId: String): Bitmap? = withContext(dispatchers.io) {
        photoVault.loadDecryptedPhoto(photoFileId)
    }

    suspend fun getMetricNoise(metricId: String): Float? = withContext(dispatchers.io) {
        metricNoiseDao.getNoiseForMetric(metricId)?.sd
    }

    // JSON serialization mappings
    private fun ScanResult.toEntity(): ScanEntity {
        val now = System.currentTimeMillis()
        return ScanEntity(
            id = id,
            createdAt = createdAt,
            kind = kind,
            engineVersion = engineVersion,
            qualityScore = qualityReport.score,
            qualityFlagsJson = JSONArray(qualityReport.flags).toString(),
            metricsJson = serializeMetrics(metrics),
            indicesJson = JSONObject(indices).toString(),
            skinJson = serializeSkin(skinResult),
            photoFileId = photoFileId,
            note = note,
            syncedAt = null,
            updatedAt = now,
            deleted = false
        )
    }

    private fun ScanEntity.toDomainModel(): ScanResult {
        return ScanResult(
            id = id,
            createdAt = createdAt,
            kind = kind,
            engineVersion = engineVersion,
            qualityReport = QualityReport(
                score = qualityScore,
                flags = parseFlags(qualityFlagsJson),
                perGate = emptyMap()
            ),
            metrics = parseMetrics(metricsJson),
            indices = parseIndices(indicesJson),
            skinResult = parseSkin(skinJson),
            photoFileId = photoFileId,
            note = note
        )
    }

    private fun serializeMetrics(metrics: Map<String, MetricValue>): String {
        val root = JSONObject()
        metrics.forEach { (key, m) ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("value", m.value.toDouble())
                put("sd", m.sd.toDouble())
                put("unit", m.unit)
                put("rangePosition", m.rangePosition.name)
                put("rangePositionText", m.rangePositionText)
                put("reliability", m.reliability.toDouble())
                put("changeability", m.changeability.name)
                put("typicalMin", m.typicalMin.toDouble())
                put("typicalMax", m.typicalMax.toDouble())
                put("isProvisional", m.isProvisional)
                put("sourceNote", m.sourceNote)
            }
            root.put(key, obj)
        }
        return root.toString()
    }

    private fun parseMetrics(json: String): Map<String, MetricValue> {
        val result = mutableMapOf<String, MetricValue>()
        if (json.isBlank()) return result
        try {
            val root = JSONObject(json)
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = root.getJSONObject(key)
                result[key] = MetricValue(
                    id = obj.optString("id", key),
                    name = obj.optString("name", key),
                    value = obj.optDouble("value", 0.0).toFloat(),
                    sd = obj.optDouble("sd", 0.5).toFloat(),
                    unit = obj.optString("unit", ""),
                    rangePosition = try {
                        RangePosition.valueOf(obj.optString("rangePosition", "WITHIN"))
                    } catch (_: Exception) {
                        RangePosition.WITHIN
                    },
                    rangePositionText = obj.optString("rangePositionText", ""),
                    reliability = obj.optDouble("reliability", 0.9).toFloat(),
                    changeability = try {
                        ChangeabilityTag.valueOf(obj.optString("changeability", "STRUCTURE"))
                    } catch (_: Exception) {
                        ChangeabilityTag.STRUCTURE
                    },
                    typicalMin = obj.optDouble("typicalMin", 0.0).toFloat(),
                    typicalMax = obj.optDouble("typicalMax", 0.0).toFloat(),
                    isProvisional = obj.optBoolean("isProvisional", false),
                    sourceNote = obj.optString("sourceNote", "")
                )
            }
        } catch (e: Exception) {
            Logger.w("Failed to parse metricsJson: ${e.message}")
        }
        return result
    }

    private fun serializeSkin(skin: SkinAnalysisResult?): String {
        if (skin == null) return ""
        val root = JSONObject()
        root.put("clarityIndex", skin.clarityIndex)
        root.put("needsClinician", skin.needsClinician)
        root.put("clinicianReason", skin.clinicianReason ?: "")
        val descObj = JSONObject()
        skin.descriptors.forEach { (k, d) ->
            val item = JSONObject()
            item.put("severity", d.severity)
            item.put("confidence", d.confidence)
            descObj.put(k, item)
        }
        root.put("descriptors", descObj)
        val compObj = JSONObject()
        skin.components.forEach { (k, v) -> compObj.put(k, v.toDouble()) }
        root.put("components", compObj)
        root.put("notes", JSONArray(skin.notes))
        return root.toString()
    }

    private fun parseSkin(json: String): SkinAnalysisResult? {
        if (json.isBlank()) return null
        return try {
            val root = JSONObject(json)
            val clarity = root.optInt("clarityIndex", 80)
            val needsClinician = root.optBoolean("needsClinician", false)
            val clinicianReason = root.optString("clinicianReason").takeIf { it.isNotBlank() }
            val descMap = mutableMapOf<String, SkinDescriptor>()
            val descObj = root.optJSONObject("descriptors")
            if (descObj != null) {
                val dKeys = descObj.keys()
                while (dKeys.hasNext()) {
                    val k = dKeys.next()
                    val o = descObj.getJSONObject(k)
                    descMap[k] = SkinDescriptor(
                        severity = o.optString("severity", "mild"),
                        confidence = o.optString("confidence", "medium")
                    )
                }
            }
            val compMap = mutableMapOf<String, Float>()
            val compObj = root.optJSONObject("components")
            if (compObj != null) {
                val cKeys = compObj.keys()
                while (cKeys.hasNext()) {
                    val k = cKeys.next()
                    compMap[k] = compObj.optDouble(k, 0.0).toFloat()
                }
            }
            val notes = mutableListOf<String>()
            val notesArr = root.optJSONArray("notes")
            if (notesArr != null) {
                for (i in 0 until notesArr.length()) {
                    notes.add(notesArr.getString(i))
                }
            }
            SkinAnalysisResult(
                clarityIndex = clarity,
                descriptors = descMap,
                components = compMap,
                needsClinician = needsClinician,
                clinicianReason = clinicianReason,
                notes = notes
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseIndices(json: String): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        if (json.isBlank()) return result
        try {
            val obj = JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                result[k] = obj.optInt(k, 85)
            }
        } catch (_: Exception) {}
        return result
    }

    private fun parseFlags(json: String): List<String> {
        val result = mutableListOf<String>()
        if (json.isBlank()) return result
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                result.add(arr.getString(i))
            }
        } catch (_: Exception) {}
        return result
    }
}
