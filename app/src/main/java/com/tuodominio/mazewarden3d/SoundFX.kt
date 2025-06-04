package com.marcone1983.mazewarden3d

import com.google.android.filament.utils.*
import android.content.Context
import android.media.SoundPool
import android.media.AudioAttributes
import kotlin.math.sqrt

object SoundFX {
    fun play(file: String) {
        // Play sound globally (short sfx)
        AudioEngine.play(file)
    }

    fun play3D(file: String, x: Float, z: Float) {
        AudioEngine.playSpatial(file, position = Vector3(x, 0f, z))
    }
}

// AudioEngine per gestione audio avanzata
object AudioEngine {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private var context: Context? = null
    
    // Posizione camera per calcoli 3D
    private var cameraPosition = Vector3(0f, 6f, 6f)
    
    fun initialize(ctx: Context) {
        context = ctx
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()
            
        loadSounds()
    }
    
    private fun loadSounds() {
        context?.let { ctx ->
            soundPool?.let { pool ->
                // Carica suoni dalla cartella raw
                try {
                    soundMap["smash.wav"] = pool.load(ctx, getResId("smash", "raw"), 1)
                    soundMap["teleport.wav"] = pool.load(ctx, getResId("teleport", "raw"), 1)
                    soundMap["zap.wav"] = pool.load(ctx, getResId("zap", "raw"), 1)
                    soundMap["wall_deploy.wav"] = pool.load(ctx, getResId("wall_deploy", "raw"), 1)
                } catch (e: Exception) {
                    // Fallback se i file audio non esistono
                }
            }
        }
    }
    
    private fun getResId(name: String, type: String): Int {
        return context?.resources?.getIdentifier(name, type, context?.packageName) ?: 0
    }
    
    fun play(file: String, volume: Float = 1f) {
        soundPool?.let { pool ->
            soundMap[file]?.let { soundId ->
                pool.play(soundId, volume, volume, 1, 0, 1f)
            }
        }
    }
    
    fun playSpatial(file: String, position: Vector3, volume: Float = 1f) {
        // Calcola volume e panning 3D
        val distance = calculateDistance(cameraPosition, position)
        val spatialVolume = calculateSpatialVolume(distance, volume)
        val panning = calculatePanning(position)
        
        soundPool?.let { pool ->
            soundMap[file]?.let { soundId ->
                pool.play(soundId, spatialVolume * panning.left, spatialVolume * panning.right, 1, 0, 1f)
            }
        }
    }
    
    fun setCameraPosition(x: Float, y: Float, z: Float) {
        cameraPosition = Vector3(x, y, z)
    }
    
    private fun calculateDistance(pos1: Vector3, pos2: Vector3): Float {
        val dx = pos1.x - pos2.x
        val dy = pos1.y - pos2.y
        val dz = pos1.z - pos2.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
    
    private fun calculateSpatialVolume(distance: Float, baseVolume: Float): Float {
        // Attenuazione inversa al quadrato della distanza
        val maxDistance = 20f
        val minVolume = 0.1f
        
        if (distance >= maxDistance) return minVolume
        
        val attenuation = 1f / (1f + distance * distance * 0.1f)
        return (baseVolume * attenuation).coerceAtLeast(minVolume)
    }
    
    private fun calculatePanning(position: Vector3): StereoChannel {
        // Calcola panning stereo basato sulla posizione X relativa alla camera
        val deltaX = position.x - cameraPosition.x
        val maxPanDistance = 10f
        
        val panFactor = (deltaX / maxPanDistance).coerceIn(-1f, 1f)
        
        return when {
            panFactor < 0 -> StereoChannel(1f, 1f + panFactor) // Più a sinistra
            panFactor > 0 -> StereoChannel(1f - panFactor, 1f) // Più a destra
            else -> StereoChannel(1f, 1f) // Centro
        }
    }
    
    fun cleanup() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}

// Data classes per audio spaziale
data class Vector3(val x: Float, val y: Float, val z: Float)

data class StereoChannel(val left: Float, val right: Float)

// Extension per retrocompatibilità con ENGINE PROPRIETARIO
fun SoundFX.playImpact(file: String, position: Vector3 = Vector3(0f, 0f, 0f)) {
    play3D(file, position.x, position.z)
}