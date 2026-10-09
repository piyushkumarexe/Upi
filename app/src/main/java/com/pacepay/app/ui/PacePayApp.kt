package com.pacepay.app.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.pacepay.app.data.AppSettingsStore
import com.pacepay.app.data.PaymentDraft
import com.pacepay.app.data.PaymentStatus
import com.pacepay.app.data.PaymentTransaction
import com.pacepay.app.data.TransactionStore
import com.pacepay.app.data.buildUpiPaymentUri
import com.pacepay.app.data.isValidUpiId
import com.pacepay.app.data.normalizedAmount
import com.pacepay.app.data.parseUpiQr
import com.pacepay.app.data.paymentResponseStatus
import com.pacepay.app.ui.components.MainDestination
import com.pacepay.app.ui.components.PaceBottomBar
import com.pacepay.app.ui.theme.PacePayTheme
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun PacePayApp(
    onLaunchQrScan: ((value: String?, error: String?) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val settings = remember(context) { AppSettingsStore(context.applicationContext) }
    val transactionStore = remember(context) { TransactionStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var destination by rememberSaveable { mutableStateOf(MainDestination.HOME) }
    var draft by remember { mutableStateOf(PaymentDraft()) }
    var darkTheme by rememberSaveable { mutableStateOf(settings.darkTheme) }
    var savedUpiId by rememberSaveable { mutableStateOf(settings.savedUpiId) }
    var transactions by remember { mutableStateOf(transactionStore.all()) }
    var activeTransactionId by rememberSaveable { mutableStateOf<String?>(null) }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun startNewPayment() {
        draft = PaymentDraft()
        destination = MainDestination.PAYMENT
    }

    fun scanForPayment() {
        onLaunchQrScan { value, error ->
            if (error != null) {
                showMessage(error)
            } else {
                val scanned = value?.let(::parseUpiQr)
                if (scanned == null) {
                    showMessage("That QR code doesn't look like a UPI payment QR.")
                } else {
                    draft = PaymentDraft(
                        recipientName = scanned.recipientName,
                        upiId = scanned.upiId,
                        amount = scanned.amount,
                        note = scanned.note,
                    )
                    destination = MainDestination.PAYMENT
                }
            }
        }
    }

    val paymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val transactionId = activeTransactionId
        if (transactionId != null) {
            val returnedIntent = result.data
            val response = returnedIntent?.getStringExtra("response")
                ?: returnedIntent?.data?.getQueryParameter("response")
                ?: returnedIntent?.data?.getQueryParameter("Status")?.let { "Status=$it" }
                ?: returnedIntent?.let { intent ->
                    val statusKey = intent.extras?.keySet()
                        ?.firstOrNull { it.equals("Status", ignoreCase = true) }
                    statusKey?.let { key -> intent.extras?.get(key)?.toString()?.let { "Status=$it" } }
                }
            val status = paymentResponseStatus(response)
            transactionStore.updateStatus(transactionId, status)
            transactions = transactionStore.all()
            destination = MainDestination.ACTIVITY
            activeTransactionId = null

            when (status) {
                PaymentStatus.COMPLETED -> showMessage("Your UPI app reported a successful payment.")
                PaymentStatus.FAILED -> showMessage("Your UPI app reported that the payment failed.")
                else -> showMessage("No final confirmation came back. Check your UPI app before retrying.")
            }
        }
    }

    fun reviewPayment() {
        val upiId = draft.upiId.trim()
        if (!isValidUpiId(upiId)) {
            showMessage("Enter a valid UPI ID, like name@bank.")
            return
        }
        val amount = normalizedAmount(draft.amount)
        if (amount == null) {
            showMessage("Enter an amount greater than zero, with up to two decimal places.")
            return
        }
        draft = draft.copy(
            recipientName = draft.recipientName.trim(),
            upiId = upiId,
            amount = amount,
            note = draft.note.trim(),
        )
        destination = MainDestination.REVIEW
    }

    fun handOffToUpiProvider() {
        val uri = try {
            buildUpiPaymentUri(draft)
        } catch (_: IllegalArgumentException) {
            showMessage("Check the amount and try again.")
            return
        }
        // Don't preflight this intent with PackageManager: Android 11+ package visibility
        // and provider-specific intent filters can hide valid UPI apps. Let the system
        // chooser resolve all installed UPI providers (PhonePe, Google Pay, Paytm, BHIM, etc.).
        val paymentIntent = Intent(Intent.ACTION_VIEW, uri)

        val transaction = PaymentTransaction(
            id = UUID.randomUUID().toString(),
            recipientName = draft.recipientName,
            upiId = draft.upiId,
            amount = normalizedAmount(draft.amount).orEmpty(),
            note = draft.note,
            status = PaymentStatus.PENDING,
            createdAt = System.currentTimeMillis(),
        )
        transactionStore.add(transaction)
        transactions = transactionStore.all()
        activeTransactionId = transaction.id

        try {
            paymentLauncher.launch(Intent.createChooser(paymentIntent, "Choose a UPI app"))
        } catch (_: ActivityNotFoundException) {
            transactionStore.updateStatus(transaction.id, PaymentStatus.UNAVAILABLE)
            transactions = transactionStore.all()
            activeTransactionId = null
            destination = MainDestination.ACTIVITY
            showMessage("Couldn't open a UPI app. Please try again after installing one.")
        } catch (_: SecurityException) {
            transactionStore.updateStatus(transaction.id, PaymentStatus.UNAVAILABLE)
            transactions = transactionStore.all()
            activeTransactionId = null
            destination = MainDestination.ACTIVITY
            showMessage("Android couldn't open the selected UPI app.")
        }
    }

    val activity = context as? Activity
    SideEffect {
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    PacePayTheme(darkTheme = darkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (destination == MainDestination.HOME ||
                    destination == MainDestination.ACTIVITY ||
                    destination == MainDestination.PROFILE
                ) {
                    PaceBottomBar(selected = destination) { destination = it }
                }
            },
        ) { contentPadding ->
            AnimatedContent(
                targetState = destination,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(180)) +
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 22 },
                            animationSpec = tween(180),
                        )) togetherWith
                        (fadeOut(animationSpec = tween(130)) +
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 28 },
                                animationSpec = tween(130),
                            ))
                },
                label = "page-transition",
            ) { page ->
                when (page) {
                    MainDestination.HOME -> HomeScreen(
                        transactions = transactions,
                        onPay = ::startNewPayment,
                        onScan = ::scanForPayment,
                        onOpenActivity = { destination = MainDestination.ACTIVITY },
                    )
                    MainDestination.PAYMENT -> PaymentFormScreen(
                        draft = draft,
                        onDraftChange = { draft = it },
                        onBack = { destination = MainDestination.HOME },
                        onScanQr = ::scanForPayment,
                        onReview = ::reviewPayment,
                    )
                    MainDestination.REVIEW -> PaymentReviewScreen(
                        draft = draft,
                        onBack = { destination = MainDestination.PAYMENT },
                        onConfirm = ::handOffToUpiProvider,
                    )
                    MainDestination.ACTIVITY -> ActivityScreen(
                        transactions = transactions,
                        onStartPayment = ::startNewPayment,
                    )
                    MainDestination.PROFILE -> ProfileScreen(
                        darkTheme = darkTheme,
                        savedUpiId = savedUpiId,
                        onDarkThemeChange = {
                            settings.darkTheme = it
                            darkTheme = it
                        },
                        onSaveUpiId = {
                            settings.savedUpiId = it
                            savedUpiId = it
                        },
                        onMessage = ::showMessage,
                    )
                }
            }
        }
    }
}
