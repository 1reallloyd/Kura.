package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.KuraDatabase
import com.example.data.local.PinPreferencesStore
import com.example.data.model.LedgerTransaction
import com.example.data.model.TransactionKind
import com.example.data.model.UiState
import com.example.data.repository.KuraRepository
import com.example.ui.components.AddContactSheet
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.BackupExportSheet
import com.example.ui.components.ConfirmDangerSheet
import com.example.ui.components.EditContactSheet
import com.example.ui.components.EditProfileSheet
import com.example.ui.components.EditTransactionSheet
import com.example.ui.components.RecentlyDeletedSheet
import com.example.ui.components.RegionSheet
import com.example.ui.components.RestoreBackupSheet
import com.example.ui.components.WhatsAppReminderSheet
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.ContactsImportScreen
import com.example.ui.screens.MainLedgerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PinMode
import com.example.ui.screens.PinScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.ShopOwnerWelcomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.KuraTheme
import com.example.ui.theme.LedgerCanvas
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.PrimaryContainerDark
import com.example.ui.theme.Saffron
import com.example.ui.viewmodel.KuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KuraTheme {
                KuraAppRoot()
            }
        }
    }
}

private enum class OnboardingSetupStep { NONE, CONTACTS, PIN }

private sealed interface ActiveSheet {
    data object None : ActiveSheet
    data object AddContact : ActiveSheet
    data object EditContact : ActiveSheet
    data object DeleteContact : ActiveSheet
    data class AddTransaction(val kind: TransactionKind) : ActiveSheet
    data class EditTransaction(val tx: LedgerTransaction) : ActiveSheet
    data object WhatsAppReminder : ActiveSheet
    data object EditProfile : ActiveSheet
    data object Region : ActiveSheet
    data class BackupExport(val json: String) : ActiveSheet
    data object RestoreBackup : ActiveSheet
    data object RecentlyDeleted : ActiveSheet
    data object RemovePin : ActiveSheet
    data object LoadSample : ActiveSheet
    data object EraseAll : ActiveSheet
}

@Composable
private fun KuraAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pinStore = remember(context) { PinPreferencesStore(context.applicationContext) }

    val welcomeStorySeen by pinStore.welcomeStorySeenFlow.collectAsStateWithLifecycle(initialValue = null)
    val onboardingSeen by pinStore.onboardingSeenFlow.collectAsStateWithLifecycle(initialValue = null)

    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    val storySeen = welcomeStorySeen
    val slidesSeen = onboardingSeen
    if (storySeen == null || slidesSeen == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LedgerCanvas),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Saffron)
        }
        return
    }

    when {
        !storySeen -> {
            ShopOwnerWelcomeScreen(
                onContinue = {
                    scope.launch { pinStore.markWelcomeStorySeen() }
                },
                onSkip = {
                    scope.launch {
                        pinStore.markWelcomeStorySeen()
                        pinStore.markOnboardingSeen()
                    }
                }
            )
        }

        !slidesSeen -> {
            OnboardingScreen(
                onComplete = {
                    scope.launch { pinStore.markOnboardingSeen() }
                }
            )
        }

        else -> {
            KuraLedgerSession(pinStore = pinStore)
        }
    }
}

@Composable
private fun KuraLedgerSession(
    pinStore: PinPreferencesStore
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewModel: KuraViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val db = KuraDatabase.getInstance(app)
                val store = PinPreferencesStore(app)
                KuraViewModel(
                    repository = KuraRepository(db.kuraDao(), store),
                    pinStore = store
                )
            }
        }
    )

    val uiState by viewModel.ledgerState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedContactId by viewModel.selectedContactId.collectAsStateWithLifecycle()
    val pinUnlocked by viewModel.pinUnlocked.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    var setupStep by remember { mutableStateOf(OnboardingSetupStep.NONE) }
    var standaloneImportOpen by remember { mutableStateOf(false) }
    var standalonePinSetOpen by remember { mutableStateOf(false) }
    var activeSheet by remember { mutableStateOf<ActiveSheet>(ActiveSheet.None) }
    var toastText by remember { mutableStateOf<String?>(null) }
    var backgroundedAtMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(viewModel) {
        viewModel.toastMessages.collect { msg ->
            toastText = msg
        }
    }

    LaunchedEffect(toastText) {
        if (toastText != null) {
            delay(2200L)
            toastText = null
        }
    }

    DisposableEffect(lifecycleOwner, uiState) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    backgroundedAtMs = System.currentTimeMillis()
                }
                Lifecycle.Event.ON_START -> {
                    val data = (uiState as? UiState.Success)?.data
                    if (
                        data != null &&
                        data.pinLockState.hasPin &&
                        backgroundedAtMs > 0L &&
                        System.currentTimeMillis() - backgroundedAtMs > 30_000L
                    ) {
                        viewModel.markPinUnlocked(false)
                        activeSheet = ActiveSheet.None
                    }
                    backgroundedAtMs = 0L
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(LedgerCanvas),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Saffron)
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(LedgerCanvas).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Unable to load your ledger", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is UiState.Success -> {
                val data = state.data
                val profile = data.profile

                when {
                    setupStep == OnboardingSetupStep.CONTACTS -> {
                        ContactsImportScreen(
                            isSetupStep = true,
                            existingContacts = data.allRawContacts,
                            isBusy = isBusy,
                            onBack = { setupStep = OnboardingSetupStep.NONE },
                            onImportBatch = { picked, type, onRes ->
                                viewModel.importContacts(picked, data.allRawContacts, type, onRes)
                            },
                            onDone = { setupStep = OnboardingSetupStep.PIN }
                        )
                    }

                    setupStep == OnboardingSetupStep.PIN -> {
                        PinScreen(
                            initialMode = PinMode.SET,
                            userName = profile?.name.orEmpty(),
                            isSetupStep = true,
                            isBusy = isBusy,
                            onSetPin = { code ->
                                viewModel.setPin(code) { setupStep = OnboardingSetupStep.NONE }
                            },
                            onVerifyPin = { _, _ -> },
                            onSkipPin = {
                                viewModel.markPinUnlocked(true)
                                setupStep = OnboardingSetupStep.NONE
                            },
                            onForgotReset = {}
                        )
                    }

                    profile == null || !profile.onboarded -> {
                        ProfileSetupScreen(
                            existingProfile = profile,
                            defaultDisplayName = "",
                            isBusy = isBusy,
                            onContinue = { name, biz, phone, loc ->
                                viewModel.saveProfile(
                                    name = name,
                                    biz = biz,
                                    phone = phone,
                                    loc = loc,
                                    currency = profile?.currency ?: "GH₵",
                                    countryCode = profile?.countryCode ?: "233",
                                    onComplete = {
                                        viewModel.markPinUnlocked(true)
                                        setupStep = OnboardingSetupStep.CONTACTS
                                    }
                                )
                            }
                        )
                    }

                    data.pinLockState.hasPin && !pinUnlocked -> {
                        PinScreen(
                            initialMode = PinMode.LOCK,
                            userName = profile.name,
                            isSetupStep = false,
                            isBusy = isBusy,
                            onSetPin = {},
                            onVerifyPin = { candidate, onResult ->
                                viewModel.verifyPin(candidate, onResult)
                            },
                            onSkipPin = {},
                            onForgotReset = {
                                viewModel.resetForgotPin {}
                            }
                        )
                    }

                    standaloneImportOpen -> {
                        ContactsImportScreen(
                            isSetupStep = false,
                            existingContacts = data.allRawContacts,
                            isBusy = isBusy,
                            onBack = { standaloneImportOpen = false },
                            onImportBatch = { picked, type, onRes ->
                                viewModel.importContacts(picked, data.allRawContacts, type, onRes)
                            },
                            onDone = { standaloneImportOpen = false }
                        )
                    }

                    standalonePinSetOpen -> {
                        PinScreen(
                            initialMode = PinMode.SET,
                            userName = profile.name,
                            isSetupStep = false,
                            isBusy = isBusy,
                            onSetPin = { code ->
                                viewModel.setPin(code) { standalonePinSetOpen = false }
                            },
                            onVerifyPin = { _, _ -> },
                            onSkipPin = { standalonePinSetOpen = false },
                            onForgotReset = {},
                            onBack = { standalonePinSetOpen = false }
                        )
                    }

                    else -> {
                        val selectedSummary = data.activeContacts.firstOrNull { it.contact.id == selectedContactId }
                        if (selectedSummary != null) {
                            ContactDetailScreen(
                                summary = selectedSummary,
                                currency = profile.currency,
                                onBack = { viewModel.closeContact() },
                                onEditContact = { activeSheet = ActiveSheet.EditContact },
                                onDeleteContact = { activeSheet = ActiveSheet.DeleteContact },
                                onAddTransaction = { kind -> activeSheet = ActiveSheet.AddTransaction(kind) },
                                onEditTransaction = { tx -> activeSheet = ActiveSheet.EditTransaction(tx) },
                                onRemindWhatsApp = { activeSheet = ActiveSheet.WhatsAppReminder }
                            )
                        } else {
                            MainLedgerScreen(
                                data = data,
                                selectedTab = selectedTab,
                                filter = filter,
                                searchQuery = searchQuery,
                                onSelectTab = viewModel::selectTab,
                                onSelectFilter = viewModel::setFilter,
                                onSearchChange = viewModel::setSearchQuery,
                                onOpenContact = viewModel::openContact,
                                onAddContact = { activeSheet = ActiveSheet.AddContact },
                                onOpenImportContacts = { standaloneImportOpen = true },
                                onEditProfile = { activeSheet = ActiveSheet.EditProfile },
                                onTogglePin = {
                                    if (data.pinLockState.hasPin) {
                                        activeSheet = ActiveSheet.RemovePin
                                    } else {
                                        standalonePinSetOpen = true
                                    }
                                },
                                onBackup = {
                                    activeSheet = ActiveSheet.BackupExport(viewModel.buildBackupJson(data))
                                },
                                onRestoreBackup = { activeSheet = ActiveSheet.RestoreBackup },
                                onOpenRecentlyDeleted = { activeSheet = ActiveSheet.RecentlyDeleted },
                                onOpenRegion = { activeSheet = ActiveSheet.Region },
                                onLoadSampleData = { activeSheet = ActiveSheet.LoadSample },
                                onEraseAllData = { activeSheet = ActiveSheet.EraseAll }
                            )
                        }

                        when (val sheet = activeSheet) {
                            ActiveSheet.None -> Unit
                            ActiveSheet.AddContact -> {
                                AddContactSheet(
                                    existingContacts = data.allRawContacts,
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onSave = { name, phone, loc, type ->
                                        viewModel.addContact(name, phone, loc, type) { newId ->
                                            activeSheet = ActiveSheet.None
                                            viewModel.openContact(newId)
                                        }
                                    }
                                )
                            }
                            ActiveSheet.EditContact -> {
                                selectedSummary?.let { summary ->
                                    EditContactSheet(
                                        contact = summary.contact,
                                        existingContacts = data.allRawContacts,
                                        isBusy = isBusy,
                                        onDismiss = { activeSheet = ActiveSheet.None },
                                        onSave = { name, phone, loc ->
                                            viewModel.updateContact(summary.contact.id, name, phone, loc) {
                                                activeSheet = ActiveSheet.None
                                            }
                                        }
                                    )
                                }
                            }
                            ActiveSheet.DeleteContact -> {
                                selectedSummary?.let { summary ->
                                    ConfirmDangerSheet(
                                        title = "Delete ${summary.contact.name}?",
                                        message = "This hides the contact and its ${summary.transactions.size} records. You can restore it from Settings, under Recently deleted.",
                                        cancelLabel = "Keep",
                                        confirmLabel = "Delete",
                                        isBusy = isBusy,
                                        onDismiss = { activeSheet = ActiveSheet.None },
                                        onConfirm = {
                                            viewModel.softDeleteContact(summary.contact.id) {
                                                activeSheet = ActiveSheet.None
                                            }
                                        }
                                    )
                                }
                            }
                            is ActiveSheet.AddTransaction -> {
                                selectedSummary?.let { summary ->
                                    AddTransactionSheet(
                                        kind = sheet.kind,
                                        currentBalanceCents = summary.balanceCents,
                                        currency = profile.currency,
                                        isBusy = isBusy,
                                        onDismiss = { activeSheet = ActiveSheet.None },
                                        onSave = { signedCents, note ->
                                            viewModel.addTransaction(
                                                contactId = summary.contact.id,
                                                amountCents = signedCents,
                                                note = note,
                                                label = sheet.kind.label
                                            ) {
                                                activeSheet = ActiveSheet.None
                                            }
                                        }
                                    )
                                }
                            }
                            is ActiveSheet.EditTransaction -> {
                                EditTransactionSheet(
                                    transaction = sheet.tx,
                                    currency = profile.currency,
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onSaveFix = { signedCents, note ->
                                        viewModel.correctTransaction(sheet.tx, signedCents, note) {
                                            activeSheet = ActiveSheet.None
                                        }
                                    },
                                    onVoid = {
                                        viewModel.voidTransaction(sheet.tx.id) {
                                            activeSheet = ActiveSheet.None
                                        }
                                    }
                                )
                            }
                            ActiveSheet.WhatsAppReminder -> {
                                selectedSummary?.let { summary ->
                                    WhatsAppReminderSheet(
                                        contact = summary.contact,
                                        balanceCents = summary.balanceCents,
                                        bizName = profile.biz,
                                        currency = profile.currency,
                                        countryCode = profile.countryCode,
                                        existingContacts = data.allRawContacts,
                                        isBusy = isBusy,
                                        onDismiss = { activeSheet = ActiveSheet.None },
                                        onSavePhone = { newPhone ->
                                            viewModel.updateContact(
                                                summary.contact.id,
                                                summary.contact.name,
                                                newPhone,
                                                summary.contact.loc
                                            ) {}
                                        },
                                        onOpenWhatsApp = { url ->
                                            activeSheet = ActiveSheet.None
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            runCatching { context.startActivity(intent) }.onFailure {
                                                viewModel.emitToast("WhatsApp or browser not available")
                                            }
                                        },
                                        onCopyMessage = { msg ->
                                            copyToClipboard(context, "Kura reminder", msg)
                                            viewModel.emitToast("Message copied")
                                            activeSheet = ActiveSheet.None
                                        }
                                    )
                                }
                            }
                            ActiveSheet.EditProfile -> {
                                EditProfileSheet(
                                    profile = profile,
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onSave = { name, biz, phone, loc ->
                                        viewModel.saveProfile(
                                            name = name,
                                            biz = biz,
                                            phone = phone,
                                            loc = loc,
                                            currency = profile.currency,
                                            countryCode = profile.countryCode,
                                            onComplete = { activeSheet = ActiveSheet.None }
                                        )
                                    }
                                )
                            }
                            ActiveSheet.Region -> {
                                RegionSheet(
                                    currentCurrency = profile.currency,
                                    currentCountryCode = profile.countryCode,
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onSave = { cur, cc ->
                                        viewModel.updateRegion(cur, cc) {
                                            activeSheet = ActiveSheet.None
                                        }
                                    }
                                )
                            }
                            is ActiveSheet.BackupExport -> {
                                BackupExportSheet(
                                    backupJson = sheet.json,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onCopy = { json ->
                                        copyToClipboard(context, "Kura backup", json)
                                        viewModel.emitToast("Backup copied")
                                    },
                                    onShare = { json ->
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Kura Ledger Backup")
                                            putExtra(Intent.EXTRA_TEXT, json)
                                        }
                                        runCatching {
                                            context.startActivity(Intent.createChooser(share, "Save Kura backup"))
                                        }
                                    }
                                )
                            }
                            ActiveSheet.RestoreBackup -> {
                                RestoreBackupSheet(
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onRestore = { raw, onErr ->
                                        viewModel.restoreFromBackup(
                                            rawJson = raw,
                                            onSuccess = { activeSheet = ActiveSheet.None },
                                            onError = onErr
                                        )
                                    }
                                )
                            }
                            ActiveSheet.RecentlyDeleted -> {
                                RecentlyDeletedSheet(
                                    deletedContacts = data.deletedContacts,
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onRestore = { contact ->
                                        viewModel.restoreContact(contact.id, contact.name)
                                    }
                                )
                            }
                            ActiveSheet.RemovePin -> {
                                ConfirmDangerSheet(
                                    title = "Remove passcode lock?",
                                    message = "Anyone who opens this app on your phone will be able to see your ledger.",
                                    cancelLabel = "Keep passcode",
                                    confirmLabel = "Remove",
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onConfirm = {
                                        viewModel.removePin { activeSheet = ActiveSheet.None }
                                    }
                                )
                            }
                            ActiveSheet.LoadSample -> {
                                ConfirmDangerSheet(
                                    title = "Load sample data?",
                                    message = "This replaces your current contacts and records with sample ones. Back up first if your records are real.",
                                    cancelLabel = "Cancel",
                                    confirmLabel = "Replace",
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onConfirm = {
                                        viewModel.loadSampleData { activeSheet = ActiveSheet.None }
                                    }
                                )
                            }
                            ActiveSheet.EraseAll -> {
                                ConfirmDangerSheet(
                                    title = "Erase everything?",
                                    message = "All contacts and records in your ledger will be deleted. It can't be undone.",
                                    cancelLabel = "Cancel",
                                    confirmLabel = "Erase",
                                    isBusy = isBusy,
                                    onDismiss = { activeSheet = ActiveSheet.None },
                                    onConfirm = {
                                        viewModel.eraseAllData { activeSheet = ActiveSheet.None }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = toastText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryContainerDark,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = toastText.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
