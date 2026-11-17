package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PinLockState
import com.example.data.local.PinPreferencesStore
import com.example.data.local.PinVerifyResult
import com.example.data.model.ContactFilter
import com.example.data.model.ContactWithSummary
import com.example.data.model.ImportResult
import com.example.data.model.LedgerContact
import com.example.data.model.LedgerTotals
import com.example.data.model.LedgerTransaction
import com.example.data.model.MainTab
import com.example.data.model.PhonebookContact
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.repository.KuraRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

data class KuraLedgerData(
    val profile: UserProfile?,
    val activeContacts: List<ContactWithSummary>,
    val deletedContacts: List<LedgerContact>,
    val allRawContacts: List<LedgerContact>,
    val allTransactions: List<LedgerTransaction>,
    val totals: LedgerTotals,
    val overdueCustomers: List<ContactWithSummary>,
    val overdueTotalCents: Long,
    val last7DaysGivenCents: Long,
    val last7DaysCollectedCents: Long,
    val topDebtors: List<ContactWithSummary>,
    val pinLockState: PinLockState
)

class KuraViewModel(
    private val repository: KuraRepository,
    private val pinStore: PinPreferencesStore
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainTab.CUSTOMERS)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _filter = MutableStateFlow(ContactFilter.ALL)
    val filter: StateFlow<ContactFilter> = _filter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedContactId = MutableStateFlow<String?>(null)
    val selectedContactId: StateFlow<String?> = _selectedContactId.asStateFlow()

    private val _pinUnlocked = MutableStateFlow(false)
    val pinUnlocked: StateFlow<Boolean> = _pinUnlocked.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    val ledgerState: StateFlow<UiState<KuraLedgerData>> = combine(
        repository.observeProfile(),
        repository.observeContacts(),
        repository.observeTransactions(),
        pinStore.pinStateFlow
    ) { profile, contacts, transactions, pinState ->
        val txByContact = transactions.groupBy { it.contactId }
        val now = System.currentTimeMillis()
        val weekAgo = now - 7L * 86_400_000L

        val active = mutableListOf<ContactWithSummary>()
        val deleted = mutableListOf<LedgerContact>()
        for (c in contacts) {
            if (c.deleted) {
                deleted.add(c)
            } else {
                val list = txByContact[c.id].orEmpty().sortedByDescending { it.tsMillis }
                var bal = 0L
                var lastAct = 0L
                for (tx in list) {
                    if (!tx.voided) {
                        bal += tx.amountCents
                        if (tx.tsMillis > lastAct) lastAct = tx.tsMillis
                    }
                }
                active.add(
                    ContactWithSummary(
                        contact = c,
                        balanceCents = bal,
                        lastActivityMs = lastAct,
                        sortTimestampMs = max(lastAct, c.createdAtMillis),
                        transactions = list
                    )
                )
            }
        }
        val sortedActive = active.sortedByDescending { it.sortTimestampMs }
        val sortedDeleted = deleted.sortedByDescending { it.deletedAtMillis ?: 0L }

        var owed = 0L
        var youOwe = 0L
        var owedCount = 0
        var oweCount = 0
        for (item in sortedActive) {
            if (item.balanceCents > 0L) {
                owed += item.balanceCents
                owedCount++
            } else if (item.balanceCents < 0L) {
                youOwe -= item.balanceCents
                oweCount++
            }
        }

        val overdue = sortedActive.filter {
            !it.contact.isSupplier &&
                it.balanceCents > 0L &&
                it.lastActivityMs > 0L &&
                (now - it.lastActivityMs) > 7L * 86_400_000L
        }
        val overdueSum = overdue.sumOf { it.balanceCents }

        val customerIds = sortedActive.filter { !it.contact.isSupplier }.map { it.contact.id }.toHashSet()
        var given7d = 0L
        var collected7d = 0L
        for (tx in transactions) {
            if (!tx.voided && tx.tsMillis >= weekAgo && tx.contactId in customerIds) {
                if (tx.amountCents > 0L) given7d += tx.amountCents
                else collected7d -= tx.amountCents
            }
        }

        val topDebtors = sortedActive
            .filter { !it.contact.isSupplier && it.balanceCents > 0L }
            .sortedByDescending { it.balanceCents }
            .take(5)

        UiState.Success(
            KuraLedgerData(
                profile = profile,
                activeContacts = sortedActive,
                deletedContacts = sortedDeleted,
                allRawContacts = contacts,
                allTransactions = transactions,
                totals = LedgerTotals(
                    owedToYouCents = owed,
                    youOweCents = youOwe,
                    owedCount = owedCount,
                    oweCount = oweCount
                ),
                overdueCustomers = overdue,
                overdueTotalCents = overdueSum,
                last7DaysGivenCents = given7d,
                last7DaysCollectedCents = collected7d,
                topDebtors = topDebtors,
                pinLockState = pinState
            )
        ) as UiState<KuraLedgerData>
    }.catch { error ->
        if (error is CancellationException) throw error
        Log.w(TAG, "Error observing ledger state", error)
        emit(UiState.Error(error.localizedMessage ?: "Failed to load ledger"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading
    )

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
        if (tab != MainTab.CUSTOMERS) {
            _searchQuery.value = ""
        }
    }

    fun setFilter(filter: ContactFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openContact(contactId: String) {
        _selectedContactId.value = contactId
    }

    fun closeContact() {
        _selectedContactId.value = null
    }

    fun markPinUnlocked(unlocked: Boolean) {
        _pinUnlocked.value = unlocked
    }

    fun saveProfile(
        name: String,
        biz: String,
        phone: String,
        loc: String,
        currency: String,
        countryCode: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveProfile(
                name = name,
                biz = biz,
                phone = phone,
                loc = loc,
                currency = currency,
                countryCode = countryCode,
                onboarded = true
            )
            _isBusy.value = false
            result.onSuccess {
                _toastMessages.tryEmit("Profile saved")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't save profile")
            }
        }
    }

    fun updateRegion(currency: String, countryCode: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateRegion(currency, countryCode)
            _isBusy.value = false
            result.onSuccess {
                _toastMessages.tryEmit("Saved")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Failed to update settings")
            }
        }
    }

    fun addContact(
        name: String,
        phone: String,
        loc: String,
        type: String,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.addContact(name, phone, loc, type)
            _isBusy.value = false
            result.onSuccess { newId ->
                _toastMessages.tryEmit("${name.trim()} added")
                onCreated(newId)
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't add contact")
            }
        }
    }

    fun updateContact(
        contactId: String,
        name: String,
        phone: String,
        loc: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateContact(contactId, name, phone, loc)
            _isBusy.value = false
            result.onSuccess {
                _toastMessages.tryEmit("Details saved")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't update details")
            }
        }
    }

    fun softDeleteContact(contactId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.softDeleteContact(contactId)
            _isBusy.value = false
            result.onSuccess {
                _selectedContactId.value = null
                _toastMessages.tryEmit("Moved to Recently deleted")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't delete contact")
            }
        }
    }

    fun restoreContact(contactId: String, contactName: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.restoreContact(contactId)
            _isBusy.value = false
            result.onSuccess {
                _toastMessages.tryEmit("$contactName restored")
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't restore contact")
            }
        }
    }

    fun importContacts(
        picked: List<PhonebookContact>,
        existingContacts: List<LedgerContact>,
        type: String,
        onResult: (ImportResult) -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.importContacts(picked, existingContacts, type)
            _isBusy.value = false
            result.onSuccess { summary ->
                if (summary.added > 0) {
                    _toastMessages.tryEmit("${summary.added} ${if (summary.added == 1) "contact" else "contacts"} imported")
                }
                onResult(summary)
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Failed to import contacts")
            }
        }
    }

    fun addTransaction(
        contactId: String,
        amountCents: Long,
        note: String,
        label: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.addTransaction(contactId, amountCents, note)
            _isBusy.value = false
            result.onSuccess {
                _toastMessages.tryEmit("$label saved")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't save entry")
            }
        }
    }

    fun voidTransaction(txId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.voidTransaction(txId)
            _isBusy.value = false
            result.onSuccess {
                _toastMessages.tryEmit("Entry voided")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't void entry")
            }
        }
    }

    fun correctTransaction(
        original: LedgerTransaction,
        newAmountCents: Long,
        newNote: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.correctTransaction(original, newAmountCents, newNote)
            _isBusy.value = false
            result.onSuccess {
                _toastMessages.tryEmit("Entry corrected")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't save correction")
            }
        }
    }

    fun loadSampleData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.loadSampleData()
            _isBusy.value = false
            result.onSuccess {
                _selectedContactId.value = null
                _toastMessages.tryEmit("Sample data loaded")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't load sample data")
            }
        }
    }

    fun eraseAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.eraseAllData()
            _isBusy.value = false
            result.onSuccess {
                _selectedContactId.value = null
                _toastMessages.tryEmit("Everything erased")
                onComplete()
            }.onFailure { err ->
                _toastMessages.tryEmit(err.localizedMessage ?: "Couldn't erase data")
            }
        }
    }

    fun buildBackupJson(data: KuraLedgerData): String =
        repository.exportBackupJson(data.profile, data.allRawContacts, data.allTransactions)

    fun restoreFromBackup(rawJson: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.restoreFromBackupJson(rawJson)
            _isBusy.value = false
            result.onSuccess {
                _selectedContactId.value = null
                _toastMessages.tryEmit("Backup restored")
                onSuccess()
            }.onFailure { err ->
                onError(err.localizedMessage ?: "That isn't a valid Kura backup")
            }
        }
    }

    fun setPin(pin: String, onDone: () -> Unit) {
        viewModelScope.launch {
            pinStore.setPin(pin)
            _pinUnlocked.value = true
            _toastMessages.tryEmit("Passcode set. Your ledger is locked.")
            onDone()
        }
    }

    fun removePin(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            pinStore.clearPin()
            _pinUnlocked.value = true
            _toastMessages.tryEmit("Passcode removed")
            onDone()
        }
    }

    fun verifyPin(candidate: String, onResult: (PinVerifyResult) -> Unit) {
        viewModelScope.launch {
            val res = pinStore.verifyPin(candidate)
            if (res is PinVerifyResult.Success) {
                _pinUnlocked.value = true
            }
            onResult(res)
        }
    }

    fun resetForgotPin(onDone: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.eraseAllData()
            pinStore.clearPin()
            _pinUnlocked.value = true
            _selectedContactId.value = null
            _isBusy.value = false
            _toastMessages.tryEmit("Data erased and passcode removed")
            onDone()
        }
    }

    fun emitToast(message: String) {
        _toastMessages.tryEmit(message)
    }

    private companion object {
        const val TAG = "KuraVM"
    }
}
