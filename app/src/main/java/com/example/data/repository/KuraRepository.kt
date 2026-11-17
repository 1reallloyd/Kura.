package com.example.data.repository

import com.example.data.local.ContactEntity
import com.example.data.local.KuraDao
import com.example.data.local.PinPreferencesStore
import com.example.data.local.TransactionEntity
import com.example.data.local.toModel
import com.example.data.model.ImportResult
import com.example.data.model.LedgerContact
import com.example.data.model.LedgerTransaction
import com.example.data.model.PhonebookContact
import com.example.data.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class KuraRepository(
    private val dao: KuraDao,
    private val prefsStore: PinPreferencesStore
) {
    fun observeProfile(): Flow<UserProfile?> = prefsStore.profileFlow

    fun observeContacts(): Flow<List<LedgerContact>> =
        dao.observeContacts().map { list -> list.map { it.toModel() } }

    fun observeTransactions(): Flow<List<LedgerTransaction>> =
        dao.observeTransactions().map { list -> list.map { it.toModel() } }

    suspend fun saveProfile(
        name: String,
        biz: String,
        phone: String,
        loc: String,
        currency: String = "GH₵",
        countryCode: String = "233",
        onboarded: Boolean = true
    ): Result<Unit> = runCatchingSafe {
        val cleanName = name.trim().take(60)
        require(cleanName.isNotEmpty()) { "Enter your name" }
        prefsStore.saveProfile(
            name = cleanName,
            biz = biz.trim().take(80),
            phone = phone.trim().take(30),
            loc = loc.trim().take(80),
            currency = cleanCurrency(currency),
            countryCode = cleanCountryCode(countryCode),
            onboarded = onboarded
        )
    }

    suspend fun updateRegion(currency: String, countryCode: String): Result<Unit> = runCatchingSafe {
        prefsStore.updateRegion(cleanCurrency(currency), cleanCountryCode(countryCode))
    }

    suspend fun addContact(
        name: String,
        phone: String,
        loc: String,
        type: String
    ): Result<String> = runCatchingSafe {
        val cleanName = name.trim().take(60)
        require(cleanName.isNotEmpty()) { "Enter a name" }
        val cleanType = if (type == LedgerContact.TYPE_SUPPLIER) {
            LedgerContact.TYPE_SUPPLIER
        } else {
            LedgerContact.TYPE_CUSTOMER
        }
        val id = newId()
        dao.insertContact(
            ContactEntity(
                id = id,
                name = cleanName,
                phone = phone.trim().take(30),
                loc = loc.trim().take(80),
                type = cleanType,
                deleted = false,
                deletedAtMillis = null,
                createdAtMillis = System.currentTimeMillis()
            )
        )
        id
    }

    suspend fun updateContact(
        contactId: String,
        name: String,
        phone: String,
        loc: String
    ): Result<Unit> = runCatchingSafe {
        val cleanName = name.trim().take(60)
        require(cleanName.isNotEmpty()) { "Enter a name" }
        dao.updateContactDetails(contactId, cleanName, phone.trim().take(30), loc.trim().take(80))
    }

    suspend fun softDeleteContact(contactId: String): Result<Unit> = runCatchingSafe {
        dao.softDeleteContact(contactId, System.currentTimeMillis())
    }

    suspend fun restoreContact(contactId: String): Result<Unit> = runCatchingSafe {
        dao.restoreContact(contactId)
    }

    suspend fun importContacts(
        picked: List<PhonebookContact>,
        existingContacts: List<LedgerContact>,
        type: String
    ): Result<ImportResult> = runCatchingSafe {
        val cleanType = if (type == LedgerContact.TYPE_SUPPLIER) {
            LedgerContact.TYPE_SUPPLIER
        } else {
            LedgerContact.TYPE_CUSTOMER
        }
        val existingDigits = existingContacts
            .mapNotNull { normalizeLast9(it.phone) }
            .toMutableSet()

        var added = 0
        var duplicates = 0
        var skippedNoName = 0
        val toInsert = mutableListOf<ContactEntity>()
        val now = System.currentTimeMillis()

        picked.forEachIndexed { idx, item ->
            val cleanName = item.name.trim().take(60)
            if (cleanName.isEmpty()) {
                skippedNoName++
                return@forEachIndexed
            }
            val cleanPhone = item.phoneNumber.trim().take(30)
            val last9 = normalizeLast9(cleanPhone)
            if (last9 != null && !existingDigits.add(last9)) {
                duplicates++
                return@forEachIndexed
            }
            toInsert.add(
                ContactEntity(
                    id = newId(),
                    name = cleanName,
                    phone = cleanPhone,
                    loc = "",
                    type = cleanType,
                    deleted = false,
                    deletedAtMillis = null,
                    createdAtMillis = now + idx
                )
            )
            added++
        }

        if (toInsert.isNotEmpty()) {
            dao.insertContacts(toInsert)
        }
        ImportResult(added = added, duplicates = duplicates, skippedNoName = skippedNoName)
    }

    suspend fun addTransaction(
        contactId: String,
        amountCents: Long,
        note: String,
        tsMillis: Long = System.currentTimeMillis()
    ): Result<Unit> = runCatchingSafe {
        require(contactId.isNotBlank()) { "Contact ID must not be blank" }
        require(amountCents != 0L) { "Amount cannot be zero" }
        dao.insertTransaction(
            TransactionEntity(
                id = newId(),
                contactId = contactId,
                amountCents = amountCents,
                note = note.trim().take(120),
                tsMillis = tsMillis.coerceAtLeast(1L),
                voided = false,
                voidedAtMillis = null,
                fixOfTxId = null
            )
        )
    }

    suspend fun voidTransaction(txId: String): Result<Unit> = runCatchingSafe {
        dao.voidTransaction(txId, System.currentTimeMillis().coerceAtLeast(1L))
    }

    suspend fun correctTransaction(
        original: LedgerTransaction,
        newAmountCents: Long,
        newNote: String
    ): Result<Unit> = runCatchingSafe {
        require(newAmountCents != 0L) { "Amount cannot be zero" }
        val replacement = TransactionEntity(
            id = newId(),
            contactId = original.contactId,
            amountCents = newAmountCents,
            note = newNote.trim().take(120),
            tsMillis = original.tsMillis.coerceAtLeast(1L),
            voided = false,
            voidedAtMillis = null,
            fixOfTxId = original.id
        )
        dao.correctTransaction(
            originalTxId = original.id,
            voidedAtMillis = System.currentTimeMillis().coerceAtLeast(1L),
            replacement = replacement
        )
    }

    suspend fun loadSampleData(): Result<Unit> = runCatchingSafe {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        data class SeedTx(val amountCents: Long, val note: String, val daysAgo: Int)
        data class SeedContact(val name: String, val phone: String, val type: String, val txs: List<SeedTx>)

        val seeds = listOf(
            SeedContact("Ama Serwaa", "0244123456", LedgerContact.TYPE_CUSTOMER, listOf(SeedTx(42000L, "Rice and oil", 2))),
            SeedContact("Kojo Mensah", "0200111222", LedgerContact.TYPE_CUSTOMER, listOf(SeedTx(100000L, "Weekly provisions", 6), SeedTx(-15000L, "Part payment", 0))),
            SeedContact("Efua Boateng", "0551234567", LedgerContact.TYPE_CUSTOMER, listOf(SeedTx(30000L, "Tomatoes and onions", 5))),
            SeedContact("Wholesale Depot", "0302123456", LedgerContact.TYPE_SUPPLIER, listOf(SeedTx(-70000L, "Stock on credit", 1))),
            SeedContact("Yaw Owusu", "0277654321", LedgerContact.TYPE_CUSTOMER, listOf(SeedTx(18500L, "Soap and sugar", 7)))
        )

        val contactEntities = mutableListOf<ContactEntity>()
        val txEntities = mutableListOf<TransactionEntity>()
        seeds.forEachIndexed { idx, seed ->
            val cid = newId()
            contactEntities.add(
                ContactEntity(
                    id = cid,
                    name = seed.name,
                    phone = seed.phone,
                    loc = "",
                    type = seed.type,
                    deleted = false,
                    deletedAtMillis = null,
                    createdAtMillis = now - (10 - idx) * 1000L
                )
            )
            seed.txs.forEach { t ->
                txEntities.add(
                    TransactionEntity(
                        id = newId(),
                        contactId = cid,
                        amountCents = t.amountCents,
                        note = t.note,
                        tsMillis = (now - t.daysAgo * day).coerceAtLeast(1L),
                        voided = false,
                        voidedAtMillis = null,
                        fixOfTxId = null
                    )
                )
            }
        }
        dao.replaceAll(contactEntities, txEntities)
    }

    suspend fun eraseAllData(): Result<Unit> = runCatchingSafe {
        dao.eraseAll()
    }

    fun exportBackupJson(
        profile: UserProfile?,
        contacts: List<LedgerContact>,
        transactions: List<LedgerTransaction>
    ): String {
        val txByContact = transactions.groupBy { it.contactId }
        fun serializeContact(c: LedgerContact): JSONObject {
            val txsJson = JSONArray()
            txByContact[c.id].orEmpty().forEach { tx ->
                txsJson.put(
                    JSONObject().apply {
                        put("id", tx.id)
                        put("amt", tx.amountCents)
                        put("note", tx.note)
                        put("ts", tx.tsMillis)
                        if (tx.voided) put("void", tx.voidedAtMillis ?: System.currentTimeMillis())
                        if (!tx.fixOfTxId.isNullOrBlank()) put("fix", tx.fixOfTxId)
                    }
                )
            }
            return JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("loc", c.loc)
                put("type", c.type)
                put("created", c.createdAtMillis)
                if (c.deleted) put("del", c.deletedAtMillis ?: System.currentTimeMillis())
                put("tx", txsJson)
            }
        }

        val activeArray = JSONArray()
        val trashArray = JSONArray()
        contacts.forEach { c ->
            if (c.deleted) trashArray.put(serializeContact(c))
            else activeArray.put(serializeContact(c))
        }

        return JSONObject().apply {
            put("v", 2)
            put("list", activeArray)
            put("trash", trashArray)
            put("user", profile?.name.orEmpty())
            put("biz", profile?.biz.orEmpty())
            put("phone", profile?.phone.orEmpty())
            put("loc", profile?.loc.orEmpty())
            put("cur", profile?.currency ?: "GH₵")
            put("cc", profile?.countryCode ?: "233")
        }.toString()
    }

    suspend fun restoreFromBackupJson(rawJson: String): Result<Unit> = runCatchingSafe {
        val root = JSONObject(rawJson)
        require(root.optInt("v", 0) == 2 && root.has("list")) {
            "That isn't a valid Kura backup"
        }

        val contactEntities = mutableListOf<ContactEntity>()
        val txEntities = mutableListOf<TransactionEntity>()

        fun collectList(array: JSONArray?, deleted: Boolean) {
            if (array == null) return
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val name = item.optString("name", "").trim().take(60)
                if (name.isEmpty()) continue
                val cid = newId()
                val type = if (item.optString("type") == LedgerContact.TYPE_SUPPLIER) {
                    LedgerContact.TYPE_SUPPLIER
                } else {
                    LedgerContact.TYPE_CUSTOMER
                }
                contactEntities.add(
                    ContactEntity(
                        id = cid,
                        name = name,
                        phone = item.optString("phone", "").trim().take(30),
                        loc = item.optString("loc", "").trim().take(80),
                        type = type,
                        deleted = deleted,
                        deletedAtMillis = if (deleted) item.optLong("del", System.currentTimeMillis()).coerceAtLeast(0L) else null,
                        createdAtMillis = item.optLong("created", System.currentTimeMillis()).coerceAtLeast(0L)
                    )
                )

                val txs = item.optJSONArray("tx") ?: continue
                for (j in 0 until txs.length()) {
                    val tObj = txs.optJSONObject(j) ?: continue
                    val amt = tObj.optLong("amt", 0L)
                    val ts = tObj.optLong("ts", 0L)
                    if (amt == 0L || ts <= 0L) continue
                    val voidTs = tObj.optLong("void", 0L)
                    val fix = tObj.optString("fix", "").trim().takeIf { it.isNotEmpty() }
                    txEntities.add(
                        TransactionEntity(
                            id = newId(),
                            contactId = cid,
                            amountCents = amt,
                            note = tObj.optString("note", "").trim().take(120),
                            tsMillis = ts,
                            voided = voidTs > 0L,
                            voidedAtMillis = if (voidTs > 0L) voidTs else null,
                            fixOfTxId = fix
                        )
                    )
                }
            }
        }

        collectList(root.optJSONArray("list"), deleted = false)
        collectList(root.optJSONArray("trash"), deleted = true)

        dao.replaceAll(contactEntities, txEntities)

        val restoredName = root.optString("user", "").trim().take(60)
        if (restoredName.isNotEmpty()) {
            prefsStore.saveProfile(
                name = restoredName,
                biz = root.optString("biz", "").trim().take(80),
                phone = root.optString("phone", "").trim().take(30),
                loc = root.optString("loc", "").trim().take(80),
                currency = cleanCurrency(root.optString("cur", "GH₵")),
                countryCode = cleanCountryCode(root.optString("cc", "233")),
                onboarded = true
            )
        }
    }

    private suspend inline fun <T> runCatchingSafe(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun newId(): String = UUID.randomUUID().toString().replace("-", "").take(16)

    companion object {
        fun normalizeLast9(phone: String?): String? {
            val digits = phone.orEmpty().filter { it.isDigit() }
            return if (digits.length >= 9) digits.takeLast(9) else null
        }

        fun cleanCurrency(raw: String?): String {
            val cleaned = raw.orEmpty()
                .replace(Regex("[<>&\"'`\\\\]"), "")
                .trim()
                .take(5)
            return cleaned.ifEmpty { "GH₵" }
        }

        fun cleanCountryCode(raw: String?): String {
            val digits = raw.orEmpty().filter { it.isDigit() }.take(4)
            return digits.ifEmpty { "233" }
        }
    }
}
