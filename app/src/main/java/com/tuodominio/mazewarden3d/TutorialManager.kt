package com.marcone1983.mazewarden3d

import android.content.Context
import android.widget.TextView

object TutorialManager {
    val steps = listOf(
        "Benvenuto nel Labirinto. Ogni passo conta.",
        "Muoviti usando le frecce direzionali.",
        "Ogni personaggio ha una sola skill speciale: usala con saggezza.",
        "Evita le trappole e raggiungi il centro del labirinto.",
        "Buona fortuna, Guardiano."
    )

    var currentStep = 0
    var isActive = false

    fun start(context: Context, speaker: VoicePlayer, screen: TextView) {
        currentStep = 0
        isActive = true
        screen.text = steps[currentStep]
        speaker.say(steps[currentStep])
        
        // Effetti visivi per il tutorial
        screen.setTextColor(android.graphics.Color.YELLOW)
        screen.textSize = 18f
        screen.setPadding(20, 20, 20, 20)
        screen.setBackgroundColor(android.graphics.Color.argb(128, 0, 0, 0))
    }

    fun next(context: Context, speaker: VoicePlayer, screen: TextView) {
        if (!isActive) return
        
        currentStep++
        if (currentStep < steps.size) {
            screen.text = steps[currentStep]
            speaker.say(steps[currentStep])
            
            // Animazione di transizione
            screen.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    screen.text = steps[currentStep]
                    screen.animate().alpha(1f).setDuration(300)
                }
        } else {
            complete(speaker, screen)
        }
    }

    fun complete(speaker: VoicePlayer, screen: TextView) {
        screen.text = "Tutorial completato."
        speaker.say("Ora tocca a te.")
        isActive = false
        
        // Nasconde il tutorial dopo 2 secondi
        screen.postDelayed({
            screen.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction {
                    screen.visibility = android.view.View.GONE
                }
        }, 2000)
    }

    fun skip(speaker: VoicePlayer, screen: TextView) {
        speaker.say("Tutorial saltato.")
        isActive = false
        screen.visibility = android.view.View.GONE
    }

    fun getProgress(): String {
        return if (isActive) {
            "Step ${currentStep + 1}/${steps.size}"
        } else {
            "Tutorial completato"
        }
    }

    fun getCurrentStepHint(): String {
        return when (currentStep) {
            0 -> "Tocca lo schermo per continuare"
            1 -> "Usa i controlli di movimento"
            2 -> "Premi il pulsante skill quando disponibile"
            3 -> "Evita le celle rosse (trappole)"
            4 -> "Inizia la tua avventura!"
            else -> ""
        }
    }

    fun isStepCompleted(stepIndex: Int): Boolean {
        return currentStep > stepIndex
    }

    fun reset() {
        currentStep = 0
        isActive = false
    }
}