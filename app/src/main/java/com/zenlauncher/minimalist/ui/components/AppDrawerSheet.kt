package com.zenlauncher.minimalist.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.zenlauncher.minimalist.audio.ZenSoundEngine
import com.zenlauncher.minimalist.model.AppInfo
import com.zenlauncher.minimalist.modes.LauncherMode
import com.zenlauncher.minimalist.ui.theme.BorderSubtle
import com.zenlauncher.minimalist.ui.theme.CardSurface
import com.zenlauncher.minimalist.ui.theme.DarkSurface
import com.zenlauncher.minimalist.ui.theme.NothingRed
import com.zenlauncher.minimalist.ui.theme.PureBlack
import com.zenlauncher.minimalist.ui.theme.TextMuted
import com.zenlauncher.minimalist.ui.theme.TextPrimary
import com.zenlauncher.minimalist.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawerSheet(
    allApps: List<AppInfo>,
    activeMode: LauncherMode,
    soundEngine: ZenSoundEngine,
    onAppClick: (AppInfo) -> Unit,
    onToggleFavorite: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
    initialScrollLetter: String? = null
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf(initialScrollLetter) }

    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            val q = searchQuery.trim().lowercase()
            allApps.filter { it.label.lowercase().contains(q) }
        }
    }

    val availableLetters = remember(filteredApps) {
        val distinct = filteredApps
            .map { it.label.take(1).uppercase() }
            .distinct()
            .sorted()
        if (distinct.isEmpty()) listOf("A") else distinct
    }

    LaunchedEffect(initialScrollLetter, filteredApps) {
        if (!initialScrollLetter.isNullOrBlank()) {
            val idx = filteredApps.indexOfFirst { it.label.startsWith(initialScrollLetter, ignoreCase = true) }
            if (idx != -1) {
                listState.scrollToItem(idx)
            }
        }
    }

    BackHandler {
        soundEngine.playBack()
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

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
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            // Search Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        soundEngine.playTick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(NothingRed),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        if (filteredApps.isNotEmpty()) {
                            val first = filteredApps.first()
                            soundEngine.playOpen()
                            onAppClick(first)
                        }
                    }),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Поиск приложений (${allApps.size})...",
                                color = TextMuted,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            soundEngine.playTick()
                            searchQuery = ""
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Count and Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "ВСЕ ПРИЛОЖЕНИЯ" else "НАЙДЕНО: ${filteredApps.size}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = TextMuted
                )

                Text(
                    text = "РЕЖИМ: ${activeMode.name.uppercase()}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NothingRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Apps List
            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "Ничего не найдено",
                        fontFamily = FontFamily.SansSerif,
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            var itemMenuExpanded by remember { mutableStateOf(false) }

                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .combinedClickable(
                                            onClick = {
                                                soundEngine.playOpen()
                                                onAppClick(app)
                                            },
                                            onLongClick = {
                                                soundEngine.playTap()
                                                itemMenuExpanded = true
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // App Icon
                                    if (app.icon != null) {
                                        val bitmap = remember(app.packageName) {
                                            app.icon.toBitmap(width = 84, height = 84, config = Bitmap.Config.ARGB_8888)
                                        }
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = app.label,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(CardSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = app.label.take(1).uppercase(),
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Label
                                    Text(
                                        text = app.label,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Favorite Toggle Indicator
                                    IconButton(
                                        onClick = {
                                            soundEngine.playTap()
                                            onToggleFavorite(app)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (app.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Pin to favorites",
                                            tint = if (app.isFavorite) NothingRed else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = itemMenuExpanded,
                                    onDismissRequest = {
                                        soundEngine.playBack()
                                        itemMenuExpanded = false
                                    },
                                    modifier = Modifier.background(DarkSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (app.isFavorite) "Убрать из «${activeMode.name}»" else "Закрепить в «${activeMode.name}»",
                                                color = TextPrimary
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (app.isFavorite) Icons.Outlined.FavoriteBorder else Icons.Outlined.Favorite,
                                                contentDescription = null,
                                                tint = NothingRed
                                            )
                                        },
                                        onClick = {
                                            soundEngine.playTap()
                                            itemMenuExpanded = false
                                            onToggleFavorite(app)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("О приложении", color = TextPrimary) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Info, contentDescription = null, tint = TextSecondary)
                                        },
                                        onClick = {
                                            soundEngine.playTap()
                                            itemMenuExpanded = false
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

                    if (availableLetters.size > 1 && searchQuery.isEmpty()) {
                        NiagaraWaveScrollbar(
                            letters = availableLetters,
                            selectedLetter = selectedLetter,
                            onLetterSelected = { letter ->
                                selectedLetter = letter
                                val idx = filteredApps.indexOfFirst { it.label.startsWith(letter, ignoreCase = true) }
                                if (idx != -1) {
                                    soundEngine.playTick()
                                    coroutineScope.launch {
                                        listState.scrollToItem(idx)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
