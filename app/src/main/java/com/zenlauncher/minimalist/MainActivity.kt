package com.zenlauncher.minimalist

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.minimalist.audio.ZenSoundEngine
import com.zenlauncher.minimalist.battery.ConnectedDevicesManager
import com.zenlauncher.minimalist.data.LauncherRepository
import com.zenlauncher.minimalist.media.MediaManager
import com.zenlauncher.minimalist.model.AppInfo
import com.zenlauncher.minimalist.modes.LauncherMode
import com.zenlauncher.minimalist.modes.ModesRepository
import com.zenlauncher.minimalist.ui.components.AppDrawerSheet
import com.zenlauncher.minimalist.ui.components.BatteryWidget
import com.zenlauncher.minimalist.ui.components.ClockWidget
import com.zenlauncher.minimalist.ui.components.FavoritesList
import com.zenlauncher.minimalist.ui.components.MediaPlayerWidget
import com.zenlauncher.minimalist.ui.components.ModeSelectorRow
import com.zenlauncher.minimalist.ui.components.SettingsDialog
import com.zenlauncher.minimalist.ui.theme.DarkSurface
import com.zenlauncher.minimalist.ui.theme.NothingRed
import com.zenlauncher.minimalist.ui.theme.PureBlack
import com.zenlauncher.minimalist.ui.theme.TextMuted
import com.zenlauncher.minimalist.ui.theme.TextPrimary
import com.zenlauncher.minimalist.ui.theme.ZenLauncherTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var soundEngine: ZenSoundEngine
    private lateinit var modesRepository: ModesRepository
    private lateinit var repository: LauncherRepository
    private lateinit var devicesManager: ConnectedDevicesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Throwable) {
            android.util.Log.w("MainActivity", "EdgeToEdge fallback", e)
        }

        try {
            soundEngine = ZenSoundEngine(this)
            modesRepository = ModesRepository(this)
            repository = LauncherRepository(this, modesRepository)
            devicesManager = ConnectedDevicesManager(this)

            soundEngine.isSoundEnabled = repository.isSoundEnabled
            soundEngine.isHapticsEnabled = repository.isHapticsEnabled

            handleModeIntent(intent)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "Initialization error", e)
        }

        setContent {
            ZenLauncherTheme {
                HomeScreen(
                    soundEngine = soundEngine,
                    repository = repository,
                    modesRepository = modesRepository,
                    devicesManager = devicesManager,
                    onLaunchApp = { app -> launchApp(app) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::devicesManager.isInitialized) {
            devicesManager.refreshConnectedBluetoothDevices()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleModeIntent(intent)
    }

    private fun handleModeIntent(intent: Intent?) {
        if (intent == null) return
        val requestedMode = intent.getStringExtra("mode") ?: intent.getStringExtra("mode_id")
        if (!requestedMode.isNullOrBlank()) {
            if (::modesRepository.isInitialized) {
                modesRepository.selectMode(requestedMode.lowercase())
            }
            if (::repository.isInitialized) {
                repository.updateFavoritesListForCurrentMode()
            }
            if (::soundEngine.isInitialized) {
                soundEngine.playTap()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundEngine.isInitialized) {
            soundEngine.release()
        }
        if (::devicesManager.isInitialized) {
            devicesManager.unregister()
        }
    }

    private fun launchApp(app: AppInfo) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                val componentIntent = Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(app.packageName, app.activityName)
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(componentIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось запустить: ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun HomeScreen(
    soundEngine: ZenSoundEngine,
    repository: LauncherRepository,
    modesRepository: ModesRepository,
    devicesManager: ConnectedDevicesManager,
    onLaunchApp: (AppInfo) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allApps by repository.allApps.collectAsState()
    val favoriteApps by repository.favoriteApps.collectAsState()
    val activeMode by modesRepository.activeMode.collectAsState()
    val allModes by modesRepository.allModes.collectAsState()
    val mediaTrackState by MediaManager.trackState.collectAsState()

    val phoneDevice by devicesManager.phoneDevice.collectAsState()
    val connectedAccessories by devicesManager.connectedAccessories.collectAsState()

    var isDrawerOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var showAddModeDialog by remember { mutableStateOf(false) }
    var newModeName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repository.loadApps()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -25 && !isDrawerOpen && !isSettingsOpen) {
                        soundEngine.playDrawer()
                        isDrawerOpen = true
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            // Clock & Date Widget
            ClockWidget(soundEngine = soundEngine)

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Connected Devices & Battery Widget
            BatteryWidget(
                phoneDevice = phoneDevice,
                connectedAccessories = connectedAccessories,
                soundEngine = soundEngine
            )

            // Minimalist Media Player Widget (dynamically visible when music is playing)
            if (mediaTrackState != null) {
                Spacer(modifier = Modifier.height(10.dp))
                MediaPlayerWidget(
                    trackState = mediaTrackState,
                    soundEngine = soundEngine
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Samsung Modes & Routines Switcher Row
            ModeSelectorRow(
                allModes = allModes,
                activeMode = activeMode,
                soundEngine = soundEngine,
                onSelectMode = { mode ->
                    modesRepository.selectMode(mode.id)
                    repository.updateFavoritesListForCurrentMode()
                },
                onAddNewMode = {
                    showAddModeDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Favorites List for current mode with fluid spring transitions
            FavoritesList(
                favorites = favoriteApps,
                activeMode = activeMode,
                soundEngine = soundEngine,
                onAppClick = onLaunchApp,
                onRemoveFavorite = { repository.toggleFavorite(it) },
                onOpenDrawer = { isDrawerOpen = true },
                onOpenSettings = { isSettingsOpen = true }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // App Drawer Sheet
        if (isDrawerOpen) {
            AppDrawerSheet(
                allApps = allApps,
                activeMode = activeMode,
                soundEngine = soundEngine,
                onAppClick = { app ->
                    isDrawerOpen = false
                    onLaunchApp(app)
                },
                onToggleFavorite = { repository.toggleFavorite(it) },
                onDismiss = { isDrawerOpen = false }
            )
        }

        // Settings Sheet
        if (isSettingsOpen) {
            SettingsDialog(
                soundEngine = soundEngine,
                repository = repository,
                modesRepository = modesRepository,
                onDismiss = { isSettingsOpen = false },
                onReloadApps = {
                    coroutineScope.launch {
                        repository.loadApps()
                    }
                }
            )
        }

        // Add Mode Dialog
        if (showAddModeDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddModeDialog = false
                    newModeName = ""
                },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "НОВЫЙ РЕЖИМ",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = TextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Введите название режима (например: Учеба, Фокус):",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = newModeName,
                            onValueChange = { newModeName = it },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = PureBlack,
                                unfocusedContainerColor = PureBlack,
                                focusedIndicatorColor = NothingRed,
                                unfocusedIndicatorColor = TextMuted
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newModeName.isNotBlank()) {
                                soundEngine.playTap()
                                modesRepository.addCustomMode(newModeName.trim())
                                repository.updateFavoritesListForCurrentMode()
                                showAddModeDialog = false
                                newModeName = ""
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = NothingRed)
                    ) {
                        Text("СОЗДАТЬ", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            soundEngine.playBack()
                            showAddModeDialog = false
                            newModeName = ""
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
                    ) {
                        Text("ОТМЕНА", fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }
    }
}
