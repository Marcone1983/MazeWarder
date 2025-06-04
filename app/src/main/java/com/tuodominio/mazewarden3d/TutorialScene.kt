package com.marcone1983.mazewarden3d

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.graphics.Color
import android.graphics.Typeface
import android.content.Intent
import android.view.View
import android.view.animation.AlphaAnimation

class TutorialScene : Activity() {
    
    private lateinit var instructions: TextView
    private lateinit var nextBtn: Button
    private lateinit var skipBtn: Button
    private lateinit var progressText: TextView
    private lateinit var voicePlayer: VoicePlayer
    
    private val steps = listOf(
        "Benvenuto, Guardiano. Il labirinto ti attende.",
        "Usa i controlli per muoverti sulla griglia 9x9.",
        "Ogni personaggio ha una skill speciale unica.",
        "Guerriero: distrugge muri. Maga: teletrasporto. Robot: hacking.",
        "Puoi usare la skill una sola volta per livello.",
        "Evita i muri e raggiungi il centro del labirinto.",
        "L'IA nemica piazzerà muri per ostacolarti.",
        "Usa strategia e timing per vincere.",
        "Ora sei pronto. Che la fortuna ti assista!"
    )
    
    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full-screen
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        voicePlayer = VoicePlayer(this)
        setupUI()
        showCurrentStep()
    }
    
    private fun setupUI() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1a1a1a"))
            setPadding(40, 40, 40, 40)
        }

        // Titolo
        val title = TextView(this).apply {
            text = "Tutorial"
            textSize = 32f
            setTextColor(Color.parseColor("#FFD700"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 50 }
        }

        // Progress
        progressText = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 30 }
        }

        // Istruzioni
        instructions = TextView(this).apply {
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.create("serif", Typeface.NORMAL)
            alpha = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                bottomMargin = 80
                topMargin = 40
            }
        }

        // Pulsanti
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        nextBtn = Button(this).apply {
            text = "Avanti"
            textSize = 18f
            setBackgroundColor(Color.parseColor("#8B4513"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(200, 120).apply {
                rightMargin = 30
            }
            setOnClickListener { nextStep() }
        }

        skipBtn = Button(this).apply {
            text = "Salta"
            textSize = 18f
            setBackgroundColor(Color.parseColor("#666666"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(200, 120)
            setOnClickListener { skipTutorial() }
        }

        buttonLayout.addView(nextBtn)
        buttonLayout.addView(skipBtn)

        layout.addView(title)
        layout.addView(progressText)
        layout.addView(instructions)
        layout.addView(buttonLayout)

        setContentView(layout)
    }
    
    private fun showCurrentStep() {
        if (currentStep >= steps.size) {
            completeTutorial()
            return
        }
        
        // Aggiorna progress
        progressText.text = "Passo ${currentStep + 1} di ${steps.size}"
        
        // Animazione fade in per il testo
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }
        
        instructions.text = steps[currentStep]
        instructions.startAnimation(fadeIn)
        
        // Voce narrante
        voicePlayer.say(steps[currentStep])
        
        // Aggiorna pulsante
        if (currentStep == steps.size - 1) {
            nextBtn.text = "Inizia Gioco"
        }
    }
    
    private fun nextStep() {
        // Fade out corrente
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 300
            fillAfter = true
        }
        
        instructions.startAnimation(fadeOut)
        
        // Dopo fade out, mostra prossimo step
        instructions.postDelayed({
            currentStep++
            showCurrentStep()
        }, 300)
    }
    
    private fun skipTutorial() {
        voicePlayer.say("Tutorial saltato.")
        goToGame()
    }
    
    private fun completeTutorial() {
        voicePlayer.say("Tutorial completato. Iniziamo!")
        
        // Salva che il tutorial è stato completato
        SaveSystem.saveProgress(this, "tutorial_completed", "true")
        
        instructions.postDelayed({
            goToGame()
        }, 2000)
    }
    
    private fun goToGame() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("from_tutorial", true)
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        voicePlayer.cleanup()
    }
    
    override fun onBackPressed() {
        if (currentStep > 0) {
            currentStep--
            showCurrentStep()
        } else {
            super.onBackPressed()
        }
    }
}