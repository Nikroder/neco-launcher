package com.zenlauncher.minimalist.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.edit
import com.zenlauncher.minimalist.model.AppInfo
import com.zenlauncher.minimalist.modes.ModesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class LauncherRepository(
    private val context: Context,
    val modesRepository: ModesRepository
) {

    private val prefs = context.getSharedPreferences("zen_launcher_prefs", Context.MODE_PRIVATE)
    private val packageManager: PackageManager = context.packageManager

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _favoriteApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val favoriteApps: StateFlow<List<AppInfo>> = _favoriteApps.asStateFlow()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("key_sound_enabled", true)
        set(value) = prefs.edit { putBoolean("key_sound_enabled", value) }

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean("key_haptics_enabled", true)
        set(value) = prefs.edit { putBoolean("key_haptics_enabled", value) }

    var isMonochromeIcons: Boolean
        get() = prefs.getBoolean("key_monochrome_icons", true)
        set(value) = prefs.edit { putBoolean("key_monochrome_icons", value) }

    var isHideStatusBar: Boolean
        get() = prefs.getBoolean("key_hide_status_bar", true)
        set(value) = prefs.edit { putBoolean("key_hide_status_bar", value) }

    var isGesturesEnabled: Boolean
        get() = prefs.getBoolean("key_gestures_enabled", true)
        set(value) = prefs.edit { putBoolean("key_gestures_enabled", value) }

    private fun getInitialDefaultSuggestions(): Set<String> {
        val defaults = mutableSetOf<String>()
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = packageManager.queryIntentActivities(mainIntent, 0)
        for (info in resolved) {
            val pkg = info.activityInfo.packageName
            if (pkg.contains("dialer") || pkg.contains("phone") ||
                pkg.contains("messaging") || pkg.contains("mms") ||
                pkg.contains("chrome") || pkg.contains("browser") ||
                pkg.contains("camera") || pkg.contains("music")) {
                defaults.add(pkg)
                if (defaults.size >= 6) break
            }
        }
        return defaults
    }

    suspend fun loadApps() = withContext(Dispatchers.IO) {
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedActivities: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
            val currentPackageName = context.packageName

            val currentActiveModeId = modesRepository.activeMode.value.id
            var modeFavorites = modesRepository.getFavoritesForMode(currentActiveModeId)

            if (modeFavorites.isEmpty() && currentActiveModeId == "default") {
                modeFavorites = getInitialDefaultSuggestions()
                modesRepository.setFavoritesForMode(currentActiveModeId, modeFavorites)
            }

            val appList = resolvedActivities
                .filter { it.activityInfo.packageName != currentPackageName }
                .map { info ->
                    val label = try { info.loadLabel(packageManager).toString() } catch (e: Throwable) { info.activityInfo.packageName }
                    val rawIcon = try { info.loadIcon(packageManager) } catch (e: Throwable) { null }
                    val icon = if (isMonochromeIcons && rawIcon != null) toMonochromeDrawable(rawIcon) else rawIcon
                    val pkgName = info.activityInfo.packageName
                    val actName = info.activityInfo.name

                    AppInfo(
                        packageName = pkgName,
                        activityName = actName,
                        label = label,
                        icon = icon,
                        isFavorite = modeFavorites.contains(pkgName)
                    )
                }
                .sortedBy { it.label.lowercase() }

            _allApps.value = appList
            updateFavoritesListForCurrentMode()
        } catch (e: Throwable) {
            android.util.Log.e("LauncherRepository", "Error loading apps", e)
        }
    }

    fun updateFavoritesListForCurrentMode() {
        val activeModeId = modesRepository.activeMode.value.id
        val modeFavorites = modesRepository.getFavoritesForMode(activeModeId)

        val updatedAll = _allApps.value.map {
            it.copy(isFavorite = modeFavorites.contains(it.packageName))
        }
        _allApps.value = updatedAll
        _favoriteApps.value = updatedAll.filter { it.isFavorite }
    }

    fun toggleFavorite(app: AppInfo) {
        modesRepository.toggleFavoriteForActiveMode(app.packageName)
        updateFavoritesListForCurrentMode()
    }

    private fun toMonochromeDrawable(drawable: Drawable): Drawable {
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)

            val paint = Paint().apply {
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val scale = 1.15f
                val translate = (-0.15f * 128f)
                val contrastMatrix = floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
                cm.postConcat(ColorMatrix(contrastMatrix))
                colorFilter = ColorMatrixColorFilter(cm)
            }
            canvas.saveLayer(null, paint)
            drawable.draw(canvas)
            canvas.restore()

            BitmapDrawable(context.resources, bitmap)
        } catch (e: Throwable) {
            drawable
        }
    }
}
