package com.zenlauncher.minimalist.modes

data class LauncherMode(
    val id: String,
    val name: String,
    val tag: String,
    val isDefault: Boolean = false,
    val isDeletable: Boolean = true
) {
    companion object {
        val DEFAULT = LauncherMode(id = "default", name = "Обычный", tag = "STD", isDefault = true, isDeletable = false)
        val WORK = LauncherMode(id = "work", name = "Работа", tag = "WRK", isDeletable = false)
        val RELAX = LauncherMode(id = "relax", name = "Отдых", tag = "RLX", isDeletable = false)
        val SLEEP = LauncherMode(id = "sleep", name = "Сон", tag = "SLP", isDeletable = false)
        val SPORT = LauncherMode(id = "sport", name = "Спорт", tag = "SPT", isDeletable = false)

        val BUILT_IN = listOf(DEFAULT, WORK, RELAX, SLEEP, SPORT)
    }
}
