package com.pacepay.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Payment metadata is stored only on this device; no account credentials are kept. */
class TransactionStore(context: Context) {
    private val preferences = context.getSharedPreferences("pace_pay_history", Context.MODE_PRIVATE)

    @Synchronized
    fun all(): List<PaymentTransaction> {
        val array = try {
            JSONArray(preferences.getString(KEY_TRANSACTIONS, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    PaymentTransaction(
                        id = item.optString("id"),
                        recipientName = item.optString("recipientName"),
                        upiId = item.optString("upiId"),
                        amount = item.optString("amount"),
                        note = item.optString("note"),
                        status = PaymentStatus.fromKey(item.optString("status")),
                        createdAt = item.optLong("createdAt"),
                    ),
                )
            }
        }.sortedByDescending { it.createdAt }
    }

    @Synchronized
    fun add(transaction: PaymentTransaction) {
        persist((listOf(transaction) + all()).distinctBy { it.id }.take(MAX_HISTORY))
    }

    @Synchronized
    fun updateStatus(id: String, status: PaymentStatus) {
        persist(all().map { transaction ->
            if (transaction.id == id) transaction.copy(status = status) else transaction
        })
    }

    private fun persist(transactions: List<PaymentTransaction>) {
        val array = JSONArray()
        transactions.forEach { transaction ->
            array.put(
                JSONObject()
                    .put("id", transaction.id)
                    .put("recipientName", transaction.recipientName)
                    .put("upiId", transaction.upiId)
                    .put("amount", transaction.amount)
                    .put("note", transaction.note)
                    .put("status", transaction.status.key)
                    .put("createdAt", transaction.createdAt),
            )
        }
        preferences.edit().putString(KEY_TRANSACTIONS, array.toString()).apply()
    }

    private companion object {
        const val KEY_TRANSACTIONS = "transactions"
        const val MAX_HISTORY = 100
    }
}

class AppSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("pace_pay_settings", Context.MODE_PRIVATE)

    var darkTheme: Boolean
        get() = preferences.getBoolean(KEY_DARK_THEME, false)
        set(value) = preferences.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var savedUpiId: String
        get() = preferences.getString(KEY_SAVED_UPI_ID, "").orEmpty()
        set(value) = preferences.edit().putString(KEY_SAVED_UPI_ID, value).apply()

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_SAVED_UPI_ID = "saved_upi_id"
    }
}
