package com.zenlauncher.minimalist.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class ZenNotificationListenerService : NotificationListenerService() {

    private var mediaSessionManager: MediaSessionManager? = null

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveSession(controllers)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val component = ComponentName(this, ZenNotificationListenerService::class.java)
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionsListener, component)
            val controllers = mediaSessionManager?.getActiveSessions(component)
            updateActiveSession(controllers)
        } catch (e: SecurityException) {
            // Permission not granted yet
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsListener)
        } catch (e: Exception) {
            // Ignore
        }
        MediaManager.setActiveController(null)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        checkSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        checkSessions()
    }

    private fun checkSessions() {
        try {
            val component = ComponentName(this, ZenNotificationListenerService::class.java)
            val controllers = mediaSessionManager?.getActiveSessions(component)
            updateActiveSession(controllers)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun updateActiveSession(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            MediaManager.setActiveController(null)
            return
        }

        // Prioritize currently playing controller
        val active = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PAUSED
        } ?: controllers.firstOrNull()

        MediaManager.setActiveController(active)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsListener)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
