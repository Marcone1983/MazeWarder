package com.tuodominio.mazewarden3d

import android.content.Context
import android.media.MediaPlayer

object VoiceOver {

    private var narrator: MediaPlayer? = null

    fun speak(context: Context, lineId: Int) {
        try {
            narrator?.release()
            narrator = MediaPlayer.create(context, lineId)
            narrator?.start()
        } catch (e: Exception) {
            // Fallback se file audio non esiste
        }
    }
    
    fun speakIntro(context: Context) {
        try {
            speak(context, R.raw.line_intro)
        } catch (e: Exception) { }
    }
    
    fun speakSkill(context: Context) {
        try {
            speak(context, R.raw.line_skill)
        } catch (e: Exception) { }
    }
    
    fun speakVictory(context: Context) {
        try {
            speak(context, R.raw.line_victory)
        } catch (e: Exception) { }
    }
    
    fun stop() {
        narrator?.stop()
        narrator?.release()
        narrator = null
    }
}