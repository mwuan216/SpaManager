package com.spacare.manager.core.sale

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PurchasedKey(
    val key: String,
    val tierId: String,
    val planTitle: String,
    val amountPaid: Long,
    val retailPrice: Long,
    val orderCode: Long,
    val purchasedAt: String,
    var isCopied: Boolean = false
)

object SalePartnerManager {

    private const val PREF_SALE = "spacare_sale_partner_pref"
    private const val KEY_IS_SALE_PARTNER = "key_is_sale_partner"
    private const val KEY_SALE_CODE = "key_sale_code"
    private const val KEY_PURCHASED_KEYS_JSON = "key_purchased_keys_json"

    // Danh sách mã kích hoạt chế độ Sale đối tác
    private val VALID_SALE_CODES = listOf(
        "SALE_VANDINH_2026",
        "VANDINH_SALE_VIP",
        "SALE70",
        "VANDINH_PARTNER_70"
    )

    /**
     * Kiểm tra xem thiết bị này đã được mở khóa quyền Sale Partner hay chưa
     */
    fun isSalePartner(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREF_SALE, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_SALE_PARTNER, false)
    }

    /**
     * Kích hoạt quyền Sale Partner bằng mã đặc biệt
     */
    fun unlockSalePartner(context: Context, inputCode: String): Boolean {
        val cleaned = inputCode.trim().uppercase()
        val isValid = VALID_SALE_CODES.any { it.equals(cleaned, ignoreCase = true) } ||
                cleaned.startsWith("SALE-") ||
                cleaned.startsWith("VD-SALE-")

        if (isValid) {
            val prefs = context.applicationContext.getSharedPreferences(PREF_SALE, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_SALE_PARTNER, true)
                .putString(KEY_SALE_CODE, cleaned)
                .apply()
            return true
        }
        return false
    }

    /**
     * Khóa / Rút lại quyền Sale Partner
     */
    fun lockSalePartner(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_SALE, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_SALE_PARTNER, false).apply()
    }

    /**
     * Lấy mã đối tác Sale hiện tại
     */
    fun getSaleCode(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREF_SALE, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SALE_CODE, "SALE_PARTNER") ?: "SALE_PARTNER"
    }

    /**
     * Lưu Key mới mua vào kho key cá nhân của Sale
     */
    fun savePurchasedKey(context: Context, item: PurchasedKey) {
        val list = getPurchasedKeys(context).toMutableList()
        // Đưa key mới nhất lên đầu danh sách
        list.add(0, item)

        val jsonArray = JSONArray()
        list.forEach { k ->
            val obj = JSONObject().apply {
                put("key", k.key)
                put("tierId", k.tierId)
                put("planTitle", k.planTitle)
                put("amountPaid", k.amountPaid)
                put("retailPrice", k.retailPrice)
                put("orderCode", k.orderCode)
                put("purchasedAt", k.purchasedAt)
                put("isCopied", k.isCopied)
            }
            jsonArray.put(obj)
        }

        val prefs = context.applicationContext.getSharedPreferences(PREF_SALE, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PURCHASED_KEYS_JSON, jsonArray.toString()).apply()
    }

    /**
     * Lấy danh sách tất cả các Key đã mua
     */
    fun getPurchasedKeys(context: Context): List<PurchasedKey> {
        val prefs = context.applicationContext.getSharedPreferences(PREF_SALE, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PURCHASED_KEYS_JSON, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<PurchasedKey>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    PurchasedKey(
                        key = obj.optString("key", ""),
                        tierId = obj.optString("tierId", ""),
                        planTitle = obj.optString("planTitle", ""),
                        amountPaid = obj.optLong("amountPaid", 0L),
                        retailPrice = obj.optLong("retailPrice", 0L),
                        orderCode = obj.optLong("orderCode", 0L),
                        purchasedAt = obj.optString("purchasedAt", ""),
                        isCopied = obj.optBoolean("isCopied", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Đánh dấu key đã sao chép gửi khách
     */
    fun markKeyAsCopied(context: Context, targetKey: String) {
        val list = getPurchasedKeys(context).toMutableList()
        val index = list.indexOfFirst { it.key.equals(targetKey, ignoreCase = true) }
        if (index != -1) {
            list[index] = list[index].copy(isCopied = true)
            val jsonArray = JSONArray()
            list.forEach { k ->
                val obj = JSONObject().apply {
                    put("key", k.key)
                    put("tierId", k.tierId)
                    put("planTitle", k.planTitle)
                    put("amountPaid", k.amountPaid)
                    put("retailPrice", k.retailPrice)
                    put("orderCode", k.orderCode)
                    put("purchasedAt", k.purchasedAt)
                    put("isCopied", k.isCopied)
                }
                jsonArray.put(obj)
            }
            val prefs = context.applicationContext.getSharedPreferences(PREF_SALE, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_PURCHASED_KEYS_JSON, jsonArray.toString()).apply()
        }
    }
}
