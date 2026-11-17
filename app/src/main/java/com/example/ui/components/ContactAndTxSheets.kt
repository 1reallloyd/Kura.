package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.LedgerContact
import com.example.data.model.LedgerTransaction
import com.example.data.model.TransactionKind
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimaryWhite
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.Saffron
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.WarmAmberBg

@Composable
internal fun KuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    testTag: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = OnSurfaceVariant.copy(alpha = 0.6f)) },
            isError = errorText != null,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLow,
                unfocusedContainerColor = SurfaceContainerLow,
                errorContainerColor = SurfaceContainerLow,
                focusedBorderColor = Saffron,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = ErrorRed
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRed,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactSheet(
    existingContacts: List<LedgerContact>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, loc: String, type: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(LedgerContact.TYPE_CUSTOMER) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add contact", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            KuraTextField(
                value = name,
                onValueChange = { name = it.take(60); nameError = null },
                label = "Full name",
                placeholder = "e.g. Ama Serwaa",
                errorText = nameError,
                testTag = "add_contact_name_input"
            )
            KuraTextField(
                value = phone,
                onValueChange = { phone = it.take(30); phoneError = null },
                label = "Phone number (optional)",
                placeholder = "e.g. 024 123 4567",
                errorText = phoneError,
                keyboardType = KeyboardType.Phone,
                testTag = "add_contact_phone_input"
            )
            KuraTextField(
                value = loc,
                onValueChange = { loc = it.take(80) },
                label = "Location or landmark (optional)",
                placeholder = "e.g. Madina market",
                testTag = "add_contact_loc_input"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    LedgerContact.TYPE_CUSTOMER to "Customer",
                    LedgerContact.TYPE_SUPPLIER to "Supplier"
                ).forEach { (key, label) ->
                    val active = type == key
                    Button(
                        onClick = { type = key },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) PrimaryBlack else SurfaceContainerLow,
                            contentColor = if (active) OnPrimaryWhite else OnSurface
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isEmpty()) {
                        nameError = "Enter a name"
                        return@Button
                    }
                    val phErr = KuraFormatters.validatePhoneOptional(phone, existingContacts)
                    if (phErr != null) {
                        phoneError = phErr
                        return@Button
                    }
                    onSave(cleanName, phone.trim(), loc.trim(), type)
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_contact_button")
            ) {
                Text("Save contact", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactSheet(
    contact: LedgerContact,
    existingContacts: List<LedgerContact>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, loc: String) -> Unit
) {
    var name by remember { mutableStateOf(contact.name) }
    var phone by remember { mutableStateOf(contact.phone) }
    var loc by remember { mutableStateOf(contact.loc) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit details", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            KuraTextField(
                value = name,
                onValueChange = { name = it.take(60); nameError = null },
                label = "Full name",
                placeholder = "Full name",
                errorText = nameError
            )
            KuraTextField(
                value = phone,
                onValueChange = { phone = it.take(30); phoneError = null },
                label = "Phone number",
                placeholder = "Optional",
                errorText = phoneError,
                keyboardType = KeyboardType.Phone
            )
            KuraTextField(
                value = loc,
                onValueChange = { loc = it.take(80) },
                label = "Location or landmark",
                placeholder = "Optional"
            )
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isEmpty()) {
                        nameError = "Enter a name"
                        return@Button
                    }
                    val phErr = KuraFormatters.validatePhoneOptional(phone, existingContacts, selfId = contact.id)
                    if (phErr != null) {
                        phoneError = phErr
                        return@Button
                    }
                    onSave(cleanName, phone.trim(), loc.trim())
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_edit_contact_button")
            ) {
                Text("Save changes", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    kind: TransactionKind,
    currentBalanceCents: Long,
    currency: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (amountCents: Long, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var warnedOverpay by remember { mutableStateOf(false) }
    var overpayWarningText by remember { mutableStateOf<String?>(null) }

    val parsedCents = remember(amountText) { KuraFormatters.parseCents(amountText) }
    val newBalancePreview = remember(parsedCents, currentBalanceCents, kind) {
        if (parsedCents == null) null
        else {
            val nb = currentBalanceCents + parsedCents * kind.sign
            val suffix = when {
                nb > 0L -> " (they owe you)"
                nb < 0L -> " (you owe them)"
                else -> " (settled)"
            }
            "New balance: ${KuraFormatters.formatSignedMoney(nb, currency)}$suffix"
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(kind.label, style = MaterialTheme.typography.headlineMedium, color = OnSurface)

            Column {
                KuraTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = null
                        warnedOverpay = false
                        overpayWarningText = null
                    },
                    label = "Amount ($currency)",
                    placeholder = "0.00",
                    errorText = amountError,
                    keyboardType = KeyboardType.Decimal,
                    testTag = "tx_amount_input"
                )
                if (newBalancePreview != null) {
                    Text(
                        text = newBalancePreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            KuraTextField(
                value = noteText,
                onValueChange = { noteText = it.take(120) },
                label = "Note",
                placeholder = kind.noteHint,
                testTag = "tx_note_input"
            )

            if (overpayWarningText != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarmAmberBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = overpayWarningText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface
                    )
                }
            }

            Button(
                onClick = {
                    val cents = KuraFormatters.parseCents(amountText)
                    if (cents == null) {
                        amountError = KuraFormatters.amountError(amountText)
                        return@Button
                    }
                    val owed: Long? = when (kind) {
                        TransactionKind.GET -> maxOf(currentBalanceCents, 0L)
                        TransactionKind.PAY -> maxOf(-currentBalanceCents, 0L)
                        else -> null
                    }
                    if (owed != null && cents > owed && !warnedOverpay) {
                        warnedOverpay = true
                        overpayWarningText = if (owed > 0L) {
                            "This is ${KuraFormatters.formatMoney(cents - owed, currency)} more than the ${KuraFormatters.formatMoney(owed, currency)} balance. Saving will flip the balance the other way."
                        } else {
                            "There is nothing to settle here. Saving will create a balance the other way."
                        }
                        return@Button
                    }
                    onSave(cents * kind.sign, noteText.trim().take(120))
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_tx_button")
            ) {
                Text(
                    text = if (warnedOverpay) "Save anyway" else "Save ${kind.label.lowercase()}",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(
    transaction: LedgerTransaction,
    currency: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSaveFix: (newSignedAmountCents: Long, newNote: String) -> Unit,
    onVoid: () -> Unit
) {
    var amountText by remember {
        mutableStateOf(KuraFormatters.formatInputCents(kotlin.math.abs(transaction.amountCents)))
    }
    var noteText by remember { mutableStateOf(transaction.note) }
    var amountError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit entry", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            KuraTextField(
                value = amountText,
                onValueChange = { amountText = it; amountError = null },
                label = "Amount ($currency)",
                placeholder = "0.00",
                errorText = amountError,
                keyboardType = KeyboardType.Decimal
            )
            KuraTextField(
                value = noteText,
                onValueChange = { noteText = it.take(120) },
                label = "Note",
                placeholder = "Optional"
            )
            Text(
                text = "The original stays in the history, marked as voided.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
            Button(
                onClick = {
                    val cents = KuraFormatters.parseCents(amountText)
                    if (cents == null) {
                        amountError = KuraFormatters.amountError(amountText)
                        return@Button
                    }
                    val sign = if (transaction.amountCents >= 0L) 1L else -1L
                    val signed = cents * sign
                    val cleanNote = noteText.trim().take(120)
                    if (signed == transaction.amountCents && cleanNote == transaction.note) {
                        onDismiss()
                    } else {
                        onSaveFix(signed, cleanNote)
                    }
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_fix_tx_button")
            ) {
                Text("Save correction", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onVoid,
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer, contentColor = OnErrorContainer),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("void_tx_button")
            ) {
                Text("Void this entry", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppReminderSheet(
    contact: LedgerContact,
    balanceCents: Long,
    bizName: String,
    currency: String,
    countryCode: String,
    existingContacts: List<LedgerContact>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSavePhone: (String) -> Unit,
    onOpenWhatsApp: (url: String) -> Unit,
    onCopyMessage: (message: String) -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (contact.phone.isBlank()) {
                Text("Add a phone number", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                KuraTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it.take(30); phoneError = null },
                    label = "Phone number",
                    placeholder = "e.g. 024 123 4567",
                    errorText = phoneError,
                    keyboardType = KeyboardType.Phone
                )
                Button(
                    onClick = {
                        val trimmed = phoneInput.trim()
                        if (!KuraFormatters.isValidPhone(trimmed)) {
                            phoneError = "Enter a valid phone number"
                            return@Button
                        }
                        val dupErr = KuraFormatters.validatePhoneOptional(trimmed, existingContacts, selfId = contact.id)
                        if (dupErr != null) {
                            phoneError = dupErr
                            return@Button
                        }
                        onSavePhone(trimmed)
                    },
                    enabled = !isBusy,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Save and continue", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                val bizSuffix = if (bizName.isNotBlank()) " with $bizName" else ""
                val message = "Hello ${contact.name}, a friendly reminder that you have an outstanding balance of ${
                    KuraFormatters.formatMoney(balanceCents, currency)
                }$bizSuffix. Thank you!"
                val url = KuraFormatters.whatsAppUrl(contact.phone, countryCode, message)

                Text("Send on WhatsApp", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Button(
                    onClick = { onOpenWhatsApp(url) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("open_whatsapp_button")
                ) {
                    Text("Open WhatsApp", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
                Button(
                    onClick = { onCopyMessage(message) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = OnSurface),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("copy_reminder_button")
                ) {
                    Text("Copy message", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
