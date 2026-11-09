package com.zenlauncher.minimalist.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MediaManager {

    private val _trackState = MutableStateFlow<MediaTrackState?>(null)
    val trackState: StateFlow<MediaTrackState?> = _trackState.asStateFlow()

    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromController(activeController)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromController(activeController)
        }

        override fun onSessionDestroyed() {
            activeController = null
            _trackState.value = null
        }
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledListeners.contains(context.packageName)
    }

    fun requestNotificationAccess(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun setActiveController(controller: MediaController?) {
        if (activeController == controller) return

        activeController?.unregisterCallback(controllerCallback)
        activeController = controller

        if (controller != null) {
            controller.registerCallback(controllerCallback)
            updateFromController(controller)
        } else {
            _trackState.value = null
        }
    }

    fun updateFromController(controller: MediaController?) {
        if (controller == null) {
            _trackState.value = null
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        // Only show if media metadata exists and is playing or paused recently
        if (metadata == null && playbackState == null) {
            _trackState.value = null
            return
        }

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Воспроизведение"

        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: "Медиа"

        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = playbackState?.position ?: 0L
        val artBitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val pkgName = controller.packageName

        _trackState.value = MediaTrackState(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            durationMs = duration,
            positionMs = position,
            packageName = pkgName,
            albumArt = artBitmap
        )
    }

    fun playPause() {
        val controller = activeController ?: return
        val playbackState = controller.playbackState
        val transport = controller.transportControls ?: return

        if (playbackState?.state == PlaybackState.STATE_PLAYING) {
            transport.pause()
        } else {
            transport.play()
        }
    }

    fun skipToNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipToPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun openApp(context: Context) {
        val pkg = activeController?.packageName ?: return
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent != null) {
            context.startActivity(intent)
        }
    }
}
