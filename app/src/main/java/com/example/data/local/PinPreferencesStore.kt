package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.math.min

private val Context.kuraPrefs by preferencesDataStore(name = "kura_local_prefs")

data class PinLockState(
    val hasPin: Boolean = false,
    val tries: Int = 0,
    val lockUntilMs: Long = 0L
)

class PinPreferencesStore(private val context: Context) {
    private val onboardingSeenKey = booleanPreferencesKey("onboarding_seen")
    private val welcomeStorySeenKey = booleanPreferencesKey("welcome_story_seen")

    private val profileNameKey = stringPreferencesKey("profile_name")
    private val profileBizKey = stringPreferencesKey("profile_biz")
    private val profilePhoneKey = stringPreferencesKey("profile_phone")
    private val profileLocKey = stringPreferencesKey("profile_loc")
    private val profileCurrencyKey = stringPreferencesKey("profile_currency")
    private val profileCcKey = stringPreferencesKey("profile_cc")
    private val profileOnboardedKey = booleanPreferencesKey("profile_onboarded")

    private val pinHashKey = stringPreferencesKey("passcode_hash")
    private val pinSaltKey = stringPreferencesKey("passcode_salt")
    private val triesKey = intPreferencesKey("passcode_tries")
    private val lockUntilKey = longPreferencesKey("passcode_lock_until")

    val welcomeStorySeenFlow: Flow<Boolean> = context.kuraPrefs.data.map { prefs ->
        prefs[welcomeStorySeenKey] ?: false
    }

    val onboardingSeenFlow: Flow<Boolean> = context.kuraPrefs.data.map { prefs ->
        prefs[onboardingSeenKey] ?: false
    }

    val profileFlow: Flow<UserProfile?> = context.kuraPrefs.data.map { prefs ->
        val onboarded = prefs[profileOnboardedKey] ?: false
        val name = prefs[profileNameKey].orEmpty()
        if (!onboarded && name.isEmpty()) {
            null
        } else {
            UserProfile(
                name = name,
                biz = prefs[profileBizKey].orEmpty(),
                phone = prefs[profilePhoneKey].orEmpty(),
                loc = prefs[profileLocKey].orEmpty(),
                currency = prefs[profileCurrencyKey] ?: "GH₵",
                countryCode = prefs[profileCcKey] ?: "233",
                onboarded = onboarded
            )
        }
    }

    val pinStateFlow: Flow<PinLockState> = context.kuraPrefs.data.map { prefs ->
        PinLockState(
            hasPin = !prefs[pinHashKey].isNullOrBlank(),
            tries = prefs[triesKey] ?: 0,
            lockUntilMs = prefs[lockUntilKey] ?: 0L
        )
    }

    suspend fun markWelcomeStorySeen() {
        context.kuraPrefs.edit { it[welcomeStorySeenKey] = true }
    }

    suspend fun markOnboardingSeen() {
        context.kuraPrefs.edit { it[onboardingSeenKey] = true }
    }

    suspend fun saveProfile(
        name: String,
        biz: String,
        phone: String,
        loc: String,
        currency: String,
        countryCode: String,
        onboarded: Boolean
    ) {
        context.kuraPrefs.edit { prefs ->
            prefs[profileNameKey] = name
            prefs[profileBizKey] = biz
            prefs[profilePhoneKey] = phone
            prefs[profileLocKey] = loc
            prefs[profileCurrencyKey] = currency
            prefs[profileCcKey] = countryCode
            prefs[profileOnboardedKey] = onboarded
        }
    }

    suspend fun updateRegion(currency: String, countryCode: String) {
        context.kuraPrefs.edit { prefs ->
            prefs[profileCurrencyKey] = currency
            prefs[profileCcKey] = countryCode
        }
    }

    suspend fun setPin(pin: String) {
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val salt = saltBytes.joinToString("") { "%02x".format(it) }
        val hash = sha256(salt + pin)
        context.kuraPrefs.edit { prefs ->
            prefs[pinSaltKey] = salt
            prefs[pinHashKey] = hash
            prefs[triesKey] = 0
            prefs[lockUntilKey] = 0L
        }
    }

    suspend fun clearPin() {
        context.kuraPrefs.edit { prefs ->
            prefs.remove(pinSaltKey)
            prefs.remove(pinHashKey)
            prefs[triesKey] = 0
            prefs[lockUntilKey] = 0L
        }
    }

    suspend fun verifyPin(candidate: String): PinVerifyResult {
        val prefs = context.kuraPrefs.data.first()
        val now = System.currentTimeMillis()
        val lockUntil = prefs[lockUntilKey] ?: 0L
        if (lockUntil > now) {
            val waitSeconds = ((lockUntil - now) / 1000L).coerceAtLeast(1L)
            return PinVerifyResult.Locked(waitSeconds)
        }
        val salt = prefs[pinSaltKey].orEmpty()
        val expectedHash = prefs[pinHashKey].orEmpty()
        if (expectedHash.isBlank()) return PinVerifyResult.Success

        if (sha256(salt + candidate) == expectedHash) {
            context.kuraPrefs.edit {
                it[triesKey] = 0
                it[lockUntilKey] = 0L
            }
            return PinVerifyResult.Success
        }

        val nextTries = (prefs[triesKey] ?: 0) + 1
        val newLockUntil = if (nextTries >= 5) {
            val factor = min(1 shl (nextTries - 5).coerceAtMost(10), 20)
            now + 30_000L * factor
        } else {
            0L
        }
        context.kuraPrefs.edit {
            it[triesKey] = nextTries
            it[lockUntilKey] = newLockUntil
        }
        return if (newLockUntil > 0L) {
            PinVerifyResult.Locked(30L)
        } else {
            PinVerifyResult.WrongPin(triesLeft = (5 - nextTries).coerceAtLeast(1))
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

sealed interface PinVerifyResult {
    data object Success : PinVerifyResult
    data class WrongPin(val triesLeft: Int) : PinVerifyResult
    data class Locked(val waitSeconds: Long) : PinVerifyResult
}
