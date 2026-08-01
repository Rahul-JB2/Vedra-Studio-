package com.example.services

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Helper utility using Android's PackageManager to retrieve actual system app icons,
 * resolve installed packages, and launch application intents for Quick Commands & Quick Actions.
 */
object AppPackageManagerHelper {

    data class SystemAppInfo(
        val label: String,
        val packageName: String,
        val icon: Drawable?,
        val iconBitmap: ImageBitmap?,
        val launchIntent: Intent?,
        val isSystemApp: Boolean
    )

    /**
     * Safely converts any Android Drawable (AdaptiveIconDrawable, VectorDrawable, BitmapDrawable, etc.)
     * into a Bitmap.
     */
    fun drawableToBitmap(drawable: Drawable?, targetSize: Int = 96): Bitmap? {
        if (drawable == null) return null
        return try {
            if (drawable is BitmapDrawable && drawable.bitmap != null && drawable.bitmap.width > 0 && drawable.bitmap.height > 0) {
                return drawable.bitmap
            }
            val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else targetSize
            val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else targetSize
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Retrieves the actual system icon for an installed application as an ImageBitmap.
     * Searches by packageName first, falling back to label lookup via PackageManager.
     */
    fun getAppIconBitmap(context: Context, packageName: String?, appLabelQuery: String? = null): ImageBitmap? {
        val pm = context.packageManager

        // 1. Try direct package name lookup
        if (!packageName.isNull_or_blank_or_special(packageName)) {
            try {
                val drawable = pm.getApplicationIcon(packageName!!)
                val bitmap = drawableToBitmap(drawable)
                if (bitmap != null) return bitmap.asImageBitmap()
            } catch (_: Exception) { }
        }

        // 2. Try alternative known package aliases if known
        val searchWord = (appLabelQuery ?: packageName ?: "").lowercase().trim()
        if (searchWord.isNotEmpty()) {
            val resolvedPkg = findInstalledPackageOnDevice(context, searchWord)
            if (resolvedPkg != null && resolvedPkg != packageName) {
                try {
                    val drawable = pm.getApplicationIcon(resolvedPkg)
                    val bitmap = drawableToBitmap(drawable)
                    if (bitmap != null) return bitmap.asImageBitmap()
                } catch (_: Exception) { }
            }
        }

        return null
    }

    private fun String?.isNull_or_blank_or_special(str: String?): Boolean {
        if (str == null || str.isBlank()) return true
        return str == "torch" || str == "scan" || str.startsWith("action_")
    }

    /**
     * Finds the exact installed package name on the device matching a query label or keyword.
     */
    fun findInstalledPackageOnDevice(context: Context, query: String): String? {
        if (query.isBlank()) return null
        return try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val cleanQuery = query.lowercase().trim()

            // Exact label match
            for (ri in resolveInfos) {
                val label = ri.loadLabel(pm).toString().lowercase()
                if (label == cleanQuery) {
                    return ri.activityInfo.packageName
                }
            }
            // Label contains query
            for (ri in resolveInfos) {
                val label = ri.loadLabel(pm).toString().lowercase()
                if (label.contains(cleanQuery) || cleanQuery.contains(label)) {
                    return ri.activityInfo.packageName
                }
            }
            // Package name contains query
            for (ri in resolveInfos) {
                val pkg = ri.activityInfo.packageName.lowercase()
                if (pkg.contains(cleanQuery)) {
                    return ri.activityInfo.packageName
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets a valid launch Intent for an application by package name or label search.
     */
    fun getLaunchIntentForApp(context: Context, packageName: String?, appLabelQuery: String? = null): Intent? {
        val pm = context.packageManager

        if (!packageName.isNullOrBlank()) {
            try {
                val intent = pm.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    return intent
                }
            } catch (_: Exception) { }
        }

        val searchWord = (appLabelQuery ?: packageName ?: "").lowercase().trim()
        if (searchWord.isNotEmpty()) {
            val resolvedPkg = findInstalledPackageOnDevice(context, searchWord)
            if (resolvedPkg != null) {
                try {
                    val intent = pm.getLaunchIntentForPackage(resolvedPkg)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        return intent
                    }
                } catch (_: Exception) { }
            }
        }

        return null
    }

    /**
     * Launches the target application safely using PackageManager launch intents.
     */
    fun launchApp(context: Context, packageName: String?, appLabelQuery: String? = null): Boolean {
        val intent = getLaunchIntentForApp(context, packageName, appLabelQuery)
        return if (intent != null) {
            try {
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    /**
     * Query all installed applications on the Android system that have launcher activities.
     */
    fun getAllInstalledApps(context: Context): List<SystemAppInfo> {
        return try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            
            resolveInfos.mapNotNull { ri ->
                try {
                    val pkg = ri.activityInfo.packageName
                    val label = ri.loadLabel(pm).toString()
                    val iconDrawable = ri.loadIcon(pm)
                    val bitmap = drawableToBitmap(iconDrawable)?.asImageBitmap()
                    val launchIntent = pm.getLaunchIntentForPackage(pkg)?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val isSys = (ri.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                    SystemAppInfo(
                        label = label,
                        packageName = pkg,
                        icon = iconDrawable,
                        iconBitmap = bitmap,
                        launchIntent = launchIntent,
                        isSystemApp = isSys
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.label.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
