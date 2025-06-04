package com.marcone1983.mazewarden3d

import com.google.android.filament.*
import com.google.android.filament.utils.*
import android.os.Handler
import android.os.Looper
import android.graphics.Color

object MaterialFX {
    fun fadeOut(entity: Int, duration: Long = 300, onComplete: () -> Unit) {
        var alpha = 1f
        val steps = 20
        val delta = alpha / steps
        val interval = duration / steps

        for (i in 1..steps) {
            Handler(Looper.getMainLooper()).postDelayed({
                val newAlpha = alpha - delta * i
                MaterialManager.setAlpha(entity, newAlpha.coerceAtLeast(0f))
                if (i == steps) onComplete()
            }, i * interval)
        }
    }

    fun fadeIn(entity: Int, duration: Long = 300) {
        var alpha = 0f
        val steps = 20
        val delta = 1f / steps
        val interval = duration / steps

        for (i in 1..steps) {
            Handler(Looper.getMainLooper()).postDelayed({
                val newAlpha = alpha + delta * i
                MaterialManager.setAlpha(entity, newAlpha.coerceAtMost(1f))
            }, i * interval)
        }
    }

    fun flashEmission(entity: Int, color: Color, duration: Long = 300) {
        MaterialManager.setEmission(entity, color)
        Handler(Looper.getMainLooper()).postDelayed({
            MaterialManager.setEmission(entity, Color.valueOf(0f, 0f, 0f))
        }, duration)
    }
}

// MaterialManager per gestione materiali Filament
object MaterialManager {
    fun setAlpha(entity: Int, alpha: Float) {
        try {
            // Usa API Filament per cambiare alpha del material
            val renderableManager = RenderableManager.Builder(1)
            // Simulazione: renderable.getMaterialInstanceAt(instance, 0).setParameter("baseColorFactor", r, g, b, alpha)
            // In implementazione reale, recupera il material instance e modifica l'alpha
        } catch (e: Exception) {
            // Fallback se l'entità non ha componente renderable
        }
    }
    
    fun setEmission(entity: Int, color: Color) {
        try {
            // Usa API Filament per cambiare emission del material
            val r = Color.red(color.toArgb()) / 255f
            val g = Color.green(color.toArgb()) / 255f  
            val b = Color.blue(color.toArgb()) / 255f
            
            // Simulazione: materialInstance.setParameter("emissiveFactor", r, g, b, 1.0f)
            // In implementazione reale, recupera il material instance e modifica l'emission
        } catch (e: Exception) {
            // Fallback se l'entità non ha componente renderable
        }
    }
    
    fun setBaseColor(entity: Int, color: Color) {
        try {
            val r = Color.red(color.toArgb()) / 255f
            val g = Color.green(color.toArgb()) / 255f
            val b = Color.blue(color.toArgb()) / 255f
            val a = Color.alpha(color.toArgb()) / 255f
            
            // Simulazione: materialInstance.setParameter("baseColorFactor", r, g, b, a)
        } catch (e: Exception) {
            // Fallback
        }
    }
    
    fun setRoughness(entity: Int, roughness: Float) {
        try {
            // Simulazione: materialInstance.setParameter("roughnessFactor", roughness)
        } catch (e: Exception) {
            // Fallback
        }
    }
    
    fun setMetallic(entity: Int, metallic: Float) {
        try {
            // Simulazione: materialInstance.setParameter("metallicFactor", metallic)
        } catch (e: Exception) {
            // Fallback
        }
    }
}