package com.example.data.local

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.example.data.model.PhonebookContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PhonebookReader {

    suspend fun readPhonebookContacts(context: Context): List<PhonebookContact> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<PhonebookContact>()
            val seen = HashSet<String>()
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val numIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val contactId = cursor.getString(idIdx).orEmpty()
                    val name = cursor.getString(nameIdx)?.trim().orEmpty()
                    val number = cursor.getString(numIdx)?.trim().orEmpty()
                    if (name.isEmpty()) continue
                    val digits = number.filter { it.isDigit() }
                    val dedupeKey = if (digits.length >= 7) {
                        "$name|${digits.takeLast(9)}"
                    } else {
                        "$contactId|$name|$number"
                    }
                    if (seen.add(dedupeKey)) {
                        result.add(
                            PhonebookContact(
                                lookupKey = "$contactId:${result.size}",
                                name = name.take(60),
                                phoneNumber = number.take(30)
                            )
                        )
                    }
                }
            }
            result
        }

    suspend fun resolvePickedContactUri(context: Context, contactUri: Uri): PhonebookContact? =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            var contactId: String? = null
            var displayName = ""
            resolver.query(
                contactUri,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    contactId = cursor.getString(0)
                    displayName = cursor.getString(1)?.trim().orEmpty()
                }
            }
            if (displayName.isEmpty() || contactId == null) return@withContext null

            var phoneNumber = ""
            resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )?.use { phoneCursor ->
                if (phoneCursor.moveToFirst()) {
                    phoneNumber = phoneCursor.getString(0)?.trim().orEmpty()
                }
            }
            PhonebookContact(
                lookupKey = contactId!!,
                name = displayName.take(60),
                phoneNumber = phoneNumber.take(30)
            )
        }
}
