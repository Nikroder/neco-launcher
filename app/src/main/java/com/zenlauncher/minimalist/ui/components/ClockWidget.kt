package com.zenlauncher.minimalist.ui.components

import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.minimalist.audio.ZenSoundEngine
import com.zenlauncher.minimalist.ui.theme.LocalLauncherTheme
import com.zenlauncher.minimalist.ui.theme.NothingRed
import com.zenlauncher.minimalist.ui.theme.TextMuted
import com.zenlauncher.minimalist.ui.theme.TextPrimary
import com.zenlauncher.minimalist.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockWidget(
    soundEngine: ZenSoundEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale("ru")) }

    val formattedTime = timeFormat.format(currentTime)
    val formattedDate = dateFormat.format(currentTime).uppercase(Locale("ru"))

    val theme = LocalLauncherTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                soundEngine.playTap()
                try {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general clock intent
                    val clockIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.deskclock")
                        ?: context.packageManager.getLaunchIntentForPackage("com.sec.android.app.clockpackage")
                    clockIntent?.let { context.startActivity(it) }
                }
            }
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nothing OS style Red Accent indicator dot
            Surface(
                modifier = Modifier.size(7.dp),
                shape = CircleShape,
                color = theme.accentColor
            ) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "N.ECO // OS",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = formattedTime,
            fontSize = 58.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            letterSpacing = (-2).sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = formattedDate,
            fontSize = 12.5.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.3.sp,
            color = TextSecondary
        )
    }
}
