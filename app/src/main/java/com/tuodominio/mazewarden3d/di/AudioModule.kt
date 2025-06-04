package com.marcone1983.mazewarden3d.di

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import com.google.android.exoplayer2.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * AudioModule - Advanced audio system configuration
 * 
 * Provides:
 * - SoundPool for short sound effects (footsteps, walls, abilities)
 * - ExoPlayer for background music and ambient sounds
 * - Audio manager for system audio control
 * - 3D spatial audio configuration
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioManager(
        @ApplicationContext context: Context
    ): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Provides
    @Singleton
    @SfxSoundPool
    fun provideSfxSoundPool(): SoundPool {
        return SoundPool.Builder()
            .setMaxStreams(10)  // Allow up to 10 simultaneous sound effects
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    @Provides
    @Singleton
    @MusicSoundPool  
    fun provideMusicSoundPool(): SoundPool {
        return SoundPool.Builder()
            .setMaxStreams(3)   // For ambient loops and music layers
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideSoundEffectManager(
        @ApplicationContext context: Context,
        @SfxSoundPool soundPool: SoundPool
    ): SoundEffectManager {
        return SoundEffectManager(context, soundPool)
    }
}

/**
 * Qualifiers for different SoundPool instances
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SfxSoundPool

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MusicSoundPool

/**
 * SoundEffectManager - Manages game sound effects with 3D positioning
 */
class SoundEffectManager(
    private val context: Context,
    private val soundPool: SoundPool
) {
    private val soundIds = mutableMapOf<String, Int>()
    
    companion object {
        // Sound effect types
        const val SOUND_FOOTSTEP = "footstep"
        const val SOUND_WALL_PLACE = "wall_place"
        const val SOUND_WALL_BREAK = "wall_break"
        const val SOUND_ABILITY_USE = "ability_use"
        const val SOUND_TELEPORT = "teleport"
        const val SOUND_VICTORY = "victory"
        const val SOUND_TURN_CHANGE = "turn_change"
    }
    
    init {
        loadSoundEffects()
    }
    
    private fun loadSoundEffects() {
        try {
            // Load sound files from assets
            soundIds[SOUND_FOOTSTEP] = soundPool.load(context.assets.openFd("audio/sfx/footstep.ogg"), 1)
            soundIds[SOUND_WALL_PLACE] = soundPool.load(context.assets.openFd("audio/sfx/wall_place.ogg"), 1)
            soundIds[SOUND_WALL_BREAK] = soundPool.load(context.assets.openFd("audio/sfx/wall_break.ogg"), 1)
            soundIds[SOUND_ABILITY_USE] = soundPool.load(context.assets.openFd("audio/sfx/ability_use.ogg"), 1)
            soundIds[SOUND_TELEPORT] = soundPool.load(context.assets.openFd("audio/sfx/teleport.ogg"), 1)
            soundIds[SOUND_VICTORY] = soundPool.load(context.assets.openFd("audio/sfx/victory.ogg"), 1)
            soundIds[SOUND_TURN_CHANGE] = soundPool.load(context.assets.openFd("audio/sfx/turn_change.ogg"), 1)
        } catch (e: Exception) {
            // Handle missing audio files gracefully
            e.printStackTrace()
        }
    }
    
    fun playSound(soundType: String, volume: Float = 1.0f, pitch: Float = 1.0f) {
        soundIds[soundType]?.let { soundId ->
            soundPool.play(soundId, volume, volume, 1, 0, pitch)
        }
    }
    
    fun playSpatialSound(
        soundType: String, 
        x: Float, 
        y: Float, 
        z: Float, 
        volume: Float = 1.0f
    ) {
        // Calculate stereo positioning based on 3D coordinates
        val pan = (x / 10f).coerceIn(-1f, 1f)  // Convert world coords to stereo pan
        val distance = kotlin.math.sqrt(x*x + y*y + z*z)
        val attenuatedVolume = (volume / (1 + distance * 0.1f)).coerceIn(0f, 1f)
        
        soundIds[soundType]?.let { soundId ->
            soundPool.play(soundId, 
                if (pan <= 0) attenuatedVolume else attenuatedVolume * (1 - pan),  // left volume
                if (pan >= 0) attenuatedVolume else attenuatedVolume * (1 + pan),  // right volume
                1, 0, 1.0f)
        }
    }
    
    fun cleanup() {
        soundPool.release()
    }
}