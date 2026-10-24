package com.spacare.manager.data.local.db.converter

import androidx.room.TypeConverter
import com.spacare.manager.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

class DataConverters {

    // --- List<String> ---
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null || list.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- List<TreatmentPackage> ---
    @TypeConverter
    fun fromTreatmentPackageList(list: List<TreatmentPackage>?): String {
        if (list == null || list.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { pkg ->
            val obj = JSONObject().apply {
                put("id", pkg.id)
                put("name", pkg.name)
                put("totalSessions", pkg.totalSessions)
                put("usedSessions", pkg.usedSessions)
                put("remainingSessions", pkg.remainingSessions)
                put("startDate", pkg.startDate)
                put("expiryDate", pkg.expiryDate)
                put("assignedTherapist", pkg.assignedTherapist)
                put("intervalDays", pkg.intervalDays)
                put("lastSessionDate", pkg.lastSessionDate ?: "")
                put("nextRecommendedDate", pkg.nextRecommendedDate ?: "")
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toTreatmentPackageList(json: String?): List<TreatmentPackage> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val list = mutableListOf<TreatmentPackage>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TreatmentPackage(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", ""),
                        totalSessions = obj.optInt("totalSessions", 1),
                        usedSessions = obj.optInt("usedSessions", 0),
                        remainingSessions = obj.optInt("remainingSessions", 1),
                        startDate = obj.optString("startDate", ""),
                        expiryDate = obj.optString("expiryDate", ""),
                        assignedTherapist = obj.optString("assignedTherapist", ""),
                        intervalDays = obj.optInt("intervalDays", 5),
                        lastSessionDate = obj.optString("lastSessionDate", "").takeIf { it.isNotBlank() },
                        nextRecommendedDate = obj.optString("nextRecommendedDate", "").takeIf { it.isNotBlank() }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- List<ServiceRecord> ---
    @TypeConverter
    fun fromServiceRecordList(list: List<ServiceRecord>?): String {
        if (list == null || list.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("date", item.date)
                put("serviceName", item.serviceName)
                put("technicianName", item.technicianName)
                put("room", item.room)
                put("cost", item.cost)
                put("feedback", item.feedback ?: "")
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toServiceRecordList(json: String?): List<ServiceRecord> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val list = mutableListOf<ServiceRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ServiceRecord(
                        id = obj.optString("id", ""),
                        date = obj.optString("date", ""),
                        serviceName = obj.optString("serviceName", ""),
                        technicianName = obj.optString("technicianName", ""),
                        room = obj.optString("room", ""),
                        cost = obj.optLong("cost", 0L),
                        feedback = obj.optString("feedback", "").takeIf { it.isNotBlank() }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- List<BeforeAfterComparison> ---
    @TypeConverter
    fun fromBeforeAfterList(list: List<BeforeAfterComparison>?): String {
        if (list == null || list.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("treatmentName", item.treatmentName)
                put("beforeDate", item.beforeDate)
                put("afterDate", item.afterDate)
                put("beforeImageUrl", item.beforeImageUrl)
                put("afterImageUrl", item.afterImageUrl)
                put("sessionMilestone", item.sessionMilestone)
                put("technicianName", item.technicianName)
                put("clinicalNotes", item.clinicalNotes)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toBeforeAfterList(json: String?): List<BeforeAfterComparison> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val list = mutableListOf<BeforeAfterComparison>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    BeforeAfterComparison(
                        id = obj.optString("id", ""),
                        treatmentName = obj.optString("treatmentName", ""),
                        beforeDate = obj.optString("beforeDate", ""),
                        afterDate = obj.optString("afterDate", ""),
                        beforeImageUrl = obj.optString("beforeImageUrl", ""),
                        afterImageUrl = obj.optString("afterImageUrl", ""),
                        sessionMilestone = obj.optString("sessionMilestone", ""),
                        technicianName = obj.optString("technicianName", ""),
                        clinicalNotes = obj.optString("clinicalNotes", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- List<SessionPhotoRecord> ---
    @TypeConverter
    fun fromSessionPhotoList(list: List<SessionPhotoRecord>?): String {
        if (list == null || list.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("sessionNumber", item.sessionNumber)
                put("title", item.title)
                put("date", item.date)
                put("imageUrl", item.imageUrl)
                put("notes", item.notes)
                put("technicianName", item.technicianName)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toSessionPhotoList(json: String?): List<SessionPhotoRecord> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val list = mutableListOf<SessionPhotoRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SessionPhotoRecord(
                        id = obj.optString("id", ""),
                        sessionNumber = obj.optInt("sessionNumber", 1),
                        title = obj.optString("title", ""),
                        date = obj.optString("date", ""),
                        imageUrl = obj.optString("imageUrl", ""),
                        notes = obj.optString("notes", ""),
                        technicianName = obj.optString("technicianName", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}

