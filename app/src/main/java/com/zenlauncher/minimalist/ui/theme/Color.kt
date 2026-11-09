package com.zenlauncher.minimalist.ui.theme

import androidx.compose.ui.graphics.Color

val PureBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF0D0D0D)
val CardSurface = Color(0xFF141414)
val BorderSubtle = Color(0xFF262626)

val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFF8E8E93)
val TextMuted = Color(0xFF48484A)

val NothingRed = Color(0xFFD71920)
val DotActive = Color(0xFFE5E5EA)
val DotInactive = Color(0xFF2C2C2E)

enum class AccentColorTheme(
    val id: String,
    val title: String,
    val color: Color
) {
    NOTHING_RED("nothing_red", "Nothing Red", Color(0xFFD71920)),
    CYBER_AMBER("cyber_amber", "Amber", Color(0xFFFF6D00)),
    ELECTRIC_CYAN("electric_cyan", "Cyan", Color(0xFF00E5FF)),
    EMERALD_MINT("emerald_mint", "Mint", Color(0xFF00E676)),
    HYPER_VIOLET("hyper_violet", "Violet", Color(0xFFA855F7)),
    SOLAR_YELLOW("solar_yellow", "Yellow", Color(0xFFFFD600)),
    SAKURA_PINK("sakura_pink", "Pink", Color(0xFFFF4081)),
    PURE_WHITE("pure_white", "White", Color(0xFFFFFFFF));

    companion object {
        fun fromId(id: String): AccentColorTheme =
            values().find { it.id == id } ?: NOTHING_RED
    }
}

enum class SurfaceTheme(
    val id: String,
    val title: String,
    val background: Color,
    val surface: Color,
    val card: Color,
    val border: Color
) {
    OLED_BLACK(
        "oled_black",
        "OLED Black",
        Color(0xFF000000),
        Color(0xFF0D0D0D),
        Color(0xFF141414),
        Color(0xFF262626)
    ),
    GRAPHITE(
        "graphite",
        "Graphite",
        Color(0xFF0C0D0E),
        Color(0xFF131518),
        Color(0xFF1B1D22),
        Color(0xFF2C2F36)
    ),
    MIDNIGHT(
        "midnight",
        "Midnight Blue",
        Color(0xFF080B12),
        Color(0xFF0E1422),
        Color(0xFF141D30),
        Color(0xFF22304E)
    ),
    DEEP_FOREST(
        "deep_forest",
        "Deep Forest",
        Color(0xFF070F0A),
        Color(0xFF0E1A13),
        Color(0xFF14241B),
        Color(0xFF223A2C)
    );

    companion object {
        fun fromId(id: String): SurfaceTheme =
            values().find { it.id == id } ?: OLED_BLACK
    }
}

enum class AppListStyle(
    val id: String,
    val title: String,
    val description: String
) {
    NIAGARA_CLEAN("niagara_clean", "Niagara Clean", "Чистый список без рамок"),
    OLED_CARDS("oled_cards", "OLED Карточки", "Карточки с микро-обводкой"),
    MINIMAL_COMPACT("minimal_compact", "Компактный", "Сверхкомпактный с номерами");

    companion object {
        fun fromId(id: String): AppListStyle =
            values().find { it.id == id } ?: NIAGARA_CLEAN
    }
}

data class LauncherThemeConfig(
    val accent: AccentColorTheme = AccentColorTheme.NOTHING_RED,
    val surface: SurfaceTheme = SurfaceTheme.OLED_BLACK,
    val appListStyle: AppListStyle = AppListStyle.NIAGARA_CLEAN
) {
    val accentColor: Color get() = accent.color
    val backgroundColor: Color get() = surface.background
    val darkSurface: Color get() = surface.surface
    val cardSurface: Color get() = surface.card
    val borderSubtle: Color get() = surface.border
}
