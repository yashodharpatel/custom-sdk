package com.fingerprintjs.android.fingerprint

import com.fingerprintjs.android.fingerprint.signal_providers.StabilityLevel


public data class FingerprintItemData(
    val signalName: String,
    val signalValue: String,
    val stabilityLevel: StabilityLevel,
    val versionStart: Fingerprinter.Version,
    val versionEnd: Fingerprinter.Version
) {
    companion object {
        val EXAMPLE = FingerprintItemData(
            signalName = "Android ID",
            signalValue = """
                {
                    "v1" : "value 1",
                    "v2" : "value 2",
                }
            """.trimIndent(),
            stabilityLevel = StabilityLevel.STABLE,
            versionStart = Fingerprinter.Version.V_2,
            versionEnd = Fingerprinter.Version.V_5,
        )
    }
}

