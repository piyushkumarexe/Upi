package com.pacepay.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pacepay.app.data.PaymentTransaction
import com.pacepay.app.ui.components.ActionTile
import com.pacepay.app.ui.components.PaceMark
import com.pacepay.app.ui.components.SectionHeading
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    transactions: List<PaymentTransaction>,
    onPay: () -> Unit,
    onScan: () -> Unit,
    onOpenActivity: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PaceMark(size = 42)
                Spacer(Modifier.width(11.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "PACE PAY",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                    )
                    Text(
                        text = "Money, at your pace.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    text = "✦",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 18.sp,
                )
            }
        }

        HomeHero(onPay = onPay)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading(title = "Pay your way", caption = "Simple by design")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ActionTile(
                    title = "Scan QR",
                    caption = "Point, scan, pay",
                    icon = Icons.Filled.QrCodeScanner,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onScan,
                )
                ActionTile(
                    title = "UPI ID",
                    caption = "Send to anyone",
                    icon = Icons.Filled.Send,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    onClick = onPay,
                )
                ActionTile(
                    title = "Activity",
                    caption = "See your payments",
                    icon = Icons.Filled.History,
                    tint = Color(0xFFB16B32),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenActivity,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            SectionHeading(title = "Recent activity", caption = if (transactions.isEmpty()) null else "View all")
            if (transactions.isEmpty()) {
                EmptyRecentCard(onPay = onPay)
            } else {
                transactions.firstOrNull()?.let { transaction ->
                    RecentPaymentCard(transaction = transaction, onClick = onOpenActivity)
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(11.dp))
                Text(
                    text = "Your UPI PIN stays in your bank's UPI app. Pace Pay never asks for it.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun HomeHero(onPay: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "hero-pulse")
    val breathing by pulse.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hero-breathing",
    )
    val deepGreen = Color(0xFF174B3B)
    val lime = Color(0xFFCCF38C)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1D5C47), deepGreen)))
            .padding(22.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 62.dp, y = (-74).dp)
                .size(194.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.045f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 42.dp, y = 72.dp)
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.045f)),
        )
        Column(modifier = Modifier.fillMaxWidth(0.91f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(breathing)
                        .clip(CircleShape)
                        .background(lime),
                )
                Text(
                    text = "UPI PAYMENTS, SIMPLIFIED",
                    color = lime,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.15.sp,
                )
            }
            Spacer(Modifier.height(13.dp))
            Text(
                text = "Pay at your\nown pace.",
                color = Color.White,
                fontSize = 29.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.65).sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "A quick hand-off to the UPI app you already trust.",
                color = Color.White.copy(alpha = 0.77f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(17.dp))
            Surface(
                onClick = onPay,
                shape = RoundedCornerShape(14.dp),
                color = lime,
                contentColor = deepGreen,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Make a payment", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyRecentCard(onPay: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp).size(22.dp),
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("A fresh start", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Your payment activity will show up here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun RecentPaymentCard(
    transaction: PaymentTransaction,
    onClick: () -> Unit,
) {
    val name = transaction.recipientName.ifBlank { transaction.upiId }
    val dateText = SimpleDateFormat("d MMM · h:mm a", Locale.getDefault()).format(Date(transaction.createdAt))
    val amountText = formatRupees(transaction.amount)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            ) {
                Text(
                    text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "₹",
                    modifier = Modifier.padding(13.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${transaction.status.label} · $dateText",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(amountText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatRupees(value: String): String {
    val amount = value.toDoubleOrNull() ?: return "₹ $value"
    return NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.format(amount)
}
