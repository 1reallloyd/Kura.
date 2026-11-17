package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactWithSummary
import com.example.data.model.LedgerTransaction
import com.example.data.model.TransactionKind
import com.example.ui.components.DarkHeroCard
import com.example.ui.components.KuraAvatar
import com.example.ui.components.KuraFormatters
import com.example.ui.theme.CoralDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LedgerCanvas
import com.example.ui.theme.OnPrimaryWhite
import com.example.ui.theme.OnSecondaryContainer
import com.example.ui.theme.OnSecondaryFixedVariant
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.OnTertiaryContainer
import com.example.ui.theme.OnTertiaryFixedVariant
import com.example.ui.theme.Outline
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SecondaryFixed
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.TertiaryFixed
import com.example.ui.theme.TertiaryFixedDim

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContactDetailScreen(
    summary: ContactWithSummary,
    currency: String,
    onBack: () -> Unit,
    onEditContact: () -> Unit,
    onDeleteContact: () -> Unit,
    onAddTransaction: (TransactionKind) -> Unit,
    onEditTransaction: (LedgerTransaction) -> Unit,
    onRemindWhatsApp: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val c = summary.contact
    val isSupplier = c.isSupplier
    val bal = summary.balanceCents

    Scaffold(
        containerColor = LedgerCanvas,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleIconAction(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurface,
                            testTag = "detail_back_button",
                            onClick = onBack
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircleIconAction(
                                icon = Icons.Default.Edit,
                                contentDescription = "Edit details",
                                tint = OnSurface,
                                testTag = "detail_edit_button",
                                onClick = onEditContact
                            )
                            CircleIconAction(
                                icon = Icons.Default.Delete,
                                contentDescription = "Delete contact",
                                tint = ErrorRed,
                                testTag = "detail_delete_button",
                                onClick = onDeleteContact
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        KuraAvatar(
                            name = c.name,
                            size = 80.dp,
                            fontSize = 24.sp,
                            showSaffronRing = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(c.name, style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DetailMetaPill(
                                icon = if (isSupplier) Icons.Default.Store else Icons.Default.Person,
                                text = if (isSupplier) "Supplier" else "Customer"
                            )
                            if (c.phone.isNotBlank()) {
                                DetailMetaPill(
                                    icon = Icons.Default.Call,
                                    text = c.phone,
                                    onClick = {
                                        val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.phone}"))
                                        runCatching { context.startActivity(dial) }
                                    }
                                )
                            }
                            if (c.loc.isNotBlank()) {
                                DetailMetaPill(icon = Icons.Default.LocationOn, text = c.loc)
                            } else {
                                Surface(
                                    onClick = onEditContact,
                                    shape = CircleShape,
                                    color = Color.White,
                                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "+ Add location",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Outline,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    DarkHeroCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val badgeText = when {
                                bal > 0L -> "Owes you"
                                bal < 0L -> "You owe"
                                else -> "All settled"
                            }
                            val badgeBg = when {
                                bal > 0L -> TertiaryFixedDim.copy(alpha = 0.2f)
                                bal < 0L -> ErrorRed.copy(alpha = 0.3f)
                                else -> Color.White.copy(alpha = 0.15f)
                            }
                            val badgeFg = when {
                                bal > 0L -> TertiaryFixedDim
                                bal < 0L -> CoralDark
                                else -> Color.White.copy(alpha = 0.85f)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(badgeBg)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(badgeText, style = MaterialTheme.typography.labelSmall, color = badgeFg)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = KuraFormatters.formatMoney(bal, currency),
                                style = MaterialTheme.typography.displayLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val lastActText = if (summary.lastActivityMs > 0L) {
                                "Last activity: ${KuraFormatters.relativeTimeAgo(summary.lastActivityMs).lowercase()}"
                            } else {
                                "No activity yet"
                            }
                            Text(
                                text = lastActText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isSupplier) {
                                DetailActionButton(
                                    modifier = Modifier.weight(1f),
                                    label = "I paid",
                                    icon = Icons.Default.Payments,
                                    containerColor = PrimaryBlack,
                                    contentColor = OnPrimaryWhite,
                                    testTag = "tx_pay_button",
                                    onClick = { onAddTransaction(TransactionKind.PAY) }
                                )
                                DetailActionButton(
                                    modifier = Modifier.weight(1f),
                                    label = "Bought on credit",
                                    icon = Icons.Default.ShoppingBag,
                                    containerColor = SecondaryContainer,
                                    contentColor = OnSecondaryContainer,
                                    testTag = "tx_buy_button",
                                    onClick = { onAddTransaction(TransactionKind.BUY) }
                                )
                            } else {
                                DetailActionButton(
                                    modifier = Modifier.weight(1f),
                                    label = "Gave credit",
                                    icon = Icons.Default.Add,
                                    containerColor = SecondaryContainer,
                                    contentColor = OnSecondaryContainer,
                                    testTag = "tx_give_button",
                                    onClick = { onAddTransaction(TransactionKind.GIVE) }
                                )
                                DetailActionButton(
                                    modifier = Modifier.weight(1f),
                                    label = "Got payment",
                                    icon = Icons.Default.Payments,
                                    containerColor = PrimaryBlack,
                                    contentColor = OnPrimaryWhite,
                                    testTag = "tx_get_button",
                                    onClick = { onAddTransaction(TransactionKind.GET) }
                                )
                            }
                        }

                        if (bal > 0L && !isSupplier) {
                            Surface(
                                onClick = onRemindWhatsApp,
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("remind_whatsapp_button")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = OnTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Remind on WhatsApp", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("History", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                        if (summary.transactions.isNotEmpty()) {
                            Text("Tap an entry to edit", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }

                if (summary.transactions.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Outline)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No records yet. Add the first one above.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                            }
                        }
                    }
                } else {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                summary.transactions.forEachIndexed { idx, tx ->
                                    TransactionRowItem(
                                        tx = tx,
                                        isSupplier = isSupplier,
                                        currency = currency,
                                        onClick = { if (!tx.voided) onEditTransaction(tx) }
                                    )
                                    if (idx < summary.transactions.lastIndex) {
                                        HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleIconAction(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(40.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun DetailMetaPill(
    icon: ImageVector,
    text: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun DetailActionButton(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        modifier = modifier.height(52.dp).testTag(testTag)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TransactionRowItem(
    tx: LedgerTransaction,
    isSupplier: Boolean,
    currency: String,
    onClick: () -> Unit
) {
    val isPositive = tx.amountCents > 0L
    val isOutflow = isPositive == !isSupplier
    val defaultNote = if (isPositive) {
        if (isSupplier) "Payment" else "Credit given"
    } else {
        if (isSupplier) "Bought on credit" else "Payment received"
    }
    val noteText = tx.note.ifBlank { defaultNote }
    val strike = if (tx.voided) TextDecoration.LineThrough else TextDecoration.None

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!tx.voided) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(if (tx.voided) 0.5f else 1f)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isOutflow) SecondaryFixed else TertiaryFixed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isOutflow) Icons.Default.NorthEast else Icons.Default.SouthWest,
                contentDescription = null,
                tint = if (isOutflow) OnSecondaryFixedVariant else OnTertiaryFixedVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = noteText,
                style = MaterialTheme.typography.labelLarge.copy(textDecoration = strike),
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${KuraFormatters.formatShortDate(tx.tsMillis)} · ${KuraFormatters.relativeTimeAgo(tx.tsMillis)}",
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = strike),
                color = OnSurfaceVariant
            )
        }
        Text(
            text = (if (isPositive) "+" else "−") + KuraFormatters.formatMoney(tx.amountCents, currency),
            style = MaterialTheme.typography.labelLarge.copy(textDecoration = strike),
            color = if (isOutflow) OnSurface else OnTertiaryContainer
        )
    }
}
