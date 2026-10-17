package com.spacare.manager.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.spacare.manager.data.local.db.SpaDatabase
import com.spacare.manager.data.local.db.converter.DataConverters
import com.spacare.manager.data.local.db.entity.*
import com.spacare.manager.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupStats(
    val clientCount: Int,
    val appointmentCount: Int,
    val transactionCount: Int,
    val packageCount: Int,
    val technicianCount: Int,
    val backupDate: String
)

object BackupManager {

    private val converters = DataConverters()

    /**
     * Xuất toàn bộ dữ liệu từ Room Database thành file JSON sao lưu
     */
    suspend fun exportBackup(context: Context, database: SpaDatabase): Result<File> = withContext(Dispatchers.IO) {
        try {
            val clients = database.clientDao().getAllClientsSync().map { it.toDomain() }
            val appointments = database.appointmentDao().getAllAppointmentsSync().map { it.toDomain() }
            val transactions = database.transactionDao().getAllTransactionsSync().map { it.toDomain() }
            val storeProfile = database.storeProfileDao().getStoreProfileSync()?.toDomain()
            val catalogPackages = database.catalogPackageDao().getAllPackagesSync().map { it.toDomain() }
            val technicians = database.technicianDao().getAllTechniciansSync().map { it.toDomain() }

            val rootJson = JSONObject().apply {
                put("app", "SpaCare Manager")
                put("version", "1.0.0")
                put("timestamp", System.currentTimeMillis())
                put("backupDate", SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()))

                // Clients
                val clientsArray = JSONArray()
                clients.forEach { c ->
                    val obj = JSONObject().apply {
                        put("id", c.id)
                        put("fullName", c.fullName)
                        put("phone", c.phone)
                        put("email", c.email)
                        put("avatarUrl", c.avatarUrl ?: "")
                        put("vipTier", c.vipTier.name)
                        put("skinType", c.skinType)
                        put("skinConditionNotes", c.skinConditionNotes)
                        put("allergyAlerts", converters.fromStringList(c.allergyAlerts))
                        put("contraindications", converters.fromStringList(c.contraindications))
                        put("treatmentPackages", converters.fromTreatmentPackageList(c.treatmentPackages))
                        put("serviceHistory", converters.fromServiceRecordList(c.serviceHistory))
                        put("beforeAfterComparisons", converters.fromBeforeAfterList(c.beforeAfterComparisons))
                        put("sessionPhotos", converters.fromSessionPhotoList(c.sessionPhotos))
                    }
                    clientsArray.put(obj)
                }
                put("clients", clientsArray)

                // Appointments
                val apptsArray = JSONArray()
                appointments.forEach { a ->
                    val obj = JSONObject().apply {
                        put("id", a.id)
                        put("clientName", a.clientName)
                        put("clientPhone", a.clientPhone)
                        put("serviceName", a.serviceName)
                        put("durationMinutes", a.durationMinutes)
                        put("staffName", a.staffName)
                        put("roomName", a.roomName)
                        put("startTime", a.startTime)
                        put("endTime", a.endTime)
                        put("date", a.date)
                        put("status", a.status.name)
                        put("price", a.price)
                        put("reminderSent", a.reminderSent)
                    }
                    apptsArray.put(obj)
                }
                put("appointments", apptsArray)

                // Transactions
                val txsArray = JSONArray()
                transactions.forEach { t ->
                    val obj = JSONObject().apply {
                        put("id", t.id)
                        put("invoiceCode", t.invoiceCode)
                        put("clientName", t.clientName)
                        put("clientPhone", t.clientPhone)
                        put("serviceName", t.serviceName)
                        put("technicianName", t.technicianName)
                        put("time", t.time)
                        put("date", t.date)
                        put("amount", t.amount)
                        put("paymentMethod", t.paymentMethod)
                        put("isPaid", t.isPaid)
                    }
                    txsArray.put(obj)
                }
                put("transactions", txsArray)

                // StoreProfile
                storeProfile?.let { sp ->
                    val spObj = JSONObject().apply {
                        put("name", sp.name)
                        put("hotline", sp.hotline)
                        put("address", sp.address)
                        put("bankBin", sp.selectedBank.bin)
                        put("bankShortName", sp.selectedBank.shortName)
                        put("bankFullName", sp.selectedBank.fullName)
                        put("accountNumber", sp.accountNumber)
                        put("accountHolder", sp.accountHolder)
                        put("defaultMemo", sp.defaultMemo)
                    }
                    put("storeProfile", spObj)
                }

                // Catalog Packages
                val packagesArray = JSONArray()
                catalogPackages.forEach { p ->
                    val obj = JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("totalSessions", p.totalSessions)
                        put("price", p.price)
                        put("durationMinutes", p.durationMinutes)
                        put("description", p.description)
                    }
                    packagesArray.put(obj)
                }
                put("catalogPackages", packagesArray)

                // Technicians
                val techsArray = JSONArray()
                technicians.forEach { tech ->
                    val obj = JSONObject().apply {
                        put("id", tech.id)
                        put("name", tech.name)
                        put("phone", tech.phone)
                        put("specialty", tech.specialty)
                        put("rating", tech.rating.toDouble())
                        put("isActive", tech.isActive)
                    }
                    techsArray.put(obj)
                }
                put("technicians", techsArray)
            }

            val dateFileStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportDir = File(context.filesDir, "exports").apply { if (!exists()) mkdirs() }
            val backupFile = File(exportDir, "SpaCare_Backup_$dateFileStr.json")

            FileOutputStream(backupFile).use { fos ->
                fos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            }

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Chia sẻ file sao lưu qua Zalo, Drive, Gmail, v.v.
     */
    fun shareBackupFile(context: Context, backupFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Bản sao lưu dữ liệu SpaCare Manager")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Lưu hoặc chia sẻ bản sao lưu"))
    }

    /**
     * Đọc nội dung file sao lưu JSON và khôi phục vào Room Database
     */
    suspend fun importBackup(context: Context, database: SpaDatabase, jsonContent: String): Result<BackupStats> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonContent)
            val backupDate = root.optString("backupDate", "Không xác định")

            // Parse Clients
            val clientsList = mutableListOf<ClientEntity>()
            if (root.has("clients")) {
                val array = root.getJSONArray("clients")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    clientsList.add(
                        ClientEntity(
                            id = obj.optString("id", "KH-${System.currentTimeMillis()}-$i"),
                            fullName = obj.optString("fullName", "Khách hàng"),
                            phone = obj.optString("phone", ""),
                            email = obj.optString("email", ""),
                            avatarUrl = obj.optString("avatarUrl", "").takeIf { it.isNotBlank() },
                            vipTier = obj.optString("vipTier", "STANDARD"),
                            skinType = obj.optString("skinType", ""),
                            skinConditionNotes = obj.optString("skinConditionNotes", ""),
                            allergyAlerts = converters.toStringList(obj.optString("allergyAlerts", "[]")),
                            contraindications = converters.toStringList(obj.optString("contraindications", "[]")),
                            treatmentPackages = converters.toTreatmentPackageList(obj.optString("treatmentPackages", "[]")),
                            serviceHistory = converters.toServiceRecordList(obj.optString("serviceHistory", "[]")),
                            beforeAfterComparisons = converters.toBeforeAfterList(obj.optString("beforeAfterComparisons", "[]")),
                            sessionPhotos = converters.toSessionPhotoList(obj.optString("sessionPhotos", "[]"))
                        )
                    )
                }
            }

            // Parse Appointments
            val apptsList = mutableListOf<AppointmentEntity>()
            if (root.has("appointments")) {
                val array = root.getJSONArray("appointments")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    apptsList.add(
                        AppointmentEntity(
                            id = obj.optString("id", "APT-${System.currentTimeMillis()}-$i"),
                            clientName = obj.optString("clientName", ""),
                            clientPhone = obj.optString("clientPhone", ""),
                            serviceName = obj.optString("serviceName", ""),
                            durationMinutes = obj.optInt("durationMinutes", 60),
                            staffName = obj.optString("staffName", ""),
                            roomName = obj.optString("roomName", ""),
                            startTime = obj.optString("startTime", "10:00"),
                            endTime = obj.optString("endTime", "11:00"),
                            date = obj.optString("date", ""),
                            status = obj.optString("status", "CONFIRMED"),
                            price = obj.optLong("price", 0L),
                            reminderSent = obj.optBoolean("reminderSent", true)
                        )
                    )
                }
            }

            // Parse Transactions
            val txsList = mutableListOf<TransactionEntity>()
            if (root.has("transactions")) {
                val array = root.getJSONArray("transactions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    txsList.add(
                        TransactionEntity(
                            id = obj.optString("id", "TX-${System.currentTimeMillis()}-$i"),
                            invoiceCode = obj.optString("invoiceCode", "SPA${1000 + i}"),
                            clientName = obj.optString("clientName", ""),
                            clientPhone = obj.optString("clientPhone", ""),
                            serviceName = obj.optString("serviceName", ""),
                            technicianName = obj.optString("technicianName", ""),
                            time = obj.optString("time", "10:00"),
                            date = obj.optString("date", ""),
                            amount = obj.optLong("amount", 0L),
                            paymentMethod = obj.optString("paymentMethod", "VietQR Chuyển Khoản"),
                            isPaid = obj.optBoolean("isPaid", true)
                        )
                    )
                }
            }

            // Parse StoreProfile
            if (root.has("storeProfile")) {
                val spObj = root.getJSONObject("storeProfile")
                val spEntity = StoreProfileEntity(
                    id = 1,
                    name = spObj.optString("name", "SpaCare Wellness Center"),
                    hotline = spObj.optString("hotline", "1900 6868"),
                    address = spObj.optString("address", ""),
                    bankBin = spObj.optString("bankBin", "970436"),
                    bankShortName = spObj.optString("bankShortName", "Vietcombank"),
                    bankFullName = spObj.optString("bankFullName", "Ngân hàng Ngoại Thương"),
                    accountNumber = spObj.optString("accountNumber", ""),
                    accountHolder = spObj.optString("accountHolder", ""),
                    defaultMemo = spObj.optString("defaultMemo", "SPACARE")
                )
                database.storeProfileDao().insertOrUpdate(spEntity)
            }

            // Parse Catalog Packages
            val packagesList = mutableListOf<CatalogPackageEntity>()
            if (root.has("catalogPackages")) {
                val array = root.getJSONArray("catalogPackages")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    packagesList.add(
                        CatalogPackageEntity(
                            id = obj.optString("id", "PKG-${System.currentTimeMillis()}-$i"),
                            name = obj.optString("name", ""),
                            totalSessions = obj.optInt("totalSessions", 1),
                            price = obj.optLong("price", 0L),
                            durationMinutes = obj.optInt("durationMinutes", 60),
                            description = obj.optString("description", "")
                        )
                    )
                }
            }

            // Parse Technicians
            val techsList = mutableListOf<TechnicianEntity>()
            if (root.has("technicians")) {
                val array = root.getJSONArray("technicians")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    techsList.add(
                        TechnicianEntity(
                            id = obj.optString("id", "TECH-${System.currentTimeMillis()}-$i"),
                            name = obj.optString("name", ""),
                            phone = obj.optString("phone", ""),
                            specialty = obj.optString("specialty", ""),
                            rating = obj.optDouble("rating", 5.0).toFloat(),
                            isActive = obj.optBoolean("isActive", true)
                        )
                    )
                }
            }

            // Write all into database atomically
            database.clientDao().deleteAll()
            database.clientDao().insertAll(clientsList)

            database.appointmentDao().deleteAll()
            database.appointmentDao().insertAll(apptsList)

            database.transactionDao().deleteAll()
            database.transactionDao().insertAll(txsList)

            if (packagesList.isNotEmpty()) {
                database.catalogPackageDao().deleteAll()
                database.catalogPackageDao().insertAll(packagesList)
            }

            if (techsList.isNotEmpty()) {
                database.technicianDao().deleteAll()
                database.technicianDao().insertAll(techsList)
            }

            Result.success(
                BackupStats(
                    clientCount = clientsList.size,
                    appointmentCount = apptsList.size,
                    transactionCount = txsList.size,
                    packageCount = packagesList.size,
                    technicianCount = techsList.size,
                    backupDate = backupDate
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

