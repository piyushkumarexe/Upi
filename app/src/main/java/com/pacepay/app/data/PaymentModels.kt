package com.pacepay.app.data

import java.math.BigDecimal

/** A payment request that will be handed off to an installed UPI provider. */
data class PaymentDraft(
    val recipientName: String = "",
    val upiId: String = "",
    val amount: String = "",
    val note: String = "",
)

data class ScannedUpi(
    val upiId: String,
    val recipientName: String = "",
    val amount: String = "",
    val note: String = "",
)

enum class PaymentStatus(val key: String, val label: String) {
    PENDING("pending", "Awaiting confirmation"),
    COMPLETED("completed", "Completed"),
    FAILED("failed", "Failed"),
    UNCONFIRMED("unconfirmed", "Not confirmed"),
    UNAVAILABLE("unavailable", "UPI app unavailable");

    companion object {
        fun fromKey(key: String?): PaymentStatus = entries.firstOrNull { it.key == key } ?: UNCONFIRMED
    }
}

data class PaymentTransaction(
    val id: String,
    val recipientName: String,
    val upiId: String,
    val amount: String,
    val note: String,
    val status: PaymentStatus,
    val createdAt: Long,
) {
    fun amountValue(): BigDecimal? = amount.toBigDecimalOrNull()
}
