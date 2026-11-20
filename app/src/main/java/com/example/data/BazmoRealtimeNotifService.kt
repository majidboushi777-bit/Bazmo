package com.example.data

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.URLEncoder

class BazmoRealtimeNotifService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wsLoopJob: Job? = null
    private var pollLoopJob: Job? = null
    private var configWatchJob: Job? = null
    @Volatile
    private var wsConnected: Boolean = false
    @Volatile
    private var activeToken: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopGracefully()
            return START_NOT_STICKY
        }

        val cfg = BazmoNotificationHelper.loadBackgroundConfig(applicationContext)
        if (cfg == null || !BazmoNotificationHelper.isRealtimeServiceEnabled(applicationContext)) {
            stopGracefully()
            return START_NOT_STICKY
        }

        val notification = BazmoNotificationHelper.buildRealtimeServiceNotification(applicationContext)
        try {
            val fgType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
            ServiceCompat.startForeground(
                this,
                BazmoNotificationHelper.REALTIME_SERVICE_NOTIF_ID,
                notification,
                fgType
            )
        } catch (_: Exception) {
            BazmoNotificationHelper.scheduleBackgroundCheck(applicationContext)
            stopSelf()
            return START_NOT_STICKY
        }

        if (wsLoopJob?.isActive != true || activeToken != cfg.token) {
            activeToken = cfg.token
            startLoops(cfg.token)
        }

        return START_STICKY
    }

    private fun startLoops(token: String) {
        wsLoopJob?.cancel()
        pollLoopJob?.cancel()
        configWatchJob?.cancel()

        configWatchJob = serviceScope.launch {
            BazmoSyncBus.configUpdates.collect {
                val latest = BazmoNotificationHelper.loadBackgroundConfig(applicationContext)
                if (latest == null || !BazmoNotificationHelper.isRealtimeServiceEnabled(applicationContext)) {
                    stopGracefully()
                } else {
                    checkUnreadNow(latest)
                }
            }
        }

        wsLoopJob = serviceScope.launch {
            val baseUrl = BazmoServerConfig.baseUrl().trim().trimEnd('/')
            val encodedToken = runCatching { URLEncoder.encode(token, "UTF-8") }.getOrDefault(token)
            val wsUrl = "${BazmoServerConfig.wsUrl(baseUrl)}?token=$encodedToken"
            var backoffMs = 2_500L

            while (isActive) {
                val currentCfg = BazmoNotificationHelper.loadBackgroundConfig(applicationContext)
                if (currentCfg == null || !BazmoNotificationHelper.isRealtimeServiceEnabled(applicationContext)) {
                    stopGracefully()
                    break
                }

                val closedSignal = CompletableDeferred<Unit>()
                var activeWs: WebSocket? = null
                try {
                    val req = Request.Builder().url(wsUrl).build()
                    activeWs = BazmoServerConfig.okHttpClient.newWebSocket(req, object : WebSocketListener() {
                        override fun onOpen(webSocket: WebSocket, response: Response) {
                            wsConnected = true
                            backoffMs = 2_500L
                            serviceScope.launch {
                                BazmoNotificationHelper.loadBackgroundConfig(applicationContext)?.let {
                                    checkUnreadNow(it)
                                }
                            }
                        }

                        override fun onMessage(webSocket: WebSocket, text: String) {
                            if (text.contains("\"sync\"")) {
                                BazmoSyncBus.notifySync()
                                serviceScope.launch {
                                    BazmoNotificationHelper.loadBackgroundConfig(applicationContext)?.let {
                                        checkUnreadNow(it)
                                    }
                                }
                            }
                        }

                        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                            wsConnected = false
                            closedSignal.complete(Unit)
                        }

                        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                            wsConnected = false
                            closedSignal.complete(Unit)
                        }
                    })
                    closedSignal.await()
                } catch (_: Exception) {
                    wsConnected = false
                } finally {
                    activeWs?.cancel()
                }

                if (!isActive) break
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(15_000L)
            }
        }

        pollLoopJob = serviceScope.launch {
            while (isActive) {
                val cfg = BazmoNotificationHelper.loadBackgroundConfig(applicationContext)
                if (cfg == null || !BazmoNotificationHelper.isRealtimeServiceEnabled(applicationContext)) {
                    stopGracefully()
                    break
                }
                checkUnreadNow(cfg)
                delay(if (wsConnected) 45_000L else 12_000L)
            }
        }
    }

    private suspend fun checkUnreadNow(cfg: BackgroundNotifConfig) {
        val repo = BazmoRepository(authToken = cfg.token, currentUid = cfg.uid)
        val res = repo.fetchUnreadNotifications(
            province = cfg.province,
            city = cfg.city,
            region = cfg.region
        )
        res.onSuccess { list ->
            val matching = BazmoNotificationHelper.filterMatchingUnread(
                notifications = list,
                uid = cfg.uid,
                isAdmin = cfg.isAdmin,
                tier = cfg.tier,
                province = cfg.province,
                city = cfg.city,
                region = cfg.region
            )
            if (matching.isNotEmpty()) {
                val posted = BazmoNotificationHelper.dispatchNewNotifications(
                    context = applicationContext,
                    matchingUnread = matching,
                    isInitialSnapshot = false
                )
                if (posted.isNotEmpty()) {
                    BazmoSyncBus.notifySync()
                }
            }
        }
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        // Android 15+ requires dataSync foreground services to stop on timeout
        BazmoNotificationHelper.scheduleBackgroundCheck(applicationContext)
        stopGracefully()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        BazmoNotificationHelper.scheduleBackgroundCheck(applicationContext)
        super.onTaskRemoved(rootIntent)
    }

    private fun stopGracefully() {
        wsConnected = false
        wsLoopJob?.cancel()
        pollLoopJob?.cancel()
        configWatchJob?.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        wsConnected = false
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_STOP = "com.example.action.STOP_REALTIME_NOTIFS"

        fun startIfPermitted(context: Context) {
            val cfg = BazmoNotificationHelper.loadBackgroundConfig(context) ?: return
            if (cfg.token.isBlank() || cfg.uid.isBlank()) return
            if (!BazmoNotificationHelper.isRealtimeServiceEnabled(context)) return
            if (!BazmoNotificationHelper.hasNotificationPermission(context)) return
            val intent = Intent(context, BazmoRealtimeNotifService::class.java)
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }
        }

        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, BazmoRealtimeNotifService::class.java))
            }
        }
    }
}
