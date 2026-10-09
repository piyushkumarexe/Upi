package com.pacepay.app.data

import android.net.Uri
import java.math.BigDecimal
import java.util.Locale

private val upiIdPattern = Regex("^[A-Za-z0-9][A-Za-z0-9._-]{0,127}@[A-Za-z0-9][A-Za-z0-9.-]{0,63}$")
private val amountPattern = Regex("^\\d{1,9}(\\.\\d{1,2})?$")

fun isValidUpiId(value: String): Boolean = upiIdPattern.matches(value.trim())

fun normalizedAmount(value: String): String? {
    val cleaned = value.trim().replace(",", "")
    if (!amountPattern.matches(cleaned)) return null
    val amount = cleaned.toBigDecimalOrNull() ?: return null
    if (amount <= BigDecimal.ZERO) return null
    return amount.stripTrailingZeros().toPlainString()
}

fun buildUpiPaymentUri(draft: PaymentDraft): Uri {
    val amount = requireNotNull(normalizedAmount(draft.amount)) { "Enter a valid amount" }
    return Uri.Builder()
        .scheme("upi")
        .authority("pay")
        .appendQueryParameter("pa", draft.upiId.trim())
        .appendQueryParameter("pn", draft.recipientName.trim().ifBlank { draft.upiId.trim() })
        .appendQueryParameter("am", amount)
        .appendQueryParameter("cu", "INR")
        .apply {
            draft.note.trim().takeIf(String::isNotEmpty)?.let { appendQueryParameter("tn", it) }
        }
        .build()
}

fun parseUpiQr(rawValue: String): ScannedUpi? {
    val value = rawValue.trim()
    if (value.isEmpty()) return null

    if (value.startsWith("upi://", ignoreCase = true)) {
        return try {
            val uri = Uri.parse(value)
            val upiId = uri.getQueryParameter("pa")?.trim().orEmpty()
            if (!isValidUpiId(upiId)) {
                null
            } else {
                ScannedUpi(
                    upiId = upiId,
                    recipientName = uri.getQueryParameter("pn").orEmpty(),
                    amount = uri.getQueryParameter("am")?.takeIf { normalizedAmount(it) != null }.orEmpty(),
                    note = uri.getQueryParameter("tn").orEmpty(),
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    return value.takeIf(::isValidUpiId)?.let { ScannedUpi(upiId = it) }
}

fun paymentResponseStatus(response: String?): PaymentStatus {
    if (response.isNullOrBlank()) return PaymentStatus.UNCONFIRMED

    val normalized = response.trim().uppercase(Locale.ROOT)
    val status = response
        .split('&', ';')
        .mapNotNull { part ->
            val separator = part.indexOf('=')
            if (separator < 0) null else part.substring(0, separator).trim() to part.substring(separator + 1).trim()
        }
        .firstOrNull { (key, _) -> key.equals("status", ignoreCase = true) }
        ?.second
        ?.uppercase(Locale.ROOT)
        ?: normalized

    return when (status) {
        "SUCCESS", "COMPLETED" -> PaymentStatus.COMPLETED
        "FAILURE", "FAILED" -> PaymentStatus.FAILED
        else -> PaymentStatus.UNCONFIRMED
    }
}
