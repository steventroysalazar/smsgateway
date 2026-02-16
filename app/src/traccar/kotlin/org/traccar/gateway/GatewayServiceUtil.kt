package org.traccar.gateway

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.provider.Telephony

@Suppress("DEPRECATION")
object GatewayServiceUtil {

    private const val DEFAULT_LIMIT = 100
    private const val MAX_LIMIT = 1000
    private const val MIN_SUFFIX_MATCH_LENGTH = 7

    fun isServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (GatewayService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun sendMessage(context: Context, phone: String, message: String, slot: Int?) {
        val smsManager = if (slot != null) {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            val subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slot)
            SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.subscriptionId)
        } else {
            SmsManager.getDefault()
        }
        val parts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
    }

    @SuppressLint("MissingPermission")
    fun getIncomingMessages(context: Context, phone: String?, since: Long?, limit: Int?): List<GatewayMessage> {
        val safeLimit = (limit ?: DEFAULT_LIMIT).coerceIn(1, MAX_LIMIT)
        val phoneQuery = phone?.trim().orEmpty()

        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (since != null) {
            conditions.add("${Telephony.Sms.DATE} >= ?")
            args.add(since.toString())
        }

        val selection = conditions.joinToString(" AND ").ifBlank { null }
        val sortOrder = "${Telephony.Sms.DATE} DESC"

        val results = mutableListOf<GatewayMessage>()

        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
            selection,
            args.toTypedArray(),
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                val address = cursor.getString(addressColumn)
                if (!address.matchesPhoneFilter(phoneQuery)) {
                    continue
                }

                val id = cursor.getLong(idColumn)
                val body = cursor.getString(bodyColumn)
                val date = cursor.getLong(dateColumn)

                results.add(
                    GatewayMessage(
                        id = id,
                        phone = address,
                        message = body,
                        date = date
                    )
                )

                if (results.size >= safeLimit) {
                    break
                }
            }
        }

        return results
    }

    private fun String?.matchesPhoneFilter(phoneQuery: String): Boolean {
        if (phoneQuery.isBlank()) {
            return true
        }

        val address = this?.trim().orEmpty()
        if (address.isBlank()) {
            return false
        }

        val queryHasLetters = phoneQuery.any { it.isLetter() }
        if (queryHasLetters) {
            return address.contains(phoneQuery, ignoreCase = true)
        }

        if (address == phoneQuery) {
            return true
        }

        if (PhoneNumberUtils.compare(address, phoneQuery)) {
            return true
        }

        val normalizedAddress = address.normalizePhoneDigits()
        val normalizedQuery = phoneQuery.normalizePhoneDigits()

        if (normalizedAddress.isBlank() || normalizedQuery.isBlank()) {
            return false
        }

        if (normalizedAddress == normalizedQuery) {
            return true
        }

        val minLength = minOf(normalizedAddress.length, normalizedQuery.length)
        return minLength >= MIN_SUFFIX_MATCH_LENGTH && (
            normalizedAddress.endsWith(normalizedQuery) || normalizedQuery.endsWith(normalizedAddress)
            )
    }

    private fun String.normalizePhoneDigits(): String {
        return filter { it.isDigit() || it == '+' }
            .removePrefix("+")
    }
}
