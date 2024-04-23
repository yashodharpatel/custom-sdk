package com.fingerprintjs.android.fingerprint

import androidx.annotation.WorkerThread
import com.fingerprintjs.android.fingerprint.device_id_signals.DeviceIdSignalsProvider
import com.fingerprintjs.android.fingerprint.fingerprinting_signals.FingerprintingSignal
import com.fingerprintjs.android.fingerprint.fingerprinting_signals.FingerprintingSignalsProvider
import com.fingerprintjs.android.fingerprint.info_providers.OsBuildInfoProvider
import com.fingerprintjs.android.fingerprint.signal_providers.SignalGroupProvider
import com.fingerprintjs.android.fingerprint.signal_providers.StabilityLevel
import com.fingerprintjs.android.fingerprint.signal_providers.device_id.DeviceIdProvider
import com.fingerprintjs.android.fingerprint.signal_providers.device_state.DeviceStateSignalGroupProvider
import com.fingerprintjs.android.fingerprint.signal_providers.hardware.HardwareSignalGroupProvider
import com.fingerprintjs.android.fingerprint.signal_providers.installed_apps.InstalledAppsSignalGroupProvider
import com.fingerprintjs.android.fingerprint.signal_providers.os_build.OsBuildSignalGroupProvider
import com.fingerprintjs.android.fingerprint.tools.DeprecationMessages
import com.fingerprintjs.android.fingerprint.tools.FingerprintingLegacySchemeSupportExtensions
import com.fingerprintjs.android.fingerprint.tools.hashers.Hasher
import com.fingerprintjs.android.fingerprint.tools.threading.safe.safe
import kotlin.coroutines.coroutineContext


internal class FingerprinterImpl internal constructor(
    private val legacyArgs: Fingerprinter.LegacyArgs?,
    private val fpSignalsProvider: FingerprintingSignalsProvider,
    private val deviceIdSignalsProvider: DeviceIdSignalsProvider,
    private val OSSignalsProvider: OsBuildInfoProvider,
    ) {
    @Volatile
    private var deviceIdResult: DeviceIdResult? = null

    private var OSResult: OSResult? = null

    private var CPUResult: CPUResult? = null


    @Volatile
    private var fingerprintResult: FingerprintResult? = null

    @WorkerThread
    @Deprecated(DeprecationMessages.DEPRECATED_SYMBOL)
    fun getDeviceId(): Result<DeviceIdResult> {
        require(legacyArgs != null)

        return safe {
            deviceIdResult?.let { return@safe it }
            val deviceIdResult = DeviceIdResult(
                legacyArgs.deviceIdProvider.fingerprint(),
                legacyArgs.deviceIdProvider.rawData().gsfId().value,
                legacyArgs.deviceIdProvider.rawData().androidId().value,
                legacyArgs.deviceIdProvider.rawData().mediaDrmId().value
            )
            this.deviceIdResult = deviceIdResult
            deviceIdResult
        }
    }

    @WorkerThread
    @Deprecated(DeprecationMessages.DEPRECATED_SYMBOL)
    fun getOS(): Result<OSResult> {
        require(legacyArgs != null)

        return safe {
            OSResult?.let { return@safe it }
            val osResult = OSResult(
                legacyArgs.osBuildSignalProvider.rawData().kernelVersion,
                legacyArgs.osBuildSignalProvider.rawData().androidVersion,
                legacyArgs.osBuildSignalProvider.rawData().sdkVersion,
                legacyArgs.osBuildSignalProvider.rawData().fingerprint
            )
            this.OSResult = osResult
            osResult
        }
    }


    @WorkerThread
    @Deprecated(DeprecationMessages.DEPRECATED_SYMBOL)
    fun getCPU(): Result<CPUResult> {
        require(legacyArgs != null)

        return safe {
            CPUResult?.let { return@safe it }
            val cpuResult = CPUResult(
                fpSignalsProvider.procCpuInfoSignal.value,
                fpSignalsProvider.procCpuInfoV2Signal.value,
                fpSignalsProvider.abiTypeSignal.value,
                fpSignalsProvider.coresCountSignal.value
            )
            this.CPUResult = cpuResult
            cpuResult
        }
    }


    @WorkerThread
    fun getDeviceId(version: Fingerprinter.Version): Result<DeviceIdResult> {
        return safe {
            DeviceIdResult(
                deviceId = deviceIdSignalsProvider.getSignalMatching(version).getIdString(),
                gsfId = deviceIdSignalsProvider.gsfIdSignal.getIdString(),
                androidId = deviceIdSignalsProvider.androidIdSignal.getIdString(),
                mediaDrmId = deviceIdSignalsProvider.mediaDrmIdSignal.getIdString(),
            )
        }
    }

    @WorkerThread
    fun getOS(version: Fingerprinter.Version): Result<OSResult> {
        return safe {
            OSResult(
                kernel = OSSignalsProvider.kernelVersion(),
                android = OSSignalsProvider.androidVersion(),
                sdk = OSSignalsProvider.sdkVersion(),
                fingerprint = OSSignalsProvider.fingerprint(),
            )
        }
    }

    @WorkerThread
    fun getCPU(version: Fingerprinter.Version): Result<CPUResult> {
        return safe {
            CPUResult(
                cpuInfo = fpSignalsProvider.procCpuInfoSignal.value,
                cpuInfo2 = fpSignalsProvider.procCpuInfoV2Signal.value,
                abiType = fpSignalsProvider.abiTypeSignal.value,
                coreCount = fpSignalsProvider.coresCountSignal.value,
            )
        }
    }


    @WorkerThread
    @Deprecated(DeprecationMessages.DEPRECATED_SYMBOL)
    fun getFingerprint(
        stabilityLevel: StabilityLevel,
    ): Result<FingerprintResult> {
        require(legacyArgs != null)

        return safe {
            fingerprintResult?.let { return@safe it }
            val fingerprintSb = StringBuilder()

            fingerprintSb.apply {
                append(legacyArgs.hardwareSignalProvider.fingerprint(stabilityLevel))
                append(legacyArgs.osBuildSignalProvider.fingerprint(stabilityLevel))
                append(legacyArgs.deviceStateSignalProvider.fingerprint(stabilityLevel))
                append(legacyArgs.installedAppsSignalProvider.fingerprint(stabilityLevel))
            }

            object : FingerprintResult {
                override val fingerprint = legacyArgs.configuration.hasher.hash(fingerprintSb.toString())

                @Suppress("UNCHECKED_CAST")
                override fun <T : SignalGroupProvider<*>> getSignalProvider(clazz: Class<T>): T? {
                    return when (clazz) {
                        HardwareSignalGroupProvider::class.java -> legacyArgs.hardwareSignalProvider
                        OsBuildSignalGroupProvider::class.java -> legacyArgs.osBuildSignalProvider
                        DeviceStateSignalGroupProvider::class.java -> legacyArgs.deviceStateSignalProvider
                        InstalledAppsSignalGroupProvider::class.java -> legacyArgs.installedAppsSignalProvider
                        DeviceIdProvider::class.java -> legacyArgs.deviceIdProvider
                        else -> null
                    } as? T
                }
            }
        }
    }

    @WorkerThread
    fun getFingerprint(
        version: Fingerprinter.Version,
        stabilityLevel: StabilityLevel,
        hasher: Hasher,
    ): Result<String> {
        return if (version < Fingerprinter.Version.fingerprintingFlattenedSignalsFirstVersion) {
            safe {
                val joinedHashes = with(FingerprintingLegacySchemeSupportExtensions) {
                    listOf(
                        hasher.hash(fpSignalsProvider.getHardwareSignals(version, stabilityLevel)),
                        hasher.hash(fpSignalsProvider.getOsBuildSignals(version, stabilityLevel)),
                        hasher.hash(fpSignalsProvider.getDeviceStateSignals(version, stabilityLevel)),
                        hasher.hash(fpSignalsProvider.getInstalledAppsSignals(version, stabilityLevel)),
                    ).joinToString(separator = "")
                }

                hasher.hash(joinedHashes)
            }
        } else {
            getFingerprint(
                fingerprintingSignals = fpSignalsProvider.getSignalsMatching(version, stabilityLevel),
                hasher = hasher,
            )
        }
    }

    @WorkerThread
    fun getFingerprint(
        fingerprintingSignals: List<FingerprintingSignal<*>>,
        hasher: Hasher,
    ): Result<String> {
        return safe { hasher.hash(fingerprintingSignals) }
    }

    internal fun getFingerprintingSignalsProvider(): FingerprintingSignalsProvider {
        return fpSignalsProvider
    }

    private fun Hasher.hash(fingerprintingSignals: List<FingerprintingSignal<*>>): String {
        val joinedString =
            fingerprintingSignals.joinToString(separator = "") { it.getHashableString() }
        return this.hash(joinedString)
    }
}
