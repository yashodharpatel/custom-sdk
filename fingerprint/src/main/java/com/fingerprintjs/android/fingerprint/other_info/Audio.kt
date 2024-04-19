package com.fingerprintjs.android.fingerprint.other_info

import android.content.Context
import android.media.AudioManager

public class Audio {
    public companion object {
        public fun isMute(context: Context): Boolean {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0 && audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        }
        public fun getCurrentAudioVolume(context: Context, streamType: Int): Int {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return audioManager.getStreamVolume(streamType)
        }

    }
}

