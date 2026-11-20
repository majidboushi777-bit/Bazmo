package com.example.data

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@SuppressLint("SpecifyJobSchedulerIdRange")
class BazmoBackgroundNotifJobService : JobService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var runningJob: Job? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        val cfg = BazmoNotificationHelper.loadBackgroundConfig(applicationContext)
        if (cfg == null || cfg.token.isBlank() || cfg.uid.isBlank()) {
            return false
        }

        runningJob?.cancel()
        runningJob = serviceScope.launch {
            try {
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
                        BazmoNotificationHelper.dispatchNewNotifications(
                            context = applicationContext,
                            matchingUnread = matching,
                            isInitialSnapshot = false
                        )
                        BazmoSyncBus.notifySync()
                    }
                }
            } finally {
                jobFinished(params, false)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        runningJob?.cancel()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
