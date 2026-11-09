package com.zenlauncher.minimalist

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.zenlauncher.minimalist.ui.components.NiagaraWaveScrollbar
import com.zenlauncher.minimalist.ui.components.SettingsDialog
import com.zenlauncher.minimalist.ui.theme.DarkSurface
import com.zenlauncher.minimalist.ui.theme.LocalLauncherTheme
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } catch (e: Throwable) {
                // Ignore
            }
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

        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (::repository.isInitialized) {
                applyFullscreen(repository.isHideStatusBar)
            } else {
                applyFullscreen(true)
            }
        } catch (e: Throwable) {
            android.util.Log.w("MainActivity", "Fullscreen init error", e)
        }

        setContent {
            val themeConfig by repository.themeConfig.collectAsState()
            ZenLauncherTheme(themeConfig = themeConfig) {
                HomeScreen(
                    soundEngine = soundEngine,
                    repository = repository,
                    modesRepository = modesRepository,
                    devicesManager = devicesManager,
                    onLaunchApp = { app -> launchApp(app) },
                    onExpandNotifications = { expandNotificationShade() },
                    onToggleHideStatusBar = { hide -> applyFullscreen(hide) }
                )
            }
        }
    }

    fun applyFullscreen(hideStatusBar: Boolean) {
        try {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (hideStatusBar) {
                controller.hide(WindowInsetsCompat.Type.statusBars())
            } else {
                controller.show(WindowInsetsCompat.Type.statusBars())
            }
        } catch (e: Throwable) {
            android.util.Log.w("MainActivity", "applyFullscreen error", e)
        }
    }

    fun expandNotificationShade() {
        try {
            val statusBarService = getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (e: Throwable) {
            try {
                val statusBarService = getSystemService("statusbar")
                val statusBarManager = Class.forName("android.app.StatusBarManager")
                val method = statusBarManager.getMethod("expand")
                method.invoke(statusBarService)
            } catch (e2: Throwable) {
                android.util.Log.w("MainActivity", "Failed to expand status bar", e2)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) {
            applyFullscreen(repository.isHideStatusBar)
        }
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
    onLaunchApp: (AppInfo) -> Unit,
    onExpandNotifications: () -> Unit,
    onToggleHideStatusBar: (Boolean) -> Unit
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
    var drawerInitialLetter by remember { mutableStateOf<String?>(null) }

    val allLetters = remember(allApps) {
        val distinct = allApps
            .map { it.label.take(1).uppercase() }
            .distinct()
            .sorted()
        if (distinct.isEmpty()) listOf("A", "M", "Z") else distinct
    }

    val density = LocalDensity.current
    val verticalThresholdPx = remember(density) { with(density) { 36.dp.toPx() } }
    val horizontalThresholdPx = remember(density) { with(density) { 48.dp.toPx() } }

    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }
    var gestureTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.loadApps()
    }

    val theme = LocalLauncherTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .displayCutoutPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (!isDrawerOpen && !isSettingsOpen) {
                            soundEngine.playTap()
                            isSettingsOpen = true
                        }
                    },
                    onLongPress = {
                        if (!isDrawerOpen && !isSettingsOpen) {
                            soundEngine.playTap()
                            isSettingsOpen = true
                        }
                    }
                )
            }
            .pointerInput(repository.isGesturesEnabled) {
                if (repository.isGesturesEnabled) {
                    detectDragGestures(
                        onDragStart = {
                            accumulatedDragX = 0f
                            accumulatedDragY = 0f
                            gestureTriggered = false
                        },
                        onDragEnd = {
                            gestureTriggered = false
                        },
                        onDragCancel = {
                            gestureTriggered = false
                        },
                        onDrag = { change, dragAmount ->
                            if (!gestureTriggered && !isDrawerOpen && !isSettingsOpen) {
                                accumulatedDragX += dragAmount.x
                                accumulatedDragY += dragAmount.y

                                val absX = kotlin.math.abs(accumulatedDragX)
                                val absY = kotlin.math.abs(accumulatedDragY)

                                if (absY > absX && absY > verticalThresholdPx) {
                                    gestureTriggered = true
                                    change.consume()
                                    if (accumulatedDragY < 0) {
                                        // Swipe UP: Open Drawer
                                        soundEngine.playDrawer()
                                        isDrawerOpen = true
                                    } else {
                                        // Swipe DOWN: Expand Notification Shade
                                        soundEngine.playTap()
                                        onExpandNotifications()
                                    }
                                } else if (absX > absY && absX > horizontalThresholdPx) {
                                    gestureTriggered = true
                                    change.consume()
                                    val modes = allModes
                                    if (modes.isNotEmpty()) {
                                        val currentIndex = modes.indexOfFirst { it.id == activeMode.id }
                                        if (currentIndex != -1) {
                                            soundEngine.playTick()
                                            val newIndex = if (accumulatedDragX < 0) {
                                                (currentIndex + 1) % modes.size
                                            } else {
                                                if (currentIndex - 1 < 0) modes.size - 1 else currentIndex - 1
                                            }
                                            modesRepository.selectMode(modes[newIndex].id)
                                            repository.updateFavoritesListForCurrentMode()
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            // Clock & Date Widget
            ClockWidget(soundEngine = soundEngine)

            Spacer(modifier = Modifier.height(6.dp))

            // Expandable Connected Devices & Battery Widget
            BatteryWidget(
                phoneDevice = phoneDevice,
                connectedAccessories = connectedAccessories,
                soundEngine = soundEngine
            )

            // Minimalist Media Player Widget (dynamically visible when music is playing)
            if (mediaTrackState != null) {
                Spacer(modifier = Modifier.height(8.dp))
                MediaPlayerWidget(
                    trackState = mediaTrackState,
                    soundEngine = soundEngine
                )
            }

            // Guaranteed breathing room: Concrete unweighted spacer NEVER collapses below 28.dp!
            Spacer(modifier = Modifier.height(28.dp))
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

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Niagara Alphabet Wave Slider on Home Screen
        if (!isDrawerOpen && !isSettingsOpen && allLetters.size > 2) {
            NiagaraWaveScrollbar(
                letters = allLetters,
                selectedLetter = null,
                onLetterSelected = { letter ->
                    soundEngine.playTick()
                    drawerInitialLetter = letter
                    isDrawerOpen = true
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp, top = 70.dp, bottom = 40.dp)
            )
        }

        // App Drawer Sheet
        if (isDrawerOpen) {
            AppDrawerSheet(
                allApps = allApps,
                activeMode = activeMode,
                soundEngine = soundEngine,
                onAppClick = { app ->
                    isDrawerOpen = false
                    drawerInitialLetter = null
                    onLaunchApp(app)
                },
                onToggleFavorite = { repository.toggleFavorite(it) },
                onDismiss = {
                    isDrawerOpen = false
                    drawerInitialLetter = null
                },
                initialScrollLetter = drawerInitialLetter
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
                },
                onToggleHideStatusBar = onToggleHideStatusBar
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
