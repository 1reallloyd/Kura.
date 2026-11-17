package com.example.data.model

data class UserProfile(
    val name: String = "",
    val biz: String = "",
    val phone: String = "",
    val loc: String = "",
    val currency: String = "GH₵",
    val countryCode: String = "233",
    val onboarded: Boolean = false
)

data class LedgerContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val loc: String = "",
    val type: String = TYPE_CUSTOMER,
    val deleted: Boolean = false,
    val deletedAtMillis: Long? = null,
    val createdAtMillis: Long = 0L
) {
    val isSupplier: Boolean
        get() = type == TYPE_SUPPLIER

    companion object {
        const val TYPE_CUSTOMER = "customer"
        const val TYPE_SUPPLIER = "supplier"
    }
}

data class LedgerTransaction(
    val id: String = "",
    val contactId: String = "",
    val amountCents: Long = 0L,
    val note: String = "",
    val tsMillis: Long = 0L,
    val voided: Boolean = false,
    val voidedAtMillis: Long? = null,
    val fixOfTxId: String? = null
)

data class ContactWithSummary(
    val contact: LedgerContact,
    val balanceCents: Long,
    val lastActivityMs: Long,
    val sortTimestampMs: Long,
    val transactions: List<LedgerTransaction>
)

data class LedgerTotals(
    val owedToYouCents: Long,
    val youOweCents: Long,
    val owedCount: Int,
    val oweCount: Int
) {
    val netBalanceCents: Long get() = owedToYouCents - youOweCents
}

data class PhonebookContact(
    val lookupKey: String,
    val name: String,
    val phoneNumber: String
)

data class ImportResult(
    val added: Int,
    val duplicates: Int,
    val skippedNoName: Int
)

enum class ContactFilter { ALL, OWE_ME, I_OWE }

enum class MainTab { CUSTOMERS, SUMMARY, SETTINGS }

enum class TransactionKind(
    val label: String,
    val sign: Int,
    val noteHint: String
) {
    GIVE("Gave credit", 1, "What did they take?"),
    GET("Got payment", -1, "Note (optional)"),
    BUY("Bought on credit", -1, "What did you buy?"),
    PAY("I paid", 1, "Note (optional)")
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
