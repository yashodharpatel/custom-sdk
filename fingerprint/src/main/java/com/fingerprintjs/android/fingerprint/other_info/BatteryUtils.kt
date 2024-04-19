package com.fingerprintjs.android.fingerprint.other_info

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import java.security.MessageDigest
import java.util.UUID

public class BatteryUtils {
    public companion object {
        public fun getBatteryIntent(context: Context): Intent? {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            return context.registerReceiver(null, intentFilter)
        }

        public fun isBatteryCharging(context: Context): Boolean {
            val batteryStatus = getBatteryIntent(context)
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        }

        public fun getBatteryHealth(context: Context): Int {
            val batteryStatus = getBatteryIntent(context)
            return batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        }

        public fun getBatteryLevel(context: Context): Int {
            val batteryStatus = getBatteryIntent(context)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
        }

        public fun getBatteryTemperature(context: Context): Float {
            val batteryStatus = getBatteryIntent(context)
            val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            return temperature / 10.0f
        }

        public fun getBatteryVoltage(context: Context): Int {
            val batteryStatus = getBatteryIntent(context)
            return batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        }

        // Requires READ_PHONE_STATE permission and potentially carrier privileges
        public fun getCellularId(context: Context): String? {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return telephonyManager.cellLocation?.toString()
            } else {
                return null
            }

        }

        public fun getFreeStorage(): Long {
            val stats = StatFs(Environment.getExternalStorageDirectory().path)
            return stats.availableBlocksLong * stats.blockSizeLong
        }

        public fun getLastBootTime(): Long {
            return System.currentTimeMillis() - SystemClock.elapsedRealtime()
        }

        public fun getNetworkConfig(context: Context): String {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo.toString()
        }

        public fun getScreenBrightness(context: Context): Int {
            return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        }

        public fun getSystemUptime(): Long {
            return SystemClock.uptimeMillis()
        }

        // Requires ACCESS_WIFI_STATE permission
        public fun getWiFiDetails(context: Context): Pair<String, String>? {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifiManager.connectionInfo
            return Pair(info.macAddress, info.ssid)
        }

        public fun isAirplaneModeOn(context: Context): Boolean {
            return Settings.System.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        }

        public fun isLowPowerModeOn(context: Context): Boolean {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isPowerSaveMode
        }

        public fun generateSessionId(): String {
            return UUID.randomUUID().toString()
        }

        public fun getAppGuid(context: Context): String {
            val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            var appGuid = prefs.getString("AppGUID", null)
            if (appGuid == null) {
                appGuid = UUID.randomUUID().toString()
                prefs.edit().putString("AppGUID", appGuid).apply()
            }
            return appGuid
        }

        public fun getAppVersion(context: Context): String {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                return packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return "Unknown"
        }

        public fun getPasteboardHash(context: Context): String? {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip() && clipboard.primaryClip != null) {
                val clipData = clipboard.primaryClip
                if (clipData!!.itemCount > 0) {
                    val item = clipData.getItemAt(0)
                    val text = item.text.toString()
                    return hashString(text, "SHA-256")
                }
            }
            return null
        }

        public fun hashString(input: String, algorithm: String): String {
            return MessageDigest.getInstance(algorithm)
                .digest(input.toByteArray())
                .fold("", { str, it -> str + "%02x".format(it) })
        }

        public fun getSensorHash(context: Context): String {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
            val sensorString = sensors.joinToString { sensor -> "${sensor.name}${sensor.vendor}${sensor.version}" }
            return hashString(sensorString, "SHA-256")
        }










    }
}
