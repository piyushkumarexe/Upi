package com.pacepay.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pacepay.app.data.PaymentDraft
import com.pacepay.app.ui.components.PrimaryButton
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun PaymentFormScreen(
    draft: PaymentDraft,
    onDraftChange: (PaymentDraft) -> Unit,
    onBack: () -> Unit,
    onScanQr: () -> Unit,
    onReview: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 21.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        ScreenBackButton(onBack = onBack)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "New payment",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
            )
            Text(
                "Add the details, then confirm them in your UPI app.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
            OutlinedTextField(
                value = draft.upiId,
                onValueChange = { onDraftChange(draft.copy(upiId = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Recipient UPI ID") },
                placeholder = { Text("name@bank") },
                trailingIcon = {
                    IconButton(onClick = onScanQr) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan a UPI QR code",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                colors = paymentFieldColors(),
            )
            OutlinedTextField(
                value = draft.recipientName,
                onValueChange = { onDraftChange(draft.copy(recipientName = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Recipient name (optional)") },
                placeholder = { Text("How should we label this payment?") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                colors = paymentFieldColors(),
            )
            OutlinedTextField(
                value = draft.amount,
                onValueChange = { onDraftChange(draft.copy(amount = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Text(
                        "₹",
                        modifier = Modifier.padding(start = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                colors = paymentFieldColors(),
            )
            OutlinedTextField(
                value = draft.note,
                onValueChange = { onDraftChange(draft.copy(note = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)") },
                placeholder = { Text("Add a short note") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                minLines = 1,
                maxLines = 2,
                shape = RoundedCornerShape(17.dp),
                colors = paymentFieldColors(),
            )
        }

        SecurityCallout(
            title = "Your PIN stays private",
            body = "Pace Pay never asks for your UPI PIN. You'll enter it only in the UPI app you choose.",
        )

        PrimaryButton(
            text = "Review payment",
            onClick = onReview,
            leadingIcon = Icons.Filled.ArrowForward,
        )
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
fun PaymentReviewScreen(
    draft: PaymentDraft,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    val displayName = draft.recipientName.trim().ifBlank { draft.upiId.trim() }
    val displayAmount = formatReviewAmount(draft.amount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 21.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        ScreenBackButton(onBack = onBack)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Check the details",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
            )
            Text(
                "Take a second to make sure everything looks right.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 21.dp, vertical = 23.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                ) {
                    Text(
                        text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "₹",
                        modifier = Modifier.padding(15.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(1.dp))
                Text(
                    displayAmount,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 31.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.6).sp,
                )
                Text(
                    displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    draft.upiId.trim(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
                if (draft.note.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            draft.note.trim(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }

        SecurityCallout(
            title = "One last check in your UPI app",
            body = "Choose an installed app such as PhonePe, Google Pay, Paytm or BHIM. Verify the recipient and amount there before authorizing.",
        )

        PrimaryButton(
            text = "Continue to UPI app",
            onClick = onConfirm,
            leadingIcon = Icons.Filled.ArrowForward,
        )
        Text(
            text = "If the provider doesn't return a confirmation, check its payment history before trying again.",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ScreenBackButton(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Go back",
                modifier = Modifier.padding(11.dp).size(19.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SecurityCallout(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
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
                modifier = Modifier.padding(start = 11.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun paymentFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
)

private fun formatReviewAmount(value: String): String {
    val amount = value.toBigDecimalOrNull() ?: return "₹ $value"
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    return "₹ ${formatter.format(amount)}"
}
