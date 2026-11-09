package com.zenlauncher.minimalist.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.minimalist.audio.ZenSoundEngine
import com.zenlauncher.minimalist.modes.LauncherMode
import com.zenlauncher.minimalist.ui.animation.ZenMotion
import com.zenlauncher.minimalist.ui.animation.bounceClick
import com.zenlauncher.minimalist.ui.theme.LocalLauncherTheme

@Composable
fun ModeSelectorRow(
    allModes: List<LauncherMode>,
    activeMode: LauncherMode,
    soundEngine: ZenSoundEngine,
    onSelectMode: (LauncherMode) -> Unit,
    onAddNewMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalLauncherTheme.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        allModes.forEach { mode ->
            val isActive = mode.id == activeMode.id

            val backgroundColor by animateColorAsState(
                targetValue = if (isActive) theme.cardSurface else theme.darkSurface,
                animationSpec = ZenMotion.ColorSpring,
                label = "modeBgColor"
            )

            val textColor by animateColorAsState(
                targetValue = if (isActive) com.zenlauncher.minimalist.ui.theme.TextPrimary else com.zenlauncher.minimalist.ui.theme.TextMuted,
                animationSpec = ZenMotion.ColorSpring,
                label = "modeTextColor"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        if (isActive) Modifier.border(1.dp, theme.accentColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        else Modifier
                    )
                    .background(backgroundColor)
                    .bounceClick(scaleDown = 0.94f) {
                        soundEngine.playTap()
                        onSelectMode(mode)
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(theme.accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = mode.name.uppercase(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    letterSpacing = 1.sp,
                    color = textColor
                )
            }
        }

        // Add custom mode button
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(theme.darkSurface)
                .bounceClick(scaleDown = 0.90f) {
                    soundEngine.playTap()
                    onAddNewMode()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add Mode",
                tint = com.zenlauncher.minimalist.ui.theme.TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
