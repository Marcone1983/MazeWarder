package com.marcone1983.mazewarden3d

import com.google.android.filament.*
import com.google.android.filament.utils.*
import android.os.Handler
import android.os.Looper

object Animator {
    private val handler = Handler(Looper.getMainLooper())

    fun playIdle(entity: Int) {
        AnimationManager.play(entity, "Idle")
    }

    fun playWalk(entity: Int) {
        AnimationManager.play(entity, "Walk")
    }

    fun playCast(entity: Int) {
        AnimationManager.play(entity, "Cast")
    }

    fun translateTo(entity: Int, x: Float, y: Float, z: Float, duration: Long = 600, onComplete: () -> Unit = {}) {
        // Simulazione getTransform per compatibilità
        val startPos = Float3(0f, 0f, 0f) // Placeholder per posizione corrente
        val steps = 30
        val dx = (x - startPos.x) / steps
        val dz = (z - startPos.z) / steps

        for (i in 0..steps) {
            handler.postDelayed({
                val newX = startPos.x + dx * i
                val newZ = startPos.z + dz * i
                
                // Usa l'API Filament corretta
                val transformManager = TransformManager.get()
                val instance = transformManager.getInstance(entity)
                transformManager.setTransform(instance, floatArrayOf(
                    1f, 0f, 0f, newX,
                    0f, 1f, 0f, y,
                    0f, 0f, 1f, newZ,
                    0f, 0f, 0f, 1f
                ))
                
                if (i == steps) onComplete()
            }, i * (duration / steps))
        }
    }

    fun fadeOutIn(entity: Int, onMid: () -> Unit) {
        MaterialFX.fadeOut(entity, duration = 300) {
            onMid()
            MaterialFX.fadeIn(entity, duration = 300)
        }
    }
}

// AnimationManager per gestione animazioni glTF
object AnimationManager {
    fun play(entity: Int, animationName: String) {
        // Placeholder per sistema animazioni
        // In implementazione reale: gltfAsset.animator?.applyAnimation(index, time)
    }
}

