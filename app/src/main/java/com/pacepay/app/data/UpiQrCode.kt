package com.pacepay.app.data

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun buildUpiReceiveUri(upiId: String): String = Uri.Builder()
    .scheme("upi")
    .authority("pay")
    .appendQueryParameter("pa", upiId.trim())
    .appendQueryParameter("pn", "Pace Pay user")
    .appendQueryParameter("cu", "INR")
    .build()
    .toString()

fun createQrBitmap(value: String, size: Int = 720): Bitmap {
    val matrix = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, size, size)
    val pixels = IntArray(size * size) { index ->
        val x = index % size
        val y = index / size
        if (matrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
    }
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, size, 0, 0, size, size)
    }
}
