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
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectedDevicesManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? = try {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    } catch (e: Throwable) {
        null
    }
    private val bluetoothAdapter: BluetoothAdapter? = try {
        bluetoothManager?.adapter
    } catch (e: Throwable) {
        null
    }

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
            try {
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
            } catch (e: Throwable) {
                Log.w("ConnectedDevicesManager", "Error in receiver", e)
            }
        }
    }

    init {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            // Android 14 (API 34+) compatibility flag: RECEIVER_EXPORTED
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        } catch (e: Throwable) {
            Log.e("ConnectedDevicesManager", "Failed to register battery receiver", e)
        }

        refreshConnectedBluetoothDevices()
    }

    fun hasBluetoothPermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun refreshConnectedBluetoothDevices() {
        try {
            if (!hasBluetoothPermission() || bluetoothAdapter == null) {
                _connectedAccessories.value = emptyList()
                return
            }

            if (!bluetoothAdapter.isEnabled) {
                _connectedAccessories.value = emptyList()
                return
            }

            val bonded = bluetoothAdapter.bondedDevices ?: return
            val connectedList = mutableListOf<ConnectedDevice>()

            for (device in bonded) {
                val batteryLevel = getDeviceBattery(device)
                if (batteryLevel >= 0) {
                    val type = resolveDeviceType(device)
                    val name = try {
                        device.name ?: "Устройство"
                    } catch (e: Throwable) {
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
        } catch (e: Throwable) {
            Log.w("ConnectedDevicesManager", "Error querying Bluetooth devices", e)
            _connectedAccessories.value = emptyList()
        }
    }

    private fun getDeviceBattery(device: BluetoothDevice): Int {
        return try {
            val method = device.javaClass.getMethod("getBatteryLevel")
            val level = method.invoke(device) as? Int ?: -1
            level
        } catch (e: Throwable) {
            -1
        }
    }

    private fun resolveDeviceType(device: BluetoothDevice): DeviceType {
        return try {
            val name = device.name?.lowercase() ?: ""
            if (name.contains("buds") || name.contains("airpods") || name.contains("headset") || name.contains("наушники")) {
                return DeviceType.HEADPHONES
            }
            if (name.contains("watch") || name.contains("часы") || name.contains("band")) {
                return DeviceType.WATCH
            }
            if (name.contains("pen") || name.contains("spen") || name.contains("стилус")) {
                return DeviceType.STYLUS
            }

            val btClass = device.bluetoothClass
            if (btClass != null) {
                val major = btClass.majorDeviceClass
                if (major == BluetoothClass.Device.Major.AUDIO_VIDEO) return DeviceType.HEADPHONES
                if (major == BluetoothClass.Device.Major.WEARABLE) return DeviceType.WATCH
            }

            DeviceType.OTHER
        } catch (e: Throwable) {
            DeviceType.OTHER
        }
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Throwable) {
            // Ignore
        }
    }
}
