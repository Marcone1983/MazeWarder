package com.marcone1983.mazewarden3d

import android.content.Context
import android.media.MediaPlayer

object VoiceOver {

    private var narrator: MediaPlayer? = null
    private var isPlaying = false

    fun speak(context: Context, lineId: Int) {
        try {
            cleanup() // Clean previous instance
            narrator = MediaPlayer.create(context, lineId)?.apply {
                setOnCompletionListener {
                    isPlaying = false
                    it.release()
                }
                setOnErrorListener { _, _, _ ->
                    isPlaying = false
                    true
                }
                isPlaying = true
                start()
            }
        } catch (e: Exception) {
            isPlaying = false
        }
    }
    
    fun speakIntro(context: Context) {
        if (!isPlaying) {
            speak(context, R.raw.line_intro)
        }
    }
    
    fun speakSkill(context: Context) {
        if (!isPlaying) {
            speak(context, R.raw.line_skill)
        }
    }
    
    fun speakVictory(context: Context) {
        if (!isPlaying) {
            speak(context, R.raw.line_victory)
        }
    }
    
    fun cleanup() {
        try {
            narrator?.apply {
                if (isPlaying) stop()
                release()
            }
            narrator = null
            isPlaying = false
        } catch (e: Exception) { }
    }
    
    // Legacy method for compatibility
    fun stop() = cleanup()
}