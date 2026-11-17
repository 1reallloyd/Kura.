package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LedgerContact
import com.example.data.model.UserProfile
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnPrimaryWhite
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.SurfaceContainerLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    profile: UserProfile?,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, biz: String, phone: String, loc: String) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name.orEmpty()) }
    var biz by remember { mutableStateOf(profile?.biz.orEmpty()) }
    var phone by remember { mutableStateOf(profile?.phone.orEmpty()) }
    var loc by remember { mutableStateOf(profile?.loc.orEmpty()) }
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
            Text("Your profile", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            KuraTextField(
                value = name,
                onValueChange = { name = it.take(60); nameError = null },
                label = "Your name",
                placeholder = "e.g. Kwame Ansah",
                errorText = nameError
            )
            KuraTextField(
                value = biz,
                onValueChange = { biz = it.take(80) },
                label = "Business name",
                placeholder = "Optional"
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
                label = "Business location",
                placeholder = "Optional"
            )
            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isEmpty()) {
                        nameError = "Enter your name"
                        return@Button
                    }
                    val cleanPhone = phone.trim()
                    if (cleanPhone.isNotEmpty() && !KuraFormatters.isValidPhone(cleanPhone)) {
                        phoneError = "Enter a valid phone number, or leave it empty"
                        return@Button
                    }
                    onSave(cleanName, biz.trim(), cleanPhone, loc.trim())
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_edit_profile_button")
            ) {
                Text("Save profile", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSheet(
    currentCurrency: String,
    currentCountryCode: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (currency: String, countryCode: String) -> Unit
) {
    var currency by remember { mutableStateOf(currentCurrency) }
    var cc by remember { mutableStateOf(currentCountryCode) }
    var curError by remember { mutableStateOf<String?>(null) }
    var ccError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Currency and country", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            KuraTextField(
                value = currency,
                onValueChange = { currency = it.take(5); curError = null },
                label = "Currency symbol",
                placeholder = "GH₵",
                errorText = curError
            )
            KuraTextField(
                value = cc,
                onValueChange = { cc = it.filter(Char::isDigit).take(4); ccError = null },
                label = "Country dialing code",
                placeholder = "233",
                errorText = ccError,
                keyboardType = KeyboardType.Number
            )
            Button(
                onClick = {
                    if (currency.trim().isEmpty()) {
                        curError = "Enter a currency symbol"
                        return@Button
                    }
                    val digits = cc.filter(Char::isDigit)
                    if (digits.isEmpty()) {
                        ccError = "Enter a dialing code, like 233"
                        return@Button
                    }
                    onSave(currency.trim(), digits)
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_region_button")
            ) {
                Text("Save", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupExportSheet(
    backupJson: String,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Back up your data", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            Text(
                text = "Copy or share this backup text somewhere safe, such as a note or an email to yourself.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
            KuraTextField(
                value = backupJson,
                onValueChange = {},
                label = "Backup JSON",
                placeholder = "",
                singleLine = false,
                minLines = 5
            )
            Button(
                onClick = { onCopy(backupJson) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Copy backup", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = { onShare(backupJson) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = OnSurface),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Share backup file text", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupSheet(
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onRestore: (rawJson: String, onError: (String) -> Unit) -> Unit
) {
    var raw by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Restore from backup", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            KuraTextField(
                value = raw,
                onValueChange = { raw = it; err = null },
                label = "Backup JSON",
                placeholder = "Paste your backup here",
                errorText = err,
                singleLine = false,
                minLines = 5
            )
            Text(
                text = "This replaces your contacts and records on this ledger.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
            Button(
                onClick = {
                    if (raw.isBlank()) {
                        err = "Paste your backup JSON first"
                    } else {
                        onRestore(raw) { message -> err = message }
                    }
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Restore", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyDeletedSheet(
    deletedContacts: List<LedgerContact>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onRestore: (LedgerContact) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Recently deleted", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            if (deletedContacts.isEmpty()) {
                Text(
                    text = "Nothing deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(deletedContacts, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerLow)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = OnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Button(
                                onClick = { onRestore(contact) },
                                enabled = !isBusy,
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlack,
                                    contentColor = OnPrimaryWhite
                                ),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text("Restore", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDangerSheet(
    title: String,
    message: String,
    cancelLabel: String,
    confirmLabel: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = OnSurface),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(cancelLabel, style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onConfirm,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(confirmLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
