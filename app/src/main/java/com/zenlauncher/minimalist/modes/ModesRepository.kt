package com.zenlauncher.minimalist.modes

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ModesRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("zen_modes_prefs", Context.MODE_PRIVATE)

    private val _allModes = MutableStateFlow<List<LauncherMode>>(emptyList())
    val allModes: StateFlow<List<LauncherMode>> = _allModes.asStateFlow()

    private val _activeMode = MutableStateFlow(LauncherMode.DEFAULT)
    val activeMode: StateFlow<LauncherMode> = _activeMode.asStateFlow()

    var isAutoDndSyncEnabled: Boolean
        get() = prefs.getBoolean("key_auto_dnd_sync", true)
        set(value) = prefs.edit { putBoolean("key_auto_dnd_sync", value) }

    init {
        loadModes()
        registerDndReceiver()
    }

    private fun loadModes() {
        val rawJson = prefs.getString("key_modes_list", null)
        val modes = if (rawJson != null) {
            try {
                val array = JSONArray(rawJson)
                val list = mutableListOf<LauncherMode>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        LauncherMode(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            tag = obj.optString("tag", "MOD"),
                            isDefault = obj.optBoolean("isDefault", false),
                            isDeletable = obj.optBoolean("isDeletable", true)
                        )
                    )
                }
                list
            } catch (e: Exception) {
                LauncherMode.BUILT_IN
            }
        } else {
            LauncherMode.BUILT_IN
        }

        _allModes.value = modes

        val activeId = prefs.getString("key_active_mode_id", LauncherMode.DEFAULT.id)
        _activeMode.value = modes.firstOrNull { it.id == activeId } ?: LauncherMode.DEFAULT
    }

    fun saveModes(modes: List<LauncherMode>) {
        val array = JSONArray()
        for (m in modes) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("tag", m.tag)
                put("isDefault", m.isDefault)
                put("isDeletable", m.isDeletable)
            }
            array.put(obj)
        }
        prefs.edit { putString("key_modes_list", array.toString()) }
        _allModes.value = modes
    }

    fun selectMode(modeId: String) {
        val mode = _allModes.value.firstOrNull { it.id == modeId } ?: return
        prefs.edit { putString("key_active_mode_id", mode.id) }
        _activeMode.value = mode
    }

    fun addCustomMode(name: String): LauncherMode {
        val id = "custom_" + System.currentTimeMillis()
        val tag = name.take(3).uppercase()
        val newMode = LauncherMode(id = id, name = name, tag = tag, isDeletable = true)
        val updated = _allModes.value + newMode
        saveModes(updated)
        selectMode(id)
        return newMode
    }

    fun deleteMode(modeId: String) {
        val mode = _allModes.value.firstOrNull { it.id == modeId } ?: return
        if (!mode.isDeletable) return

        val updated = _allModes.value.filter { it.id != modeId }
        saveModes(updated)

        // Clear associated favorites
        prefs.edit { remove("key_favs_mode_$modeId") }

        if (_activeMode.value.id == modeId) {
            selectMode(LauncherMode.DEFAULT.id)
        }
    }

    fun getFavoritesForMode(modeId: String): Set<String> {
        val saved = prefs.getStringSet("key_favs_mode_$modeId", null)
        if (saved != null) return saved

        // Fallback to default launcher favorites if empty for standard mode
        if (modeId == LauncherMode.DEFAULT.id) {
            val legacy = context.getSharedPreferences("zen_launcher_prefs", Context.MODE_PRIVATE)
                .getStringSet("key_favorites", null)
            if (legacy != null) return legacy
        }

        return emptySet()
    }

    fun setFavoritesForMode(modeId: String, favorites: Set<String>) {
        prefs.edit { putStringSet("key_favs_mode_$modeId", favorites) }
    }

    fun toggleFavoriteForActiveMode(packageName: String): Set<String> {
        val modeId = _activeMode.value.id
        val current = getFavoritesForMode(modeId).toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            if (current.size < 7) {
                current.add(packageName)
            }
        }
        setFavoritesForMode(modeId, current)
        return current
    }

    private fun registerDndReceiver() {
        try {
            val filter = IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    if (!isAutoDndSyncEnabled) return
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                    val currentFilter = nm.currentInterruptionFilter

                    // If Samsung DND / Sleep routine is enabled
                    if (currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
                        if (_activeMode.value.id != LauncherMode.SLEEP.id) {
                            selectMode(LauncherMode.SLEEP.id)
                        }
                    }
                }
            }
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Throwable) {
            android.util.Log.w("ModesRepository", "Failed to register DND receiver", e)
        }
    }
}
