package com.zenlauncher.minimalist.battery

enum class DeviceType {
    PHONE,
    HEADPHONES,
    WATCH,
    STYLUS,
    SPEAKER,
    OTHER
}

data class ConnectedDevice(
    val id: String,
    val name: String,
    val batteryLevel: Int, // 0..100 (-1 if unavailable)
    val type: DeviceType,
    val isCharging: Boolean = false
)
