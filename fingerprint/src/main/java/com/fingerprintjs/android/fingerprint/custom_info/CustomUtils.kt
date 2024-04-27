package com.fingerprintjs.android.fingerprint.custom_info

import android.Manifest.permission.READ_PHONE_NUMBERS
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.READ_SMS
import android.app.ActivityManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
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
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.fingerprintjs.android.fingerprint.Fingerprinter
import com.fingerprintjs.android.fingerprint.FingerprinterFactory
import com.fingerprintjs.android.fingerprint.api.DataModal
import com.fingerprintjs.android.fingerprint.api.RestApiService
import com.fingerprintjs.android.fingerprint.signal_providers.StabilityLevel
import com.fingerprintjs.android.fingerprint.toFingerprintItemData
import com.google.gson.Gson
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

public class CustomUtils {
    public companion object {
        public fun collectDeviceInfo(context: Context): List<DeviceInfoItem> {
            val deviceInfoList = mutableListOf<DeviceInfoItem>()
            val fingerPrinter = FingerprinterFactory.create(context)

            // Usage
            fingerPrinter.getFingerprint(version = Fingerprinter.Version.V_5) { fingerprint ->
                deviceInfoList.add(DeviceInfoItem("Fingerprint", fingerprint))
            }

            fingerPrinter.getDeviceId(version = Fingerprinter.Version.V_5) { result ->
                deviceInfoList.add(DeviceInfoItem("Device ID", result.deviceId))
                deviceInfoList.add(DeviceInfoItem("Android ID", result.androidId ?: "Unavailable"))
                deviceInfoList.add(DeviceInfoItem("GSF ID", result.gsfId ?: "Unavailable"))
                deviceInfoList.add(
                    DeviceInfoItem(
                        "Media DRM ID",
                        result.mediaDrmId ?: "Unavailable"
                    )
                )
            }
            val signals = fingerPrinter.getFingerprintingSignalsProvider()?.getSignalsMatching(
                version = Fingerprinter.Version.V_5,
                stabilityLevel = StabilityLevel.STABLE
            ).orEmpty()
            signals.mapIndexed { _, signal ->
                val data = signal.toFingerprintItemData()
                deviceInfoList.add(DeviceInfoItem(data.signalName, data.signalValue.toString()))
            }
            deviceInfoList.add(DeviceInfoItem("Is Mute", isMute(context).toString()))
            deviceInfoList.add(
                DeviceInfoItem(
                    "Current Audio Volume",
                    getCurrentAudioVolume(context, AudioManager.STREAM_MUSIC).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Battery Charging",
                    isBatteryCharging(context).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Battery Health",
                    getBatteryHealth(context).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Battery Level",
                    getBatteryLevel(context).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Battery Temperature",
                    getBatteryTemperature(context).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Battery Voltage",
                    getBatteryVoltage(context).toString()
                )
            )

            deviceInfoList.add(
                DeviceInfoItem(
                    "Free Storage",
                    getFreeStorage().toString()
                )
            )
            deviceInfoList.add(DeviceInfoItem("Source", getSource().toString()))
            deviceInfoList.add(
                DeviceInfoItem(
                    "Last Boot Time",
                    getLastBootTime().toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Network Config",
                    getNetworkConfig(context)
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Screen Brightness",
                    getScreenBrightness(context).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "System Uptime",
                    getSystemUptime().toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Wi-Fi Mac Address",
                    getWiFiMacAddress(context) ?: ""
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Airplane Mode On",
                    isAirplaneModeOn(context).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Low Power Mode On",
                    isLowPowerModeOn(context).toString()
                )
            )
            deviceInfoList.add(DeviceInfoItem("Session ID", generateSessionId()))
            deviceInfoList.add(DeviceInfoItem("App GUID", getAppGuid(context)))
            deviceInfoList.add(DeviceInfoItem("App Version", getAppVersion(context)))
            deviceInfoList.add(
                DeviceInfoItem(
                    "Pasteboard Hash",
                    getPasteboardHash(context) ?: ""
                )
            )
            deviceInfoList.add(DeviceInfoItem("Sensor Hash", getSensorHash(context)))
            deviceInfoList.add(
                DeviceInfoItem(
                    "Screen Width",
                    getScreenWidth(context).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Screen Height",
                    getScreenHeight(context).toString()
                )
            )
            deviceInfoList.add(DeviceInfoItem("Type", getType()))
            deviceInfoList.add(DeviceInfoItem("CPU Speed", getCPUSpeed()))
            deviceInfoList.add(DeviceInfoItem("CPU Type", getCPUType().toString()))
            deviceInfoList.add(DeviceInfoItem("Device Name", getDeviceName()))
            deviceInfoList.add(
                DeviceInfoItem(
                    "Physical Memory",
                    getPhysicalMemory(context).toString()
                )
            )
            deviceInfoList.add(DeviceInfoItem("Kernel Version", getKernelVersion()))
            deviceInfoList.add(DeviceInfoItem("Kernel Name", getKernelName()))
            deviceInfoList.add(DeviceInfoItem("Kernel Architecture", getKernelArch()))
            deviceInfoList.add(
                DeviceInfoItem(
                    "Screen Scale",
                    getScreenScale(context).toString()
                )
            )
            deviceInfoList.add(
                DeviceInfoItem(
                    "Mobile Number",
                    getMobileNumber(context) ?: ""
                )
            )
            deviceInfoList.add(DeviceInfoItem("Android Version", getAndroidVersion()))
            deviceInfoList.add(
                DeviceInfoItem(
                    "Carrier Country",
                    getCarrierCountry(context)
                )
            )
            deviceInfoList.add(DeviceInfoItem("Carrier Name", getCarrierName(context)))
            deviceInfoList.add(DeviceInfoItem("Is Emulator", isEmulator().toString()))
            deviceInfoList.add(DeviceInfoItem("Is Rooted", isRooted().toString()))
            deviceInfoList.add(DeviceInfoItem("Region Country", getRegionCountry()))
            deviceInfoList.add(DeviceInfoItem("Region Language", getRegionLanguage()))
            deviceInfoList.add(DeviceInfoItem("Region Timezone", getRegionTimezone()))
            deviceInfoList.add(DeviceInfoItem("Build Device", getBuildDevice()))
            deviceInfoList.add(DeviceInfoItem("Build ID", getBuildId()))
            deviceInfoList.add(
                DeviceInfoItem(
                    "Build Manufacturer",
                    getBuildManufacturer()
                )
            )
            deviceInfoList.add(DeviceInfoItem("Build Time", getBuildTime().toString()))
            val json = convertListToJson(deviceInfoList)
            val jsonBytes = json.toByteArray(Charsets.UTF_8)
            val body = Base64.encodeToString(jsonBytes, Base64.DEFAULT)
            callApi(body)
            return deviceInfoList
        }

        private fun callApi(body: String) {
            val apiService = RestApiService()
            val userInfo = DataModal(
                payload = body
            )

            apiService.addPayload(userInfo) { response ->
                if (response != null) {
                    if (response.message == "Successfully created payload") {
                        Log.i("API SUCCESS", response.data.toString())
                    } else {
                        Log.i("API FAILURE", response.data.toString())
                    }
                } else {
                    Log.i("API FAILURE", "Response is null")
                }
            }

        }


        public fun convertListToJson(deviceInfoList: List<DeviceInfoItem>): String {
            val map = deviceInfoList.associate { it.title to it.detail }
            val gson = Gson()
            return gson.toJson(map)
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

        public fun getCellularId(context: Context): String {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.deviceId
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
            if (ActivityCompat.checkSelfPermission(
                    context,
                    READ_SMS
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                return telephonyManager.line1Number
            } else {
                return ""
            }
        }

        public fun getIMSI(context: Context): String {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.subscriberId ?: ""
        }

        public fun getIMEI(context: Context): String {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager!!.deviceId ?: ""
        }

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

        public fun getBuildTime(): Long {
            return Build.TIME
        }
    }
}
