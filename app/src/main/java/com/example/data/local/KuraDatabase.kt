package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.example.data.model.LedgerContact
import com.example.data.model.LedgerTransaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val loc: String,
    val type: String,
    val deleted: Boolean = false,
    val deletedAtMillis: Long? = null,
    val createdAtMillis: Long
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["contactId"])]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val amountCents: Long,
    val note: String,
    val tsMillis: Long,
    val voided: Boolean = false,
    val voidedAtMillis: Long? = null,
    val fixOfTxId: String? = null
)

fun ContactEntity.toModel(): LedgerContact = LedgerContact(
    id = id,
    name = name,
    phone = phone,
    loc = loc,
    type = type,
    deleted = deleted,
    deletedAtMillis = deletedAtMillis,
    createdAtMillis = createdAtMillis
)

fun TransactionEntity.toModel(): LedgerTransaction = LedgerTransaction(
    id = id,
    contactId = contactId,
    amountCents = amountCents,
    note = note,
    tsMillis = tsMillis,
    voided = voided,
    voidedAtMillis = voidedAtMillis,
    fixOfTxId = fixOfTxId
)

@Dao
interface KuraDao {
    @Query("SELECT * FROM contacts")
    fun observeContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM transactions")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("UPDATE contacts SET name = :name, phone = :phone, loc = :loc WHERE id = :id")
    suspend fun updateContactDetails(id: String, name: String, phone: String, loc: String)

    @Query("UPDATE contacts SET deleted = 1, deletedAtMillis = :deletedAtMillis WHERE id = :id")
    suspend fun softDeleteContact(id: String, deletedAtMillis: Long)

    @Query("UPDATE contacts SET deleted = 0, deletedAtMillis = NULL WHERE id = :id")
    suspend fun restoreContact(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(txs: List<TransactionEntity>)

    @Query("UPDATE transactions SET voided = 1, voidedAtMillis = :voidedAtMillis WHERE id = :txId")
    suspend fun voidTransaction(txId: String, voidedAtMillis: Long)

    @Transaction
    suspend fun correctTransaction(
        originalTxId: String,
        voidedAtMillis: Long,
        replacement: TransactionEntity
    ) {
        voidTransaction(originalTxId, voidedAtMillis)
        insertTransaction(replacement)
    }

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Transaction
    suspend fun eraseAll() {
        deleteAllTransactions()
        deleteAllContacts()
    }

    @Transaction
    suspend fun replaceAll(
        contacts: List<ContactEntity>,
        transactions: List<TransactionEntity>
    ) {
        deleteAllTransactions()
        deleteAllContacts()
        if (contacts.isNotEmpty()) insertContacts(contacts)
        if (transactions.isNotEmpty()) insertTransactions(transactions)
    }
}

@Database(
    entities = [ContactEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KuraDatabase : RoomDatabase() {
    abstract fun kuraDao(): KuraDao

    companion object {
        @Volatile
        private var INSTANCE: KuraDatabase? = null

        fun getInstance(context: Context): KuraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KuraDatabase::class.java,
                    "kura_ledger.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
