package com.pacepay.app.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pacepay.app.data.PaymentStatus
import com.pacepay.app.data.PaymentTransaction
import com.pacepay.app.data.createQrBitmap
import com.pacepay.app.data.isValidUpiId
import com.pacepay.app.data.buildUpiReceiveUri
import com.pacepay.app.ui.components.PageHeading
import com.pacepay.app.ui.components.PrimaryButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

@Composable
fun ActivityScreen(
    transactions: List<PaymentTransaction>,
    onStartPayment: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 21.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        PageHeading(
            eyebrow = "YOUR MONEY, YOUR MOVES",
            title = "Activity",
            supportingText = "A clear, on-device record of payment requests started here.",
        )

        if (transactions.isEmpty()) {
            EmptyActivity(onStartPayment = onStartPayment)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                transactions.forEach { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }
            Text(
                text = "Status comes from the UPI app when it returns a response. Always verify uncertain payments in your bank or UPI app before retrying.",
                modifier = Modifier.padding(horizontal = 3.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EmptyActivity(onStartPayment: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 33.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(20.dp).size(34.dp),
            )
        }
        Text("Nothing here just yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "When you start a payment, its local status will appear here. A payment is only complete when your UPI provider confirms it.",
            modifier = Modifier.fillMaxWidth(0.88f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(3.dp))
        PrimaryButton(text = "Start a payment", onClick = onStartPayment, modifier = Modifier.fillMaxWidth(0.82f))
    }
}

@Composable
private fun TransactionCard(transaction: PaymentTransaction) {
    val title = transaction.recipientName.ifBlank { transaction.upiId }
    val date = SimpleDateFormat("d MMM yyyy · h:mm a", Locale.getDefault()).format(Date(transaction.createdAt))
    val statusColor = when (transaction.status) {
        PaymentStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        PaymentStatus.FAILED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val amount = formatAmount(transaction.amount)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.11f),
            ) {
                Icon(
                    imageVector = when (transaction.status) {
                        PaymentStatus.COMPLETED -> Icons.Filled.CheckCircle
                        PaymentStatus.PENDING -> Icons.Filled.Schedule
                        else -> Icons.Filled.QrCode2
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(12.dp).size(21.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    transaction.upiId,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = statusColor.copy(alpha = 0.10f),
                ) {
                    Text(
                        transaction.status.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                amount,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun ProfileScreen(
    darkTheme: Boolean,
    savedUpiId: String,
    onDarkThemeChange: (Boolean) -> Unit,
    onSaveUpiId: (String) -> Unit,
    onMessage: (String) -> Unit,
) {
    var upiId by rememberSaveable(savedUpiId) { mutableStateOf(savedUpiId) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 21.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        PageHeading(
            eyebrow = "MADE TO FEEL LIKE YOURS",
            title = "Your space",
            supportingText = "Simple settings, with your privacy at the center.",
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                ) {
                    Text(
                        if (darkTheme) "☾" else "☀",
                        modifier = Modifier.padding(11.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 18.sp,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("Appearance", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        if (darkTheme) "Deep, easy-on-the-eyes mode" else "Soft, warm light mode",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
                Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Your payment QR", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Create a shareable QR for your own UPI ID. No bank account is connected to Pace Pay.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
            OutlinedTextField(
                value = upiId,
                onValueChange = {
                    upiId = it
                    qrBitmap = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Your UPI ID") },
                placeholder = { Text("you@bank") },
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
            )
            PrimaryButton(
                text = "Create my QR",
                onClick = {
                    val value = upiId.trim()
                    if (!isValidUpiId(value)) {
                        onMessage("Enter a valid UPI ID, like name@bank.")
                    } else {
                        try {
                            qrBitmap = createQrBitmap(buildUpiReceiveUri(value))
                            upiId = value
                            onSaveUpiId(value)
                        } catch (_: Exception) {
                            onMessage("Couldn't create the QR code. Please try again.")
                        }
                    }
                },
                leadingIcon = Icons.Filled.QrCode2,
            )

            AnimatedVisibility(
                visible = qrBitmap != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                qrBitmap?.let { bitmap ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(Color.White)
                                    .padding(8.dp),
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "UPI payment QR code for $upiId",
                                    modifier = Modifier.size(210.dp),
                                )
                            }
                            Text(upiId, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Let the person paying you verify your UPI app's recipient details before they approve.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.76f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 1.dp).size(18.dp),
                )
                Column(
                    modifier = Modifier.padding(start = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("Your money stays with your bank", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Pace Pay doesn't collect your UPI PIN, bank password, or card details. Payments are authorized in the UPI app you select.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    )
                }
            }
        }

        Text(
            "Pace Pay · Prototype provider hand-off · v1.0.0",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
        )
        Spacer(Modifier.height(8.dp))
    }
}

private fun formatAmount(value: String): String {
    val number = value.toBigDecimalOrNull() ?: return "₹ $value"
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    return "₹ ${formatter.format(number)}"
}
