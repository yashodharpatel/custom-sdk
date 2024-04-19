package com.fingerprintjs.android.fingerprint

import android.annotation.SuppressLint
import com.fingerprintjs.android.fingerprint.fingerprinting_signals.FingerprintingSignal

@SuppressLint("DiscouragedApi")
public fun FingerprintingSignal<*>.toFingerprintItemData(): FingerprintItemData {
    return FingerprintItemData(
        signalName = this.humanName,
        signalValue = this.humanValue,
        stabilityLevel = this.info.stabilityLevel,
        versionStart = this.info.addedInVersion,
        versionEnd = this.info.removedInVersion ?: Fingerprinter.Version.latest,
    )
}
