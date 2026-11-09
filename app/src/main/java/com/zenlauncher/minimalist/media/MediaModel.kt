package com.zenlauncher.minimalist.media

import android.graphics.Bitmap

data class MediaTrackState(
    val title: String,
    val artist: String,
    val isPlaying: Boolean,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val packageName: String? = null,
    val albumArt: Bitmap? = null
)
