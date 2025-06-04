package com.marcone1983.mazewarden3d

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

class VoicePlayer(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingMessages = mutableListOf<String>()

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    // Imposta lingua italiana
                    val result = engine.setLanguage(Locale.ITALIAN)
                    
                    if (result == TextToSpeech.LANG_MISSING_DATA || 
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Fallback a inglese se italiano non disponibile
                        engine.setLanguage(Locale.ENGLISH)
                    }
                    
                    // Configura parametri voce
                    engine.setPitch(1.0f)
                    engine.setSpeechRate(0.9f) // Leggermente più lento per chiarezza
                    
                    // Listener per eventi TTS
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            // Voce iniziata
                        }
                        
                        override fun onDone(utteranceId: String?) {
                            // Voce completata
                        }
                        
                        override fun onError(utteranceId: String?) {
                            // Errore nella sintesi vocale
                        }
                    })
                    
                    isInitialized = true
                    
                    // Riproduci messaggi in coda
                    pendingMessages.forEach { message ->
                        speakNow(message)
                    }
                    pendingMessages.clear()
                }
            } else {
                // TTS non disponibile, modalità silenziosa
                isInitialized = false
            }
        }
    }

    fun say(text: String) {
        if (isInitialized) {
            speakNow(text)
        } else {
            // Aggiungi alla coda se TTS non ancora pronto
            pendingMessages.add(text)
        }
    }

    private fun speakNow(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
    }

    fun sayQueued(text: String) {
        // Aggiunge alla coda invece di sovrascrivere
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "utterance_${System.currentTimeMillis()}")
        } else {
            pendingMessages.add(text)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun pause() {
        tts?.stop()
    }

    fun setVoiceSpeed(speed: Float) {
        tts?.setSpeechRate(speed.coerceIn(0.1f, 3.0f))
    }

    fun setVoicePitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.1f, 2.0f))
    }

    fun isAvailable(): Boolean {
        return isInitialized
    }

    fun getSupportedLanguages(): Set<Locale>? {
        return tts?.availableLanguages
    }

    fun cleanup() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            pendingMessages.clear()
        } catch (e: Exception) {
            // Handle cleanup errors gracefully
        }
    }

    // Metodi di convenienza per messaggi comuni del gioco
    fun announceSkillUsed(characterName: String, skillName: String) {
        say("$characterName ha usato $skillName!")
    }

    fun announceMovement(characterName: String, direction: String) {
        say("$characterName si muove verso $direction")
    }

    fun announceGameEvent(event: String) {
        say(event)
    }

    fun announceTrapTriggered() {
        say("Attenzione! Trappola attivata!")
    }

    fun announceVictory() {
        say("Congratulazioni! Hai completato il labirinto!")
    }

    fun announceGameOver() {
        say("Game Over. Riprova!")
    }

    // Narratore per tutorial esteso
    fun narrateTutorial(stepIndex: Int) {
        val narratives = mapOf(
            0 to "Il labirinto antico si apre davanti a te. Le antiche pietre sussurrano segreti perduti.",
            1 to "Senti il potere scorrere sotto i tuoi piedi. Ogni passo è una scelta.",
            2 to "Gli spiriti del labirinto osservano. Useranno i loro poteri per ostacolarti.",
            3 to "Le trappole dormono nelle ombre. Solo i saggi le evitano.",
            4 to "Il destino ti chiama, Guardiano. Che la tua saggezza ti guidi."
        )
        
        narratives[stepIndex]?.let { narrative ->
            sayQueued(narrative)
        }
    }
}