package com.zenlauncher.minimalist.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.minimalist.audio.ZenSoundEngine
import com.zenlauncher.minimalist.data.LauncherRepository
import com.zenlauncher.minimalist.media.MediaManager
import com.zenlauncher.minimalist.modes.ModesRepository
import com.zenlauncher.minimalist.ui.theme.BorderSubtle
import com.zenlauncher.minimalist.ui.theme.CardSurface
import com.zenlauncher.minimalist.ui.theme.NothingRed
import com.zenlauncher.minimalist.ui.theme.PureBlack
import com.zenlauncher.minimalist.ui.theme.TextMuted
import com.zenlauncher.minimalist.ui.theme.TextPrimary
import com.zenlauncher.minimalist.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    soundEngine: ZenSoundEngine,
    repository: LauncherRepository,
    modesRepository: ModesRepository,
    onDismiss: () -> Unit,
    onReloadApps: () -> Unit,
    onToggleHideStatusBar: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var soundEnabled by remember { mutableStateOf(repository.isSoundEnabled) }
    var hapticsEnabled by remember { mutableStateOf(repository.isHapticsEnabled) }
    var monochromeEnabled by remember { mutableStateOf(repository.isMonochromeIcons) }
    var hideStatusBarEnabled by remember { mutableStateOf(repository.isHideStatusBar) }
    var gesturesEnabled by remember { mutableStateOf(repository.isGesturesEnabled) }
    var autoDndEnabled by remember { mutableStateOf(modesRepository.isAutoDndSyncEnabled) }

    val allModes by modesRepository.allModes.collectAsState()

    ModalBottomSheet(
        onDismissRequest = {
            soundEngine.playBack()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = PureBlack,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "НАСТРОЙКИ // ZEN OS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Медиа-плеер
            Text(
                text = "МЕДИА-ПЛЕЕР",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        soundEngine.playTap()
                        MediaManager.requestNotificationAccess(context)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Доступ к уведомлениям медиа",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (MediaManager.isNotificationAccessGranted(context)) "Разрешено • Виджет активен" else "Требуется для управления воспроизведением",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = if (MediaManager.isNotificationAccessGranted(context)) NothingRed else TextMuted
                    )
                }
                Text(
                    text = "→",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section: Режимы Samsung (Modes and Routines)
            Text(
                text = "РЕЖИМЫ И СЦЕНАРИИ SAMSUNG",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Авто-переключение по «Не беспокоить»",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Включать режим «Сон» при активации DND в Samsung",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Switch(
                    checked = autoDndEnabled,
                    onCheckedChange = { checked ->
                        autoDndEnabled = checked
                        modesRepository.isAutoDndSyncEnabled = checked
                        soundEngine.playTap()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = TextPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom modes list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Активные профили (${allModes.size})",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                allModes.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${mode.name} [${mode.tag}]",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        if (mode.isDeletable) {
                            IconButton(
                                onClick = {
                                    soundEngine.playTap()
                                    modesRepository.deleteMode(mode.id)
                                    repository.updateFavoritesListForCurrentMode()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete mode",
                                    tint = NothingRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section: Звуки и Визуал
            Text(
                text = "ЗВУК И ТАКТИЛЬНОСТЬ",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Sound Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Звуки системы (Zen)",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Бархатные синусоидальные микро-ноты",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { checked ->
                        soundEnabled = checked
                        repository.isSoundEnabled = checked
                        soundEngine.isSoundEnabled = checked
                        if (checked) soundEngine.playTap()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = TextPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Haptics Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Тактильный отклик",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Мягкая микро-вибрация при тапах",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = { checked ->
                        hapticsEnabled = checked
                        repository.isHapticsEnabled = checked
                        soundEngine.isHapticsEnabled = checked
                        soundEngine.playTap()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = TextPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monochrome Icons Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Монохромные иконки",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Стильный ч/б фильтр для всех приложений",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Switch(
                    checked = monochromeEnabled,
                    onCheckedChange = { checked ->
                        monochromeEnabled = checked
                        repository.isMonochromeIcons = checked
                        soundEngine.playTap()
                        onReloadApps()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = TextPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section: Экран и Жесты
            Text(
                text = "ЭКРАН И ЖЕСТЫ",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Hide status bar toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Скрыть строку уведомлений",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Полный экран OLED (шторка доступна по свайпу вниз)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Switch(
                    checked = hideStatusBarEnabled,
                    onCheckedChange = { checked ->
                        hideStatusBarEnabled = checked
                        repository.isHideStatusBar = checked
                        onToggleHideStatusBar(checked)
                        soundEngine.playTap()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = TextPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gestures toggle and help card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Управление свайпами",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Быстрые жесты на главном экране",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Switch(
                        checked = gesturesEnabled,
                        onCheckedChange = { checked ->
                            gesturesEnabled = checked
                            repository.isGesturesEnabled = checked
                            soundEngine.playTap()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = TextPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BorderSubtle
                        )
                    )
                }

                if (gesturesEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderSubtle)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Свайп вниз ↓ : Открыть шторку уведомлений\n• Свайп вверх ↑ : Все приложения и поиск\n• Свайп влево/вправо ← → : Смена режима Samsung\n• Двойной тап 2× : Открыть настройки",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Set as Default Home Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        soundEngine.playTap()
                        try {
                            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val altIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                context.startActivity(altIntent)
                            } catch (e2: Exception) {
                                // Ignore
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Сделать лаунчером по умолчанию",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "→",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NothingRed
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
