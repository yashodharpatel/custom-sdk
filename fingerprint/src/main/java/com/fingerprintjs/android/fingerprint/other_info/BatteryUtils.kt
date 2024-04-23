package com.fingerprintjs.android.fingerprint.other_info

import android.Manifest
import android.app.ActivityManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

public class BatteryUtils {
    public companion object {
        public fun collectDeviceInfo(context: Context): List<DeviceInfoItem> {
            val deviceInfoList = mutableListOf<DeviceInfoItem>()

            // Add device information
            deviceInfoList.add(DeviceInfoItem("Is Mute", BatteryUtils.isMute(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Current Audio Volume", BatteryUtils.getCurrentAudioVolume(context, AudioManager.STREAM_MUSIC).toString()))
            deviceInfoList.add(DeviceInfoItem("Battery Charging", BatteryUtils.isBatteryCharging(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Battery Health", BatteryUtils.getBatteryHealth(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Battery Level", BatteryUtils.getBatteryLevel(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Battery Temperature", BatteryUtils.getBatteryTemperature(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Battery Voltage", BatteryUtils.getBatteryVoltage(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Cellular ID", BatteryUtils.getCellularId(context) ?: ""))
            deviceInfoList.add(DeviceInfoItem("Free Storage", BatteryUtils.getFreeStorage().toString()))
            deviceInfoList.add(DeviceInfoItem("Source", BatteryUtils.getSource().toString()))
            deviceInfoList.add(DeviceInfoItem("Last Boot Time", BatteryUtils.getLastBootTime().toString()))
            deviceInfoList.add(DeviceInfoItem("Network Config", BatteryUtils.getNetworkConfig(context)))
            deviceInfoList.add(DeviceInfoItem("Screen Brightness", BatteryUtils.getScreenBrightness(context).toString()))
            deviceInfoList.add(DeviceInfoItem("System Uptime", BatteryUtils.getSystemUptime().toString()))
            deviceInfoList.add(DeviceInfoItem("Wi-Fi Mac Address", BatteryUtils.getWiFiMacAddress(context) ?: ""))
            deviceInfoList.add(DeviceInfoItem("Wi-Fi SSID", BatteryUtils.getWiFiSSID(context) ?: ""))
            deviceInfoList.add(DeviceInfoItem("Airplane Mode On", BatteryUtils.isAirplaneModeOn(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Low Power Mode On", BatteryUtils.isLowPowerModeOn(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Session ID", BatteryUtils.generateSessionId()))
            deviceInfoList.add(DeviceInfoItem("App GUID", BatteryUtils.getAppGuid(context)))
            deviceInfoList.add(DeviceInfoItem("App Version", BatteryUtils.getAppVersion(context)))
            deviceInfoList.add(DeviceInfoItem("Pasteboard Hash", BatteryUtils.getPasteboardHash(context) ?: ""))
            deviceInfoList.add(DeviceInfoItem("Sensor Hash", BatteryUtils.getSensorHash(context)))
            deviceInfoList.add(DeviceInfoItem("Screen Width", BatteryUtils.getScreenWidth(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Screen Height", BatteryUtils.getScreenHeight(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Type", BatteryUtils.getType()))
//            deviceInfoList.add(DeviceInfoItem("IMEI", BatteryUtils.getIMEI(context)))
            deviceInfoList.add(DeviceInfoItem("CPU Speed", BatteryUtils.getCPUSpeed()))
            deviceInfoList.add(DeviceInfoItem("CPU Type", BatteryUtils.getCPUType().toString()))
            deviceInfoList.add(DeviceInfoItem("Device Name", BatteryUtils.getDeviceName()))
            deviceInfoList.add(DeviceInfoItem("Physical Memory", BatteryUtils.getPhysicalMemory(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Kernel Version", BatteryUtils.getKernelVersion()))
            deviceInfoList.add(DeviceInfoItem("Kernel Name", BatteryUtils.getKernelName()))
            deviceInfoList.add(DeviceInfoItem("Kernel Architecture", BatteryUtils.getKernelArch()))
            deviceInfoList.add(DeviceInfoItem("Screen Scale", BatteryUtils.getScreenScale(context).toString()))
            deviceInfoList.add(DeviceInfoItem("Mobile Number", BatteryUtils.getMobileNumber(context) ?: ""))
//            deviceInfoList.add(DeviceInfoItem("Advertising ID", BatteryUtils.getAdvertisingId(context)))
//            deviceInfoList.add(DeviceInfoItem("IMSI", BatteryUtils.getIMSI(context)))
            deviceInfoList.add(DeviceInfoItem("Android Version", BatteryUtils.getAndroidVersion()))
            deviceInfoList.add(DeviceInfoItem("Carrier Country", BatteryUtils.getCarrierCountry(context)))
            deviceInfoList.add(DeviceInfoItem("Carrier Name", BatteryUtils.getCarrierName(context)))
            deviceInfoList.add(DeviceInfoItem("Is Emulator", BatteryUtils.isEmulator().toString()))
            deviceInfoList.add(DeviceInfoItem("Is Rooted", BatteryUtils.isRooted().toString()))
            deviceInfoList.add(DeviceInfoItem("Region Country", BatteryUtils.getRegionCountry()))
            deviceInfoList.add(DeviceInfoItem("Region Language", BatteryUtils.getRegionLanguage()))
            deviceInfoList.add(DeviceInfoItem("Region Timezone", BatteryUtils.getRegionTimezone()))
            deviceInfoList.add(DeviceInfoItem("Build Device", BatteryUtils.getBuildDevice()))
            deviceInfoList.add(DeviceInfoItem("Build ID", BatteryUtils.getBuildId()))
            deviceInfoList.add(DeviceInfoItem("Build Manufacturer", BatteryUtils.getBuildManufacturer()))
            deviceInfoList.add(DeviceInfoItem("Build Time", BatteryUtils.getBuildTime().toString()))

            return deviceInfoList
        }

        public fun isMute(context: Context): Boolean {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0 && audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        }

        public fun getCurrentAudioVolume(context: Context, streamType: Int): Int {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.getStreamVolume(streamType)
        }

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

        public fun getSource(): Int {
            return Build.VERSION.SDK_INT
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
        public fun getWiFiMacAddress(context: Context): String? {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info: WifiInfo? = wifiManager.connectionInfo
            return info?.macAddress
        }

        public fun getWiFiSSID(context: Context): String? {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info: WifiInfo? = wifiManager.connectionInfo
            return info?.ssid
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

        private fun getDisplayMetrics(context: Context): DisplayMetrics {
            val metrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(metrics)
            return metrics
        }


        public fun getScreenWidth(context: Context): Int {
            val metrics = getDisplayMetrics(context)
            return metrics.widthPixels
        }

        public fun getScreenHeight(context: Context): Int {
            val metrics = getDisplayMetrics(context)
            return metrics.heightPixels
        }

        public fun getType(): String {
            return "Android"
        }

        @RequiresApi(Build.VERSION_CODES.O)
        public fun getIMEI(context: Context): String {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.imei ?: ""
        }

        public fun getCPUSpeed(): String {
            return "${Build.BOARD}, ${Build.CPU_ABI}, ${Build.CPU_ABI2}"
        }

        public fun getCPUType(): Int {
            return try {
                val file = File("/sys/devices/system/cpu/")
                val cpuFiles = file.listFiles { _, name -> name.startsWith("cpu") }
                cpuFiles?.size ?: 0
            } catch (e: Exception) {
                0
            }
        }


        public fun getDeviceName(): String {
            return Build.MODEL
        }

        public fun getPhysicalMemory(context: Context): Long {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            return memoryInfo.totalMem
        }


        public fun getKernelVersion(): String {
            return "Version: ${System.getProperty("os.version")}"
        }
        public fun getKernelName(): String {
            return "Name: ${System.getProperty("os.name")}"

        }
        public fun getKernelArch(): String {
            return "Architecture: ${System.getProperty("os.arch")}"
        }
        public fun getScreenScale(context: Context): Float {
            val displayMetrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.density
        }


        public fun getMobileNumber(context: Context): String? {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.line1Number
        }

        public fun getAdvertisingId(context: Context): String {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            return adInfo.id ?: ""
        }

        public fun getIMSI(context: Context): String {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.subscriberId ?: ""
        }

//        public fun getLatitude(context: Context): Double? {
//            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
//            return try {
//                val location: Location? = fusedLocationClient.lastLocation.await()
//                location?.latitude
//            } catch (e: Exception) {
//                null
//            }
//        }
//
//        public fun getLongitude(context: Context): Double? {
//            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
//            return try {
//                val location: Location? = fusedLocationClient.lastLocation.await()
//                location?.longitude
//            } catch (e: Exception) {
//                null
//            }
//        }


        public fun getAndroidVersion(): String {
            return Build.VERSION.RELEASE
        }

        public fun getCarrierCountry(context: Context): String {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.simCountryIso ?: ""
        }

        public fun getCarrierName(context: Context): String {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.networkOperatorName ?: ""
        }

        public fun isEmulator(): Boolean {
            return Build.FINGERPRINT.contains("generic")
                    || Build.FINGERPRINT.contains("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || "google_sdk" == Build.PRODUCT
        }

        public fun isRooted(): Boolean {
            val buildTags = Build.TAGS
            return buildTags != null && buildTags.contains("test-keys")
        }

        public fun getRegionCountry(): String {
            return Locale.getDefault().country
        }

        public fun getRegionLanguage(): String {
            return Locale.getDefault().language
        }

        public fun getRegionTimezone(): String {
            return TimeZone.getDefault().id
        }

        public fun getBuildDevice(): String {
            return Build.DEVICE
        }

        public fun getBuildId(): String {
            return Build.ID
        }

        public fun getBuildManufacturer(): String {
            return Build.MANUFACTURER
        }

//        public fun getBuildNumber(): String {
//            return BuildConfig.BUILD_TYPE
//        }

        public fun getBuildTime(): Long {
            return Build.TIME
        }







    }
}
