package com.example.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

data class PermissionStatus(
    val permissionName: String,
    val isGranted: Boolean,
    val requiredForFeature: String
)

data class PermissionGroup(
    val id: String,
    val title: String,
    val subtitle: String,
    val permissions: List<String>,
    val iconKey: String = "mic"
)

object PermissionService {

    val REQUIRED_PERMISSIONS = listOf(
        Manifest.permission.RECORD_AUDIO to "Voice Commands & Ved AI",
        Manifest.permission.CAMERA to "QR Code & Document Scanner",
        Manifest.permission.READ_CONTACTS to "Contact Aliases & Calls",
        Manifest.permission.SEND_SMS to "SMS Messaging Actions",
        Manifest.permission.READ_CALENDAR to "Calendar Event Scheduling",
        Manifest.permission.CALL_PHONE to "Direct Voice Calling"
    )

    fun getPermissionGroups(): List<PermissionGroup> {
        val mediaPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val groups = mutableListOf(
            PermissionGroup(
                id = "voice",
                title = "Voice Interface",
                subtitle = "Process local acoustic data & voice commands",
                permissions = listOf(Manifest.permission.RECORD_AUDIO),
                iconKey = "mic"
            ),
            PermissionGroup(
                id = "identity",
                title = "Identity Node",
                subtitle = "Mapping relationship entities & direct phone calls",
                permissions = listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE),
                iconKey = "person"
            ),
            PermissionGroup(
                id = "vault",
                title = "Local Vault",
                subtitle = "Save and retrieve offline states, media & files",
                permissions = mediaPerms,
                iconKey = "folder"
            ),
            PermissionGroup(
                id = "sms",
                title = "SMS Messaging Engine",
                subtitle = "Send direct SMS messages & emergency quick texts",
                permissions = listOf(Manifest.permission.SEND_SMS),
                iconKey = "sms"
            ),
            PermissionGroup(
                id = "youtube",
                title = "YouTube & Media Playback",
                subtitle = "Play YouTube videos, playlists, episodes & offline media",
                permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                },
                iconKey = "youtube"
            ),
            PermissionGroup(
                id = "calendar",
                title = "Calendar Node",
                subtitle = "Manage schedules, events, and study reminders",
                permissions = listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
                iconKey = "calendar"
            ),
            PermissionGroup(
                id = "vision",
                title = "Vision Scanner",
                subtitle = "QR code scanning and document optical capture",
                permissions = listOf(Manifest.permission.CAMERA),
                iconKey = "camera"
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            groups.add(
                PermissionGroup(
                    id = "alerts",
                    title = "Neural Alerts",
                    subtitle = "Real-time system updates, background tasks & alarms",
                    permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                    iconKey = "notifications"
                )
            )
        }

        return groups
    }

    fun isGroupGranted(context: Context, group: PermissionGroup): Boolean {
        return group.permissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isPermissionsOnboarded(context: Context): Boolean {
        val prefs = context.getSharedPreferences("vedra_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_permissions_onboarded", false)
    }

    fun setPermissionsOnboarded(context: Context, onboarded: Boolean = true) {
        val prefs = context.getSharedPreferences("vedra_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_permissions_onboarded", onboarded).apply()
    }

    fun checkAllPermissions(context: Context): List<PermissionStatus> {
        val list = mutableListOf<PermissionStatus>()
        for ((perm, feature) in REQUIRED_PERMISSIONS) {
            val isGranted = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            list.add(PermissionStatus(perm, isGranted, feature))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isNotifGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            list.add(PermissionStatus(Manifest.permission.POST_NOTIFICATIONS, isNotifGranted, "Push Notifications & Alarms"))
        }

        return list
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
