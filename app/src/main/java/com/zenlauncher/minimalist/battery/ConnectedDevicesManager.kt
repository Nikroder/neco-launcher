package com.zenlauncher.minimalist.battery

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectedDevicesManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_MANAGER_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _phoneDevice = MutableStateFlow(
        ConnectedDevice(
            id = "phone_internal",
            name = "Телефон",
            batteryLevel = 100,
            type = DeviceType.PHONE,
            isCharging = false
        )
    )
    val phoneDevice: StateFlow<ConnectedDevice> = _phoneDevice.asStateFlow()

    private val _connectedAccessories = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    val connectedAccessories: StateFlow<List<ConnectedDevice>> = _connectedAccessories.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val percent = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 100
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    _phoneDevice.value = _phoneDevice.value.copy(
                        batteryLevel = percent,
                        isCharging = isCharging
                    )
                }
                "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED",
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    refreshConnectedBluetoothDevices()
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)
        refreshConnectedBluetoothDevices()
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun refreshConnectedBluetoothDevices() {
        if (!hasBluetoothPermission() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectedAccessories.value = emptyList()
            return
        }

        try {
            val bonded = bluetoothAdapter.bondedDevices ?: return
            val connectedList = mutableListOf<ConnectedDevice>()

            for (device in bonded) {
                // Check if device is connected via reflection or profile
                val batteryLevel = getDeviceBattery(device)
                if (batteryLevel >= 0) {
                    val type = resolveDeviceType(device)
                    val name = try {
                        device.name ?: "Устройство"
                    } catch (e: SecurityException) {
                        "Bluetooth-устройство"
                    }
                    connectedList.add(
                        ConnectedDevice(
                            id = device.address ?: name,
                            name = name,
                            batteryLevel = batteryLevel,
                            type = type,
                            isCharging = false
                        )
                    )
                }
            }

            _connectedAccessories.value = connectedList
        } catch (e: SecurityException) {
            // Permission denied
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun getDeviceBattery(device: BluetoothDevice): Int {
        return try {
            val method = device.javaClass.getMethod("getBatteryLevel")
            val level = method.invoke(device) as? Int ?: -1
            level
        } catch (e: Exception) {
            -1
        }
    }

    private fun resolveDeviceType(device: BluetoothDevice): DeviceType {
        val name = try { device.name?.lowercase() ?: "" } catch (e: Exception) { "" }
        if (name.contains("buds") || name.contains("airpods") || name.contains("headset") || name.contains("наушники")) {
            return DeviceType.HEADPHONES
        }
        if (name.contains("watch") || name.contains("часы") || name.contains("band")) {
            return DeviceType.WATCH
        }
        if (name.contains("pen") || name.contains("spen") || name.contains("стилус")) {
            return DeviceType.STYLUS
        }

        val btClass = try { device.bluetoothClass } catch (e: Exception) { null }
        if (btClass != null) {
            val major = btClass.majorDeviceClass
            if (major == BluetoothClass.Device.Major.AUDIO_VIDEO) return DeviceType.HEADPHONES
            if (major == BluetoothClass.Device.Major.WEARABLE) return DeviceType.WATCH
        }

        return DeviceType.OTHER
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
