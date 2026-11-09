package com.zenlauncher.minimalist.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.zenlauncher.minimalist.model.AppInfo
import com.zenlauncher.minimalist.modes.LauncherMode
import com.zenlauncher.minimalist.ui.animation.ZenMotion
import com.zenlauncher.minimalist.ui.animation.bounceClick
import com.zenlauncher.minimalist.ui.theme.BorderSubtle
import com.zenlauncher.minimalist.ui.theme.CardSurface
import com.zenlauncher.minimalist.ui.theme.DarkSurface
import com.zenlauncher.minimalist.ui.theme.NothingRed
import com.zenlauncher.minimalist.ui.theme.TextMuted
import com.zenlauncher.minimalist.ui.theme.TextPrimary
import com.zenlauncher.minimalist.ui.theme.TextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesList(
    favorites: List<AppInfo>,
    activeMode: LauncherMode,
    soundEngine: ZenSoundEngine,
    onAppClick: (AppInfo) -> Unit,
    onRemoveFavorite: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode Header Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ИЗБРАННЫЕ // ${activeMode.name.uppercase()}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.4.sp,
                color = TextMuted
            )
            Text(
                text = "${favorites.size.coerceAtMost(6)} / 06",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        // Fluid Animated Content Transition on Mode Change
        AnimatedContent(
            targetState = favorites,
            transitionSpec = {
                (fadeIn(ZenMotion.smoothFade()) + slideInHorizontally(ZenMotion.IntOffsetSpring) { 40 })
                    .togetherWith(fadeOut(ZenMotion.smoothFade()) + slideOutHorizontally(ZenMotion.IntOffsetSpring) { -40 })
            },
            label = "modeFavoritesTransition"
        ) { currentFavorites ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentFavorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardSurface)
                            .bounceClick {
                                soundEngine.playDrawer()
                                onOpenDrawer()
                            }
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Закрепите приложения для режима «${activeMode.name}»",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                } else {
                    currentFavorites.take(6).forEachIndexed { index, app ->
                        var menuExpanded by remember { mutableStateOf(false) }

                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CardSurface)
                                    .combinedClickable(
                                        onClick = {
                                            soundEngine.playOpen()
                                            onAppClick(app)
                                        },
                                        onLongClick = {
                                            soundEngine.playTap()
                                            menuExpanded = true
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("%02d", index + 1),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = app.label,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.3.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    modifier = Modifier.size(5.dp),
                                    shape = CircleShape,
                                    color = BorderSubtle
                                ) {}
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = {
                                    soundEngine.playBack()
                                    menuExpanded = false
                                },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Убрать из «${activeMode.name}»", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = NothingRed)
                                    },
                                    onClick = {
                                        soundEngine.playTap()
                                        menuExpanded = false
                                        onRemoveFavorite(app)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("О приложении", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Info, contentDescription = null, tint = TextSecondary)
                                    },
                                    onClick = {
                                        soundEngine.playTap()
                                        menuExpanded = false
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", app.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Footer: All Apps & Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // All Apps Trigger
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardSurface)
                    .bounceClick {
                        soundEngine.playDrawer()
                        onOpenDrawer()
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "•••",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = NothingRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ВСЕ ПРИЛОЖЕНИЯ",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = TextSecondary
                )
            }

            // Quick Settings Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .bounceClick {
                        soundEngine.playTap()
                        onOpenSettings()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ZEN // PREFS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = TextMuted
                )
            }
        }
    }
}
