package com.pacepay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.code.scanner.GmsBarcodeScanning
import com.pacepay.app.ui.PacePayApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PacePayApp(onLaunchQrScan = ::launchQrScan)
        }
    }

    private fun launchQrScan(onResult: (value: String?, error: String?) -> Unit) {
        try {
            GmsBarcodeScanning.getClient(this)
                .startScan()
                .addOnSuccessListener { result ->
                    val value = result.rawValue
                    if (value.isNullOrBlank()) {
                        onResult(null, "No QR code was found. Try again in better light.")
                    } else {
                        onResult(value, null)
                    }
                }
                .addOnCanceledListener {
                    // The user closed the scanner; leave the current screen unchanged.
                }
                .addOnFailureListener {
                    onResult(null, "The QR scanner couldn't start. Check Google Play services and try again.")
                }
        } catch (_: Exception) {
            onResult(null, "The QR scanner isn't available on this device.")
        }
    }
}
