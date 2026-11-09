package com.zenlauncher.minimalist.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.minimalist.ui.theme.DarkSurface
import com.zenlauncher.minimalist.ui.theme.LocalLauncherTheme
import com.zenlauncher.minimalist.ui.theme.NothingRed
import com.zenlauncher.minimalist.ui.theme.PureBlack
import com.zenlauncher.minimalist.ui.theme.TextMuted
import com.zenlauncher.minimalist.ui.theme.TextPrimary
import kotlin.math.abs
import kotlin.math.sin

/**
 * Niagara Launcher inspired Wave Alphabet Scrollbar.
 * Provides a fluid curved alphabet strip where letters physically bulge out like a wave
 * towards the user's thumb, accompanied by a floating letter indicator and haptics.
 */
@Composable
fun NiagaraWaveScrollbar(
    letters: List<String>,
    selectedLetter: String?,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDragStateChanged: (Boolean) -> Unit = {}
) {
    if (letters.isEmpty()) return

    val theme = LocalLauncherTheme.current
    var isDragging by remember { mutableStateOf(false) }
    var currentTouchY by remember { mutableFloatStateOf(0f) }
    var totalHeightPx by remember { mutableFloatStateOf(1f) }
    var activeIndex by remember { mutableIntStateOf(-1) }

    fun updateSelection(y: Float) {
        val clampedY = y.coerceIn(0f, totalHeightPx)
        val fraction = (clampedY / totalHeightPx).coerceIn(0f, 0.999f)
        val index = (fraction * letters.size).toInt().coerceIn(0, letters.size - 1)
        if (index != activeIndex) {
            activeIndex = index
            val letter = letters[index]
            onLetterSelected(letter)
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(42.dp)
            .onGloballyPositioned { coordinates ->
                totalHeightPx = coordinates.size.height.toFloat().coerceAtLeast(1f)
            }
            .pointerInput(letters) {
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        onDragStateChanged(true)
                        currentTouchY = offset.y
                        updateSelection(offset.y)
                        tryAwaitRelease()
                        isDragging = false
                        onDragStateChanged(false)
                        activeIndex = -1
                    }
                )
            }
            .pointerInput(letters) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        onDragStateChanged(true)
                        currentTouchY = offset.y
                        updateSelection(offset.y)
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragStateChanged(false)
                        activeIndex = -1
                    },
                    onDragCancel = {
                        isDragging = false
                        onDragStateChanged(false)
                        activeIndex = -1
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentTouchY = (currentTouchY + dragAmount.y).coerceIn(0f, totalHeightPx)
                        updateSelection(currentTouchY)
                    }
                )
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        // Vertical Alphabet Strip with Wave Physics
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            letters.forEachIndexed { index, letter ->
                val distance = if (isDragging && activeIndex >= 0) abs(index - activeIndex) else 999
                val inWave = distance <= 3

                // Wave bulge offset towards left
                val targetOffsetX = if (inWave) {
                    val factor = 1f - (distance / 3.5f)
                    val curved = sin(factor * (Math.PI / 2.0)).toFloat()
                    (-24f * curved).dp
                } else {
                    0.dp
                }

                // Scale factor for letters inside wave
                val targetScale = if (inWave) {
                    1f + (1f - distance / 3.5f) * 0.65f
                } else {
                    1f
                }

                val animatedOffsetX by animateDpAsState(
                    targetValue = targetOffsetX,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "waveOffset"
                )

                val animatedScale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "waveScale"
                )

                val textColor: Color = when {
                    isDragging && distance == 0 -> theme.accentColor
                    isDragging && inWave -> TextPrimary
                    selectedLetter == letter -> theme.accentColor
                    else -> TextMuted.copy(alpha = 0.65f)
                }

                Box(
                    modifier = Modifier
                        .offset(x = animatedOffsetX)
                        .scale(animatedScale)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (inWave || selectedLetter == letter) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 9.sp,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Floating Niagara Large Letter Popup
        AnimatedVisibility(
            visible = isDragging && activeIndex in letters.indices,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            if (activeIndex in letters.indices) {
                val letter = letters[activeIndex]
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = -130, y = 0) }
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.backgroundColor)
                        .border(1.5.dp, theme.accentColor, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = letter,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
