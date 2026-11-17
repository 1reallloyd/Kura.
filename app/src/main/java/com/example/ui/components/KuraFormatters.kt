package com.example.ui.components

import android.net.Uri
import com.example.data.model.LedgerContact
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object KuraFormatters {
    private val integerFormatter: NumberFormat = NumberFormat.getIntegerInstance(Locale("en", "GH"))

    fun formatMoney(cents: Long, currency: String = "GH₵"): String {
        val absCents = abs(cents)
        val whole = absCents / 100L
        val frac = (absCents % 100L).toString().padStart(2, '0')
        return "${currency.ifBlank { "GH₵" }} ${integerFormatter.format(whole)}.$frac"
    }

    fun formatSignedMoney(cents: Long, currency: String = "GH₵"): String {
        val prefix = if (cents < 0L) "−" else ""
        return prefix + formatMoney(cents, currency)
    }

    fun formatInputCents(absCents: Long): String {
        val whole = absCents / 100L
        val frac = (absCents % 100L).toString().padStart(2, '0')
        return "$whole.$frac"
    }

    fun parseCents(raw: String): Long? {
        val s = raw.replace(Regex("[,\\s]"), "")
        if (!Regex("^\\d{0,9}(\\.\\d{0,2})?$").matches(s) || !s.any { it.isDigit() }) {
            return null
        }
        val parts = s.split('.')
        val whole = parts[0].ifEmpty { "0" }.toLongOrNull() ?: return null
        val fracStr = parts.getOrElse(1) { "" }.padEnd(2, '0')
        val frac = fracStr.toLongOrNull() ?: return null
        val total = whole * 100L + frac
        return if (total > 0L) total else null
    }

    fun amountError(raw: String): String {
        val s = raw.replace(Regex("[,\\s]"), "")
        return when {
            Regex("[^\\d.]|\\..*\\.").containsMatchIn(s) -> "Use numbers only, like 150.50"
            Regex("\\.\\d{3,}").containsMatchIn(s) -> "Use at most 2 decimal places"
            Regex("^\\d{10,}").containsMatchIn(s) -> "That amount is too large"
            else -> "Enter an amount above 0"
        }
    }

    fun isValidPhone(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length in 9..15
    }

    fun validatePhoneOptional(
        phone: String,
        existingContacts: List<LedgerContact>,
        selfId: String? = null
    ): String? {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) return null
        val digits = trimmed.filter { it.isDigit() }
        if (digits.length !in 9..15) {
            return "Enter a valid phone number, or leave it empty"
        }
        val last9 = digits.takeLast(9)
        val duplicate = existingContacts.firstOrNull {
            !it.deleted &&
                it.id != selfId &&
                it.phone.filter { ch -> ch.isDigit() }.takeLast(9) == last9
        }
        return duplicate?.let { "${it.name} already has this number" }
    }

    fun initials(name: String): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        if (words.isEmpty()) return "K"
        return words.joinToString("") { word ->
            val cp = word.codePointAt(0)
            String(Character.toChars(cp)).uppercase(Locale.getDefault())
        }
    }

    fun relativeTimeAgo(tsMillis: Long): String {
        if (tsMillis <= 0L) return "No activity yet"
        val todayStart = startOfDayMillis(System.currentTimeMillis())
        val targetStart = startOfDayMillis(tsMillis)
        val days = ((todayStart - targetStart).toDouble() / 86_400_000.0).roundToLong()
        return when {
            days <= 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7L -> "$days days ago"
            days < 30L -> {
                val weeks = days / 7L
                if (weeks < 2L) "1 week ago" else "$weeks weeks ago"
            }
            else -> {
                val months = (days / 30L).coerceAtLeast(1L)
                if (months < 2L) "1 month ago" else "$months months ago"
            }
        }
    }

    fun formatShortDate(tsMillis: Long): String {
        val sdf = SimpleDateFormat("d MMM", Locale("en", "GH"))
        return sdf.format(Date(tsMillis))
    }

    fun formatHeaderClock(nowMillis: Long = System.currentTimeMillis()): String {
        val dateStr = SimpleDateFormat("EEEE, d MMMM", Locale("en", "GH")).format(Date(nowMillis))
        val timeStr = SimpleDateFormat("h:mm a", Locale("en", "GH")).format(Date(nowMillis))
        return "$dateStr · $timeStr"
    }

    fun greetingForHour(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun whatsAppUrl(phone: String, countryCode: String, text: String): String {
        var digits = phone.filter { it.isDigit() }
        if (digits.startsWith("00")) digits = digits.drop(2)
        val cc = countryCode.ifBlank { "233" }
        digits = when {
            digits.startsWith("0") -> cc + digits.drop(1)
            digits.length == 9 -> cc + digits
            else -> digits
        }
        return "https://wa.me/$digits?text=${Uri.encode(text)}"
    }

    private fun startOfDayMillis(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
