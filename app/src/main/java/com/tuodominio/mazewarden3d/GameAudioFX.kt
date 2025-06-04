package com.marcone1983.mazewarden3d

import android.content.Context
import android.media.MediaPlayer

object GameAudioFX {

    private var stepSound: MediaPlayer? = null
    private var wallHit: MediaPlayer? = null
    private var skillFX: MediaPlayer? = null
    private var bgMusic: MediaPlayer? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) {
            cleanup() // Cleanup previous instances
        }
        
        try {
            stepSound = MediaPlayer.create(context, R.raw.step_fx)?.apply {
                setOnCompletionListener { it.seekTo(0) } // Reset for reuse
            }
            wallHit = MediaPlayer.create(context, R.raw.wall_fx)?.apply {
                setOnCompletionListener { it.seekTo(0) }
            }
            skillFX = MediaPlayer.create(context, R.raw.skill_fx)?.apply {
                setOnCompletionListener { it.seekTo(0) }
            }
            bgMusic = MediaPlayer.create(context, R.raw.main_menu_music)?.apply {
                isLooping = true
                setOnErrorListener { _, _, _ -> true } // Handle errors gracefully
            }
            isInitialized = true
        } catch (e: Exception) {
            // Fallback se i file audio non esistono ancora
            cleanup()
        }
    }

    fun playStep() { 
        try {
            stepSound?.takeIf { !it.isPlaying }?.start()
        } catch (e: Exception) { }
    }
    
    fun playWallHit() { 
        try {
            wallHit?.takeIf { !it.isPlaying }?.start()
        } catch (e: Exception) { }
    }
    
    fun playSkill() { 
        try {
            skillFX?.takeIf { !it.isPlaying }?.start()
        } catch (e: Exception) { }
    }
    
    fun startBackgroundMusic() {
        try {
            bgMusic?.takeIf { !it.isPlaying }?.start()
        } catch (e: Exception) { }
    }
    
    fun pauseBackgroundMusic() {
        try {
            bgMusic?.takeIf { it.isPlaying }?.pause()
        } catch (e: Exception) { }
    }

    fun cleanup() {
        try {
            bgMusic?.apply {
                if (isPlaying) stop()
                release()
            }
            stepSound?.release()
            wallHit?.release()
            skillFX?.release()
            
            stepSound = null
            wallHit = null
            skillFX = null
            bgMusic = null
            isInitialized = false
        } catch (e: Exception) { }
    }
    
    // Legacy method for compatibility
    fun stopAll() = cleanup()
}