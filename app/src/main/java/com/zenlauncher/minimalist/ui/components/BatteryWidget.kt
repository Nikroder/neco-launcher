package com.zenlauncher.minimalist.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.minimalist.audio.ZenSoundEngine
import com.zenlauncher.minimalist.battery.ConnectedDevice
import com.zenlauncher.minimalist.battery.DeviceType
import com.zenlauncher.minimalist.ui.animation.ZenMotion
import com.zenlauncher.minimalist.ui.theme.BorderSubtle
import com.zenlauncher.minimalist.ui.theme.CardSurface
import com.zenlauncher.minimalist.ui.theme.DarkSurface
import com.zenlauncher.minimalist.ui.theme.DotActive
import com.zenlauncher.minimalist.ui.theme.DotInactive
import com.zenlauncher.minimalist.ui.theme.NothingRed
import com.zenlauncher.minimalist.ui.theme.TextMuted
import com.zenlauncher.minimalist.ui.theme.TextPrimary
import com.zenlauncher.minimalist.ui.theme.TextSecondary

@Composable
fun BatteryWidget(
    phoneDevice: ConnectedDevice,
    connectedAccessories: List<ConnectedDevice>,
    soundEngine: ZenSoundEngine,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = ZenMotion.FloatSpring,
        label = "arrowRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .animateContentSize(animationSpec = ZenMotion.IntSizeSpring)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                soundEngine.playTap()
                isExpanded = !isExpanded
            }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Main Header Row (Phone Battery)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (phoneDevice.isCharging) NothingRed else DotActive)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (phoneDevice.isCharging) "CHARGING" else "BATTERY",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = TextSecondary
                )

                if (connectedAccessories.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${connectedAccessories.size} DEV",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NothingRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Segmented dot matrix bar for phone (5 dots)
                SegmentedDotMatrix(level = phoneDevice.batteryLevel)

                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${phoneDevice.batteryLevel}%",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "▾",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }
        }

        // Expanded Connected Devices Section
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(ZenMotion.smoothFade()) + slideInVertically(ZenMotion.IntOffsetSpring) { -15 },
            exit = fadeOut(ZenMotion.smoothFade()) + slideOutVertically(ZenMotion.IntOffsetSpring) { -15 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSubtle)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ПОДКЛЮЧЕННЫЕ УСТРОЙСТВА",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Internal Phone Row
                ConnectedDeviceRow(device = phoneDevice)

                if (connectedAccessories.isEmpty()) {
                    Text(
                        text = "Bluetooth-устройства не подключены",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    connectedAccessories.forEach { accessory ->
                        ConnectedDeviceRow(device = accessory)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedDeviceRow(device: ConnectedDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            val icon = when (device.type) {
                DeviceType.HEADPHONES -> Icons.Outlined.Headphones
                DeviceType.WATCH -> Icons.Outlined.Watch
                else -> Icons.Outlined.Smartphone
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = device.name,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 1
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            SegmentedDotMatrix(level = device.batteryLevel)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${device.batteryLevel}%",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SegmentedDotMatrix(level: Int) {
    val activeDots = (level / 20).coerceIn(0, 5)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (i <= activeDots) DotActive else DotInactive)
            )
        }
    }
}
