package com.marcone1983.mazewarden3d

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.content.Intent

class MainMenu : Activity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenitore
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_ruined_castle_fire)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // LOGO
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.mazewarden_logo)
            layoutParams = LinearLayout.LayoutParams(400, 200).apply {
                bottomMargin = 50
            }
            alpha = 0f
        }

        // Animazione logo
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 2000
            fillAfter = true
        }
        logo.startAnimation(fadeIn)

        // MUSICA EPICA
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.main_menu_music)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        } catch (e: Exception) {
            // Fallback silenzioso se audio non disponibile
        }
        
        // VOICE-OVER INTRO
        VoiceOver.speakIntro(this)

        // PULSANTI
        val buttons = listOf(
            "Nuova Partita" to ::startGame,
            "Continua" to ::continueGame,
            "Tutorial" to ::openTutorial,
            "Opzioni" to ::openOptions,
            "Crediti" to ::showCredits,
            "Esci" to ::exitGame
        )

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        buttons.forEach { (label, action) ->
            val btn = Button(this).apply {
                text = label
                textSize = 24f
                setTextColor(Color.parseColor("#FFE4B5"))
                setBackgroundResource(R.drawable.btn_rune_selector)
                typeface = Typeface.create("serif-monospace", Typeface.BOLD)
                setOnClickListener { action() }
                layoutParams = LinearLayout.LayoutParams(600, 150).apply {
                    bottomMargin = 30
                }
                setPadding(20, 10, 20, 10)
            }
            menu.addView(btn)
        }

        layout.addView(logo)
        layout.addView(menu)

        setContentView(layout)
    }

    private fun startGame() {
        // startActivity(Intent(this, IntroScene::class.java)) // TODO: Creare IntroScene
        // Fallback temporaneo a MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun continueGame() {
        Toast.makeText(this, "Funzione non ancora implementata", Toast.LENGTH_SHORT).show()
    }

    private fun openTutorial() {
        // startActivity(Intent(this, TutorialScene::class.java)) // TODO: Creare TutorialScene
        // Fallback: avvia MainActivity con tutorial
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("start_tutorial", true)
        startActivity(intent)
        finish()
    }

    private fun openOptions() {
        startActivity(Intent(this, OptionsMenu::class.java))
    }

    private fun showCredits() {
        // startActivity(Intent(this, CreditsScene::class.java)) // TODO: Creare CreditsScene
        Toast.makeText(this, "Crediti - MazeWarden 3D ULTRA AAA Edition", Toast.LENGTH_LONG).show()
    }

    private fun exitGame() {
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            }
            GameAudioFX.cleanup()
            VoiceOver.cleanup()
        } catch (e: Exception) {
            // Handle cleanup errors gracefully
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
            GameAudioFX.pauseBackgroundMusic()
        } catch (e: Exception) { }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (::mediaPlayer.isInitialized && !mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
            GameAudioFX.startBackgroundMusic()
        } catch (e: Exception) { }
    }
}