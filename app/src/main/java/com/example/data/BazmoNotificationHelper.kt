package com.example.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class BackgroundNotifConfig(
    val token: String,
    val uid: String,
    val tier: String,
    val isAdmin: Boolean,
    val province: String,
    val city: String,
    val region: String
)

object BazmoSyncBus {
    private val _syncEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 32)
    val syncEvents = _syncEvents.asSharedFlow()

    private val _configUpdates = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    val configUpdates = _configUpdates.asSharedFlow()

    fun notifySync() {
        _syncEvents.tryEmit(Unit)
    }

    fun notifyConfigUpdated() {
        _configUpdates.tryEmit(Unit)
    }
}

object BazmoNotificationHelper {
    const val CHANNEL_ID = "bazmo_alerts"
    private const val CHANNEL_NAME = "اعلان‌های صنفی بازمو"
    private const val CHANNEL_DESC = "اطلاعیه‌ها، هشدارهای موجودی قطعه و پیام‌های مدیریت بازمو"

    const val REALTIME_CHANNEL_ID = "bazmo_realtime_bg_v2"
    private const val LEGACY_REALTIME_CHANNEL_ID = "bazmo_realtime_status"
    private const val REALTIME_CHANNEL_NAME = "همگام‌سازی پس‌زمینه بازمو"
    private const val REALTIME_CHANNEL_DESC = "سرویس سبک پس‌زمینه برای دریافت آنی اعلان‌های بازمو بدون ایجاد مزاحمت"
    const val REALTIME_SERVICE_NOTIF_ID = 7740

    const val EXTRA_OPEN_NOTIFICATIONS = "bazmo_open_notifications"

    private const val PREFS_NAME = "bazmo_notif_service"
    private const val KEY_NOTIFIED_IDS = "notified_ids"
    private const val KEY_BG_TOKEN = "bg_token"
    private const val KEY_BG_UID = "bg_uid"
    private const val KEY_BG_TIER = "bg_tier"
    private const val KEY_BG_ADMIN = "bg_admin"
    private const val KEY_BG_PROVINCE = "bg_province"
    private const val KEY_BG_CITY = "bg_city"
    private const val KEY_BG_REGION = "bg_region"
    private const val KEY_REALTIME_ENABLED = "realtime_service_enabled"
    private const val BG_JOB_ID = 7741

    fun saveBackgroundConfig(context: Context, config: BackgroundNotifConfig) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BG_TOKEN, config.token)
            .putString(KEY_BG_UID, config.uid)
            .putString(KEY_BG_TIER, config.tier)
            .putBoolean(KEY_BG_ADMIN, config.isAdmin)
            .putString(KEY_BG_PROVINCE, config.province)
            .putString(KEY_BG_CITY, config.city)
            .putString(KEY_BG_REGION, config.region)
            .apply()
        BazmoSyncBus.notifyConfigUpdated()
    }

    fun loadBackgroundConfig(context: Context): BackgroundNotifConfig? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_BG_TOKEN, "").orEmpty()
        val uid = prefs.getString(KEY_BG_UID, "").orEmpty()
        if (token.isBlank() || uid.isBlank()) return null
        return BackgroundNotifConfig(
            token = token,
            uid = uid,
            tier = prefs.getString(KEY_BG_TIER, BazmoRoles.REPAIR_TECH).orEmpty(),
            isAdmin = prefs.getBoolean(KEY_BG_ADMIN, false),
            province = prefs.getString(KEY_BG_PROVINCE, "تهران").orEmpty(),
            city = prefs.getString(KEY_BG_CITY, "تهران").orEmpty(),
            region = prefs.getString(KEY_BG_REGION, "همه مناطق").orEmpty()
        )
    }

    fun clearBackgroundConfig(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_BG_TOKEN)
            .remove(KEY_BG_UID)
            .remove(KEY_BG_TIER)
            .remove(KEY_BG_ADMIN)
            .remove(KEY_BG_PROVINCE)
            .remove(KEY_BG_CITY)
            .remove(KEY_BG_REGION)
            .apply()
        BazmoSyncBus.notifyConfigUpdated()
    }

    fun isRealtimeServiceEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REALTIME_ENABLED, true)

    fun setRealtimeServiceEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REALTIME_ENABLED, enabled)
            .apply()
    }

    fun scheduleBackgroundCheck(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
        // Do not overwrite/reset an already scheduled periodic job timer
        if (scheduler.getPendingJob(BG_JOB_ID) != null) return
        val component = ComponentName(context, BazmoBackgroundNotifJobService::class.java)
        val info = JobInfo.Builder(BG_JOB_ID, component)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(15L * 60L * 1000L)
            .setPersisted(true)
            .build()
        runCatching { scheduler.schedule(info) }
    }

    fun cancelBackgroundCheck(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
        runCatching { scheduler.cancel(BG_JOB_ID) }
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            runCatching { manager.deleteNotificationChannel(LEGACY_REALTIME_CHANNEL_ID) }
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(channel)
            }
            if (manager.getNotificationChannel(REALTIME_CHANNEL_ID) == null) {
                val rtChannel = NotificationChannel(
                    REALTIME_CHANNEL_ID,
                    REALTIME_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = REALTIME_CHANNEL_DESC
                    enableVibration(false)
                    setSound(null, null)
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_SECRET
                }
                manager.createNotificationChannel(rtChannel)
            }
        }
    }

    fun buildRealtimeServiceNotification(context: Context): Notification {
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_NOTIFICATIONS, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            REALTIME_SERVICE_NOTIF_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, REALTIME_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_bazmo)
            .setContentTitle("بازمو فعال است")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
            .setOngoing(false)
            .setSilent(true)
            .setShowWhen(false)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun hasNotificationPermission(context: Context): Boolean {
        val runtimeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return runtimeGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun filterMatchingUnread(
        notifications: List<NotificationEntity>,
        uid: String,
        isAdmin: Boolean,
        tier: String,
        province: String,
        city: String,
        region: String
    ): List<NotificationEntity> {
        val normProv = BazmoLocations.normalizeZwnj(province)
        val normCity = BazmoLocations.normalizeZwnj(city)
        val normReg = BazmoLocations.normalizeZwnj(region)
        val normTier = BazmoLocations.normalizeZwnj(tier.ifBlank { BazmoRoles.REPAIR_TECH })
        return notifications.filter { n ->
            if (n.readBy.contains(uid)) return@filter false
            val provMatch = BazmoLocations.isAllScope(n.province) ||
                BazmoLocations.isAllScope(province) ||
                BazmoLocations.normalizeZwnj(n.province) == normProv
            val cityMatch = BazmoLocations.isAllScope(n.city) ||
                BazmoLocations.isAllScope(city) ||
                BazmoLocations.normalizeZwnj(n.city) == normCity
            val regMatch = BazmoLocations.isAllScope(n.region) ||
                BazmoLocations.isAllScope(region) ||
                BazmoLocations.normalizeZwnj(n.region) == normReg
            val tierMatch = isAdmin ||
                BazmoLocations.isAllScope(n.tierFilter) ||
                BazmoLocations.normalizeZwnj(n.tierFilter) == normTier
            provMatch && cityMatch && regMatch && tierMatch
        }
    }

    fun dispatchNewNotifications(
        context: Context,
        matchingUnread: List<NotificationEntity>,
        isInitialSnapshot: Boolean
    ): List<NotificationEntity> {
        ensureChannel(context)
        if (matchingUnread.isEmpty()) return emptyList()

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alreadyNotified = prefs.getStringSet(KEY_NOTIFIED_IDS, emptySet()).orEmpty().toMutableSet()

        val unseen = matchingUnread.filter { it.id.isNotBlank() && it.id !in alreadyNotified }
        if (unseen.isEmpty()) return emptyList()

        val toPost = if (isInitialSnapshot) listOf(unseen.first()) else unseen.take(3)
        val permissionGranted = hasNotificationPermission(context)

        // Only mark unseen IDs as notified if permission is granted (so we actually posted them)
        // OR if this is an initial backlog snapshot (preventing future flood of old items)
        if (permissionGranted || isInitialSnapshot) {
            unseen.forEach { alreadyNotified.add(it.id) }
            val trimmed = if (alreadyNotified.size > 150) {
                alreadyNotified.toList().takeLast(150).toSet()
            } else {
                alreadyNotified.toSet()
            }
            prefs.edit().putStringSet(KEY_NOTIFIED_IDS, trimmed).apply()
        }

        if (!permissionGranted) {
            return if (isInitialSnapshot) emptyList() else toPost
        }

        val manager = NotificationManagerCompat.from(context)
        toPost.forEach { notif ->
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_OPEN_NOTIFICATIONS, true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notif.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val built = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_bazmo)
                .setContentTitle(notif.title.ifBlank { "اعلان جدید بازمو" })
                .setContentText(notif.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notif.body))
                .setSubText(notif.type.ifBlank { "اطلاعیه صنفی" })
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            try {
                manager.notify(notif.id.hashCode(), built)
            } catch (_: SecurityException) {
                // Permission revoked mid-flight
            }
        }
        return if (isInitialSnapshot) emptyList() else toPost
    }
}
