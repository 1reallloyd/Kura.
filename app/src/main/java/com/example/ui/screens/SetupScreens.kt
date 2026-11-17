package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.PhonebookReader
import com.example.data.local.PinVerifyResult
import com.example.data.model.ImportResult
import com.example.data.model.LedgerContact
import com.example.data.model.PhonebookContact
import com.example.data.model.UserProfile
import com.example.ui.components.ConfirmDangerSheet
import com.example.ui.components.KuraFormatters
import com.example.ui.components.KuraTextField
import com.example.ui.components.PhonebookPickerSheet
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimaryWhite
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.OnTertiaryFixedVariant
import com.example.ui.theme.Outline
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.TertiaryFixed
import com.example.ui.theme.WarmAmberBg
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
private fun StepBadgeIcon(icon: ImageVector) {
    Box(modifier = Modifier.size(64.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = OnSurface, modifier = Modifier.size(32.dp))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 3.dp, y = (-3).dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(SecondaryContainer)
        )
    }
}

@Composable
fun ProfileSetupScreen(
    existingProfile: UserProfile?,
    defaultDisplayName: String,
    isBusy: Boolean,
    onContinue: (name: String, biz: String, phone: String, loc: String) -> Unit
) {
    var name by remember(existingProfile, defaultDisplayName) {
        mutableStateOf(existingProfile?.name?.ifBlank { defaultDisplayName } ?: defaultDisplayName)
    }
    var biz by remember(existingProfile) { mutableStateOf(existingProfile?.biz.orEmpty()) }
    var phone by remember(existingProfile) { mutableStateOf(existingProfile?.phone.orEmpty()) }
    var loc by remember(existingProfile) { mutableStateOf(existingProfile?.loc.orEmpty()) }
    var nameErr by remember { mutableStateOf<String?>(null) }
    var phoneErr by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxSize()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text("Step 1 of 3", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepBadgeIcon(Icons.Default.Storefront)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Set up your profile", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tell us who's running this ledger. You can change this later in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 300.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                KuraTextField(
                    value = name,
                    onValueChange = { name = it.take(60); nameErr = null },
                    label = "Your name",
                    placeholder = "e.g. Kwame Ansah",
                    errorText = nameErr,
                    testTag = "profile_name_input"
                )
                KuraTextField(
                    value = biz,
                    onValueChange = { biz = it.take(80) },
                    label = "Business name (optional)",
                    placeholder = "e.g. Kwame's Provisions",
                    testTag = "profile_biz_input"
                )
                KuraTextField(
                    value = phone,
                    onValueChange = { phone = it.take(30); phoneErr = null },
                    label = "Phone number (optional)",
                    placeholder = "e.g. 024 123 4567",
                    errorText = phoneErr,
                    keyboardType = KeyboardType.Phone
                )
                KuraTextField(
                    value = loc,
                    onValueChange = { loc = it.take(80) },
                    label = "Business location (optional)",
                    placeholder = "e.g. Madina market, Accra"
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isEmpty()) {
                        nameErr = "Enter your name to continue"
                        return@Button
                    }
                    val cleanPhone = phone.trim()
                    if (cleanPhone.isNotEmpty() && !KuraFormatters.isValidPhone(cleanPhone)) {
                        phoneErr = "Enter a valid phone number, or leave it empty"
                        return@Button
                    }
                    onContinue(cleanName, biz.trim(), cleanPhone, loc.trim())
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_continue_button")
            ) {
                Text("Continue", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ContactsImportScreen(
    isSetupStep: Boolean,
    existingContacts: List<LedgerContact>,
    isBusy: Boolean,
    onBack: () -> Unit,
    onImportBatch: (picked: List<PhonebookContact>, type: String, onResult: (ImportResult) -> Unit) -> Unit,
    onDone: () -> Unit
) {
    if (!isSetupStep) {
        BackHandler(onBack = onDone)
    } else {
        BackHandler(onBack = onBack)
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var targetType by remember { mutableStateOf(LedgerContact.TYPE_CUSTOMER) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var phonebookList by remember { mutableStateOf<List<PhonebookContact>?>(null) }
    var permissionDeniedNote by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadPhonebookAndOpenSheet() {
        scope.launch {
            try {
                errorMessage = null
                permissionDeniedNote = false
                phonebookList = PhonebookReader.readPhonebookContacts(context)
            } catch (e: Exception) {
                errorMessage = "Couldn't read your phonebook contacts. Try picking one contact below."
            }
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            loadPhonebookAndOpenSheet()
        } else {
            permissionDeniedNote = true
        }
    }

    val singleContactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val picked = PhonebookReader.resolvePickedContactUri(context, uri)
                if (picked == null) {
                    errorMessage = "That contact has no name to import."
                } else {
                    onImportBatch(listOf(picked), targetType) { res ->
                        importResult = res
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxSize()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = if (isSetupStep) onBack else onDone,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("contacts_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                if (isSetupStep) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text("Step 2 of 3", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepBadgeIcon(Icons.Default.Contacts)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Bring in your contacts", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pick the people you already trade with from your phonebook so you don't type them in one by one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 320.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    LedgerContact.TYPE_CUSTOMER to "As customers",
                    LedgerContact.TYPE_SUPPLIER to "As suppliers"
                ).forEach { (key, label) ->
                    val active = targetType == key
                    Button(
                        onClick = { targetType = key },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) PrimaryBlack else SurfaceContainerLow,
                            contentColor = if (active) OnPrimaryWhite else OnSurface
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        loadPhonebookAndOpenSheet()
                    } else {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("choose_phonebook_contacts_button")
            ) {
                Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose from my phonebook", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Allow contacts permission to open your phonebook and pick multiple people at once.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { singleContactPickerLauncher.launch(null) },
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = OnSurface),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pick_single_phonebook_button")
            ) {
                Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick a contact from phonebook", style = MaterialTheme.typography.labelLarge)
            }

            if (permissionDeniedNote) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WarmAmberBg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Phonebook permission was not granted. You can still pick contacts one by one above, or enable Contacts access in App Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface
                    )
                    TextButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text("Open App Settings", style = MaterialTheme.typography.labelLarge, color = PrimaryBlack)
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ErrorContainer)
                        .padding(14.dp)
                ) {
                    Text(errorMessage!!, style = MaterialTheme.typography.bodyMedium, color = OnErrorContainer)
                }
            }

            if (importResult != null) {
                val res = importResult!!
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TertiaryFixed)
                        .padding(14.dp)
                ) {
                    val summary = buildString {
                        if (res.added > 0) {
                            append("${res.added} ${if (res.added == 1) "contact" else "contacts"} added.")
                        } else {
                            append("Nothing new to add.")
                        }
                        if (res.duplicates > 0) append(" ${res.duplicates} already in Kura.")
                        if (res.skippedNoName > 0) append(" ${res.skippedNoName} skipped (no name).")
                    }
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = OnTertiaryFixedVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (importResult != null && importResult!!.added > 0) {
                Button(
                    onClick = onDone,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = OnPrimaryWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("contacts_continue_button")
                ) {
                    Text(if (isSetupStep) "Continue" else "Done", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            } else {
                TextButton(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("contacts_skip_button")
                ) {
                    Text(
                        text = if (isSetupStep) "Skip for now" else "Done",
                        style = MaterialTheme.typography.labelLarge,
                        color = OnSurfaceVariant
                    )
                }
                if (isSetupStep) {
                    Text(
                        text = "You can import contacts any time from Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (phonebookList != null) {
        PhonebookPickerSheet(
            contacts = phonebookList!!,
            targetTypeLabel = if (targetType == LedgerContact.TYPE_SUPPLIER) "suppliers" else "customers",
            isBusy = isBusy,
            onDismiss = { phonebookList = null },
            onImportSelected = { selected ->
                onImportBatch(selected, targetType) { res ->
                    importResult = res
                    phonebookList = null
                }
            }
        )
    }
}

enum class PinMode { SET, CONFIRM, LOCK }

@Composable
fun PinScreen(
    initialMode: PinMode,
    userName: String,
    isSetupStep: Boolean,
    isBusy: Boolean,
    onSetPin: (String) -> Unit,
    onVerifyPin: (candidate: String, onResult: (PinVerifyResult) -> Unit) -> Unit,
    onSkipPin: () -> Unit,
    onForgotReset: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    if (onBack != null) {
        BackHandler(onBack = onBack)
    }

    var mode by remember(initialMode) { mutableStateOf(initialMode) }
    var buffer by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun triggerShake(msg: String) {
        isError = true
        message = msg
        scope.launch {
            shakeOffset.animateTo(-14f, tween(60))
            shakeOffset.animateTo(14f, tween(70))
            shakeOffset.animateTo(-8f, tween(60))
            shakeOffset.animateTo(0f, tween(60))
            delay(120L)
            buffer = ""
        }
    }

    fun onFourDigitsEntered(code: String) {
        when (mode) {
            PinMode.SET -> {
                firstPin = code
                buffer = ""
                message = ""
                isError = false
                mode = PinMode.CONFIRM
            }
            PinMode.CONFIRM -> {
                if (code == firstPin) {
                    onSetPin(code)
                } else {
                    mode = PinMode.SET
                    firstPin = ""
                    triggerShake("Codes don't match. Start again.")
                }
            }
            PinMode.LOCK -> {
                onVerifyPin(code) { res ->
                    when (res) {
                        PinVerifyResult.Success -> Unit
                        is PinVerifyResult.WrongPin -> triggerShake("Wrong passcode. ${res.triesLeft} tries left.")
                        is PinVerifyResult.Locked -> triggerShake("Too many tries. Wait ${res.waitSeconds} seconds.")
                    }
                }
            }
        }
    }

    LaunchedEffect(buffer) {
        if (buffer.length == 4) {
            delay(160L)
            onFourDigitsEntered(buffer)
        }
    }

    val title = when (mode) {
        PinMode.SET -> "Protect your ledger"
        PinMode.CONFIRM -> "Confirm your passcode"
        PinMode.LOCK -> "Welcome back, $userName"
    }
    val subtitle = when (mode) {
        PinMode.SET -> "Set a 4-digit passcode to lock your records on this device."
        PinMode.CONFIRM -> "Enter the same 4 digits again."
        PinMode.LOCK -> "Enter your passcode to open your ledger."
    }

    val keypadRows = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxSize()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    mode == PinMode.CONFIRM -> {
                        IconButton(onClick = {
                            mode = PinMode.SET
                            buffer = ""
                            message = ""
                            isError = false
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    onBack != null -> {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    else -> Spacer(modifier = Modifier.size(48.dp))
                }

                if (mode != PinMode.LOCK && isSetupStep) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text("Step 3 of 3", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            StepBadgeIcon(Icons.Default.Pin)
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge, color = OnSurface, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 290.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { idx ->
                    val filled = idx < buffer.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (filled) PrimaryBlack else SurfaceContainerHighest)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message.ifEmpty { " " },
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) ErrorRed else OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.widthIn(max = 290.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { (digit, letters) ->
                            PinKeyButton(digit = digit, letters = letters) {
                                if (buffer.length < 4) {
                                    buffer += digit
                                    message = ""
                                    isError = false
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(72.dp))
                    PinKeyButton(digit = "0", letters = "") {
                        if (buffer.length < 4) {
                            buffer += "0"
                            message = ""
                            isError = false
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (buffer.isNotEmpty()) buffer = buffer.dropLast(1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete digit",
                            tint = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (mode == PinMode.SET) {
                TextButton(
                    onClick = onSkipPin,
                    modifier = Modifier.height(48.dp).testTag("skip_pin_button")
                ) {
                    Text("Skip for now", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                }
            } else if (mode == PinMode.LOCK) {
                TextButton(
                    onClick = { showForgotDialog = true },
                    modifier = Modifier.height(48.dp).testTag("forgot_pin_button")
                ) {
                    Text("Forgot passcode?", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                }
            }
        }
    }

    if (showForgotDialog) {
        ConfirmDangerSheet(
            title = "Forgot your passcode?",
            message = "A passcode can't be recovered. You can erase everything in your ledger and start fresh, then restore from a backup if you made one.",
            cancelLabel = "Cancel",
            confirmLabel = "Erase and reset",
            isBusy = isBusy,
            onDismiss = { showForgotDialog = false },
            onConfirm = {
                showForgotDialog = false
                onForgotReset()
            }
        )
    }
}

@Composable
private fun PinKeyButton(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = SurfaceContainerLow,
        modifier = Modifier
            .size(72.dp)
            .testTag("pin_key_$digit")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                style = MaterialTheme.typography.headlineLarge,
                color = OnSurface
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                    color = OnSurfaceVariant.copy(alpha = 0.65f)
                )
            }
        }
    }
}
