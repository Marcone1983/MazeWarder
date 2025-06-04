package com.tuodominio.mazewarden3d

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.graphics.Color
import android.graphics.Typeface
import android.content.Intent
import android.view.View
import android.view.animation.AlphaAnimation
import android.media.MediaPlayer

class GameEnd : Activity() {

    private lateinit var voicePlayer: VoicePlayer
    private var mediaPlayer: MediaPlayer? = null
    
    companion object {
        const val EXTRA_VICTORY = "victory"
        const val EXTRA_SCORE = "score"
        const val EXTRA_TIME = "time"
        const val EXTRA_CHARACTER = "character"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full-screen
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        voicePlayer = VoicePlayer(this)
        
        val isVictory = intent.getBooleanExtra(EXTRA_VICTORY, false)
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val time = intent.getStringExtra(EXTRA_TIME) ?: "N/A"
        val character = intent.getStringExtra(EXTRA_CHARACTER) ?: "Guardiano"
        
        setupUI(isVictory, score, time, character)
        playAudio(isVictory)
        announceResult(isVictory, character)
    }
    
    private fun setupUI(isVictory: Boolean, score: Int, time: String, character: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(if (isVictory) Color.parseColor("#0a2a0a") else Color.parseColor("#2a0a0a"))
            setPadding(40, 40, 40, 40)
        }

        // Titolo principale
        val resultText = TextView(this).apply {
            text = if (isVictory) "🏆 VITTORIA! 🏆" else "💀 SCONFITTA 💀"
            textSize = 36f
            setTextColor(if (isVictory) Color.parseColor("#FFD700") else Color.parseColor("#FF6B6B"))
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            alpha = 0f
        }

        // Sottotitolo
        val subtitle = TextView(this).apply {
            text = if (isVictory) 
                "Il $character ha completato il labirinto!" 
            else 
                "Il $character è stato sconfitto..."
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.create("serif", Typeface.ITALIC)
            alpha = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 30; bottomMargin = 50 }
        }

        // Statistiche
        val statsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.argb(100, 255, 255, 255))
            setPadding(30, 30, 30, 30)
            alpha = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 50 }
        }

        val statsTitle = TextView(this).apply {
            text = "📊 STATISTICHE"
            textSize = 18f
            setTextColor(Color.parseColor("#FFD700"))
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val scoreText = TextView(this).apply {
            text = "Punteggio: $score"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val timeText = TextView(this).apply {
            text = "Tempo: $time"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val characterText = TextView(this).apply {
            text = "Personaggio: $character"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        statsLayout.addView(statsTitle)
        statsLayout.addView(scoreText)
        statsLayout.addView(timeText)
        statsLayout.addView(characterText)

        // Pulsanti
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val restartBtn = Button(this).apply {
            text = "🔄 Riprova"
            textSize = 18f
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(200, 120).apply {
                rightMargin = 20
            }
            setOnClickListener { restartGame() }
        }

        val menuBtn = Button(this).apply {
            text = "🏠 Menu"
            textSize = 18f
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(200, 120).apply {
                leftMargin = 20
            }
            setOnClickListener { goToMenu() }
        }

        buttonLayout.addView(restartBtn)
        buttonLayout.addView(menuBtn)

        layout.addView(resultText)
        layout.addView(subtitle)
        layout.addView(statsLayout)
        layout.addView(buttonLayout)

        setContentView(layout)

        // Animazioni
        animateElements(resultText, subtitle, statsLayout)
        
        // Salva statistiche se vittoria
        if (isVictory) {
            saveVictoryStats(score, time, character)
        }
    }
    
    private fun animateElements(vararg views: View) {
        views.forEachIndexed { index, view ->
            val delay = (index * 800).toLong()
            
            view.postDelayed({
                val fadeIn = AlphaAnimation(0f, 1f).apply {
                    duration = 1000
                    fillAfter = true
                }
                view.startAnimation(fadeIn)
            }, delay)
        }
    }
    
    private fun playAudio(isVictory: Boolean) {
        try {
            val audioRes = if (isVictory) R.raw.background_music else R.raw.background_music // TODO: Aggiungere audio sconfitta
            mediaPlayer = MediaPlayer.create(this, audioRes)
            mediaPlayer?.setVolume(0.5f, 0.5f)
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Fallback silenzioso
        }
    }
    
    private fun announceResult(isVictory: Boolean, character: String) {
        val message = if (isVictory) {
            "Congratulazioni! Il $character ha trionfato nel labirinto!"
        } else {
            "Il $character è stato sconfitto. Non arrenderti, Guardiano!"
        }
        
        voicePlayer.postDelayed({
            voicePlayer.say(message)
        }, 2000)
    }
    
    private fun saveVictoryStats(score: Int, time: String, character: String) {
        SaveSystem.saveProgress(this, "last_victory_score", score.toString())
        SaveSystem.saveProgress(this, "last_victory_time", time)
        SaveSystem.saveProgress(this, "last_victory_character", character)
        
        // Incrementa contatore vittorie
        val currentWins = SaveSystem.loadProgress(this, "total_wins")?.toIntOrNull() ?: 0
        SaveSystem.saveProgress(this, "total_wins", (currentWins + 1).toString())
        
        // Migliore punteggio
        val bestScore = SaveSystem.loadProgress(this, "best_score")?.toIntOrNull() ?: 0
        if (score > bestScore) {
            SaveSystem.saveProgress(this, "best_score", score.toString())
            voicePlayer.postDelayed({
                voicePlayer.say("Nuovo record personale!")
            }, 4000)
        }
    }
    
    private fun restartGame() {
        voicePlayer.say("Nuova sfida!")
        val intent = Intent(this, CharacterSelector::class.java)
        intent.putExtra("restart_game", true)
        startActivity(intent)
        finish()
    }
    
    private fun goToMenu() {
        voicePlayer.say("Ritorno al menu principale.")
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        voicePlayer.cleanup()
        mediaPlayer?.release()
    }
    
    override fun onBackPressed() {
        goToMenu()
    }
}