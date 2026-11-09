package com.zenlauncher.minimalist.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable?,
    val isFavorite: Boolean = false,
    val launchCount: Int = 0
)
