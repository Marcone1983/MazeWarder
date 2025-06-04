package com.marcone1983.mazewarden3d

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.speech.tts.TextToSpeech
import android.content.Intent
import java.util.*

class IntroScene : Activity() {

    private lateinit var tts: TextToSpeech
    private val phrases = listOf(
        "Nel cuore del labirinto, il tempo si è fermato.",
        "I Guardiani sono l'ultima speranza dell'umanità.",
        "Solo chi conosce i muri può trovare la via.",
        "Il destino di tutti è nelle tue mani, Guardiano.",
        "Che la tua saggezza ti guidi attraverso le ombre."
    )
    private var index = 0
    private lateinit var text: TextView
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Modalità full-screen
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        text = TextView(this).apply {
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.create("serif", Typeface.BOLD)
            alpha = 0f
            setPadding(40, 40, 40, 40)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            addView(text)
        }

        setContentView(layout)

        // Inizializza TTS
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.ITALIAN
                tts.setPitch(0.9f)
                tts.setSpeechRate(0.8f)
                
                // Inizia musica di sottofondo
                try {
                    mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
                    mediaPlayer?.isLooping = true
                    mediaPlayer?.setVolume(0.3f, 0.3f)
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    // Fallback silenzioso
                }
                
                // Inizia sequenza intro
                handler.postDelayed({ showNextLine() }, 1000)
            }
        }
    }

    private fun showNextLine() {
        if (index >= phrases.size) {
            // Fine intro, vai a CharacterSelector
            handler.postDelayed({
                startActivity(Intent(this, CharacterSelector::class.java))
                finish()
            }, 2000)
            return
        }

        // Fade out precedente
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 500
            fillAfter = true
        }

        if (text.alpha > 0f) {
            text.startAnimation(fadeOut)
            handler.postDelayed({
                showCurrentPhrase()
            }, 500)
        } else {
            showCurrentPhrase()
        }
    }

    private fun showCurrentPhrase() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 2000
            fillAfter = true
        }

        text.text = phrases[index]
        text.startAnimation(fadeIn)
        
        // Voce narrante
        tts.speak(phrases[index], TextToSpeech.QUEUE_FLUSH, null, "intro_$index")

        index++
        handler.postDelayed({ showNextLine() }, 4500)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
        mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        // Salta intro
        handler.removeCallbacksAndMessages(null)
        startActivity(Intent(this, CharacterSelector::class.java))
        finish()
    }
}