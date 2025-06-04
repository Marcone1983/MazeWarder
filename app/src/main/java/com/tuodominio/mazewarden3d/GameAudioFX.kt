package com.tuodominio.mazewarden3d

import android.content.Context
import android.media.MediaPlayer

object GameAudioFX {

    private var stepSound: MediaPlayer? = null
    private var wallHit: MediaPlayer? = null
    private var skillFX: MediaPlayer? = null
    private var bgMusic: MediaPlayer? = null

    fun init(context: Context) {
        try {
            stepSound = MediaPlayer.create(context, R.raw.step_fx)
            wallHit = MediaPlayer.create(context, R.raw.wall_fx)
            skillFX = MediaPlayer.create(context, R.raw.skill_fx)
            bgMusic = MediaPlayer.create(context, R.raw.main_menu_music)?.apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            // Fallback se i file audio non esistono ancora
        }
    }

    fun playStep() { 
        try {
            stepSound?.start() 
        } catch (e: Exception) { }
    }
    
    fun playWallHit() { 
        try {
            wallHit?.start() 
        } catch (e: Exception) { }
    }
    
    fun playSkill() { 
        try {
            skillFX?.start() 
        } catch (e: Exception) { }
    }

    fun stopAll() {
        try {
            bgMusic?.stop()
            stepSound?.release()
            wallHit?.release()
            skillFX?.release()
        } catch (e: Exception) { }
    }
}