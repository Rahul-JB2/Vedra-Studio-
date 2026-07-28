package com.example.services

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

object ContactsService {

    data class ContactInfo(
        val name: String,
        val phoneNumber: String
    )

    fun findContactByName(context: Context, nameQuery: String): ContactInfo? {
        val cleanQuery = nameQuery.trim()
        if (cleanQuery.isEmpty()) return null

        return try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$cleanQuery%"),
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val name = it.getString(nameIdx)
                    val number = it.getString(numIdx)
                    return ContactInfo(name, number)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun makeCall(context: Context, target: String): String {
        return try {
            val digits = target.filter { it.isDigit() || it == '+' }
            val uri = if (digits.isNotEmpty()) Uri.parse("tel:$digits") else Uri.parse("tel:$target")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Initiating call to $target..."
        } catch (e: Exception) {
            "Unable to open dialer: ${e.localizedMessage}"
        }
    }

    fun sendSMS(context: Context, target: String, messageText: String): String {
        val digits = target.filter { it.isDigit() || it == '+' }
        val recipientNumber = if (digits.isNotEmpty()) digits else target

        if (recipientNumber.isBlank()) {
            return "Invalid contact or phone number for SMS."
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission && messageText.isNotBlank()) {
            return try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                if (messageText.length > 160) {
                    val parts = smsManager.divideMessage(messageText)
                    smsManager.sendMultipartTextMessage(recipientNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(recipientNumber, null, messageText, null, null)
                }
                "SMS sent directly to $target: '$messageText'"
            } catch (e: Exception) {
                openSmsComposer(context, recipientNumber, messageText)
                "Opened SMS composer for $target: ${e.localizedMessage}"
            }
        } else {
            openSmsComposer(context, recipientNumber, messageText)
            return if (messageText.isBlank()) {
                "Opening SMS composer for $target..."
            } else {
                "Opened SMS composer for $target with message: '$messageText'"
            }
        }
    }

    private fun openSmsComposer(context: Context, number: String, messageText: String) {
        try {
            val uri = Uri.parse("smsto:$number")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun sendWhatsApp(context: Context, target: String, messageText: String): String {
        val digits = target.filter { it.isDigit() || it == '+' }
        val recipientNumber = if (digits.isNotEmpty()) digits else target

        val pm = context.packageManager
        val isWaInstalled = pm.getLaunchIntentForPackage("com.whatsapp") != null || pm.getLaunchIntentForPackage("com.whatsapp.w4b") != null

        if (!isWaInstalled) {
            openInPlayStore(context, "com.whatsapp")
            return "'whatsapp' is not installed. Opening Google Play Store page..."
        }

        val cleanPhone = recipientNumber.replace(Regex("""[^0-9+]"""), "")
        val encodedMessage = Uri.encode(messageText)

        val waUri = if (cleanPhone.isNotBlank()) {
            if (messageText.isNotBlank()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            } else {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
            }
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
        }

        val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (pm.getLaunchIntentForPackage("com.whatsapp") != null) {
                setPackage("com.whatsapp")
            } else if (pm.getLaunchIntentForPackage("com.whatsapp.w4b") != null) {
                setPackage("com.whatsapp.w4b")
            }
        }

        return try {
            context.startActivity(waIntent)
            if (messageText.isNotBlank()) {
                "Opening WhatsApp chat for $target with message: '$messageText'..."
            } else {
                "Opening WhatsApp chat for $target..."
            }
        } catch (e: Exception) {
            "Failed to open WhatsApp: ${e.localizedMessage}"
        }
    }

    private fun openInPlayStore(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
