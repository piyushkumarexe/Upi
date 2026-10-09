package com.pacepay.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiLinksTest {
    @Test
    fun acceptsSimpleAndDottedUpiIds() {
        assertTrue(isValidUpiId("alice@bank"))
        assertTrue(isValidUpiId("a.b-c_1@bank.handle"))
        assertFalse(isValidUpiId("alice bank@bank"))
        assertFalse(isValidUpiId("@bank"))
    }

    @Test
    fun normalizesPositiveAmountsToAtMostTwoDecimals() {
        assertEquals("1250", normalizedAmount("1,250"))
        assertEquals("10.5", normalizedAmount("10.50"))
        assertNull(normalizedAmount("0"))
        assertNull(normalizedAmount("-1"))
        assertNull(normalizedAmount("1.001"))
    }

    @Test
    fun onlyAnExplicitProviderSuccessCountsAsCompleted() {
        assertEquals(PaymentStatus.COMPLETED, paymentResponseStatus("Status=SUCCESS&txnId=123"))
        assertEquals(PaymentStatus.FAILED, paymentResponseStatus("Status=FAILURE"))
        assertEquals(PaymentStatus.UNCONFIRMED, paymentResponseStatus("Status=SUBMITTED"))
        assertEquals(PaymentStatus.UNCONFIRMED, paymentResponseStatus(null))
    }
}
