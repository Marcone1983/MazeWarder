package com.tuodominio.mazewarden3d

import android.content.Context
import android.media.AudioManager

object SettingsUtils {

    fun setVolume(context: Context, percent: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newVolume = (maxVolume * percent) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
    }

    fun getVolume(context: Context): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return (currentVolume * 100) / maxVolume
    }
}