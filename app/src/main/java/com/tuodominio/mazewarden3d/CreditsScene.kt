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
import android.view.animation.TranslateAnimation
import android.media.MediaPlayer

class CreditsScene : Activity() {

    private lateinit var voicePlayer: VoicePlayer
    private var mediaPlayer: MediaPlayer? = null

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
        startCreditsSequence()
    }
    
    private fun setupUI() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFillViewport = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 100, 40, 100)
        }

        // Logo
        val logo = TextView(this).apply {
            text = "⚔️ MazeWarden 3D ⚔️"
            textSize = 32f
            setTextColor(Color.parseColor("#FFD700"))
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            alpha = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 80 }
        }

        // Credits content
        val creditsContent = """
            🎮 ULTRA AAA EDITION
            
            ═══════════════════════════
            
            🎯 GAME DESIGN & CONCEPT
            White Califoxi
            
            💻 DEVELOPMENT & CODE
            Kotlin/Android Studio
            Termux Environment
            Claude Code Assistant
            
            🎨 GRAPHICS & 3D ENGINE
            Google Filament PBR
            glTF 2.0 Models
            HDR Environment Mapping
            Procedural Generation
            
            🔊 AUDIO SYSTEM
            3D Spatial Audio (Oboe)
            Text-to-Speech Integration
            Ambient Soundscapes
            Medieval Archive Sounds
            
            🤖 ARTIFICIAL INTELLIGENCE
            Strategic AI Pathfinding
            Dynamic Wall Placement
            A* Algorithm Implementation
            Procedural Map Generation
            
            🏗️ ARCHITECTURE
            Jetpack Compose UI
            Hilt Dependency Injection
            Coroutines & Flow
            Material Design 3
            
            ═══════════════════════════
            
            🎪 SPECIAL THANKS
            
            Dark Souls Series - Inspiration
            Medieval Fantasy Genre
            Android Developer Community
            Open Source Contributors
            
            ═══════════════════════════
            
            📱 TECHNICAL SPECS
            
            Min SDK: 24 (Android 7.0+)
            Target SDK: 33
            OpenGL ES 3.1 Required
            NDK: ARM64 + x86_64
            
            ═══════════════════════════
            
            🏆 ACHIEVEMENTS UNLOCKED
            
            ✅ AAA-Quality Mobile Game
            ✅ Advanced 3D Graphics
            ✅ Immersive Audio Experience
            ✅ Strategic AI Gameplay
            ✅ Professional Architecture
            
            ═══════════════════════════
            
            💫 FUTURE UPDATES
            
            🔮 Multiplayer Mode
            🌍 New Biomes & Levels
            ⚡ Enhanced Particle FX
            🎵 Dynamic Music System
            🏅 Achievement System
            
            ═══════════════════════════
            
            🌟 POWERED BY
            
            Google Filament Engine
            Android Jetpack
            Kotlin Coroutines
            Material You Design
            
            ═══════════════════════════
            
            📧 CONTACT & SUPPORT
            
            Developer: White Califoxi
            Platform: Termux/Android
            Built with: ❤️ & ☕
            
            ═══════════════════════════
            
            🎭 FINAL WORDS
            
            "Nel labirinto della vita,
             ogni muro è una sfida,
             ogni passo una scelta,
             ogni vittoria una conquista."
            
            Grazie per aver giocato
            MazeWarden 3D!
            
            🚀 READY FOR LAUNCH! 🚀
        """.trimIndent()

        val creditsText = TextView(this).apply {
            text = creditsContent
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.create("monospace", Typeface.NORMAL)
            lineSpacing = 8f, 1.2f
            alpha = 0f
        }

        // Back button
        val backBtn = Button(this).apply {
            text = "🏠 Torna al Menu"
            textSize = 18f
            setBackgroundColor(Color.parseColor("#8B4513"))
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(300, 120).apply {
                topMargin = 50
            }
            alpha = 0f
            setOnClickListener { goBack() }
        }

        layout.addView(logo)
        layout.addView(creditsText)
        layout.addView(backBtn)
        
        scrollView.addView(layout)
        setContentView(scrollView)
        
        // Store views for animation
        layout.tag = Triple(logo, creditsText, backBtn)
    }
    
    private fun startCreditsSequence() {
        // Background music
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.main_menu_music)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(0.4f, 0.4f)
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Fallback silenzioso
        }
        
        val layout = (findViewById<ScrollView>(android.R.id.content) as ScrollView)
            .getChildAt(0) as LinearLayout
        val (logo, creditsText, backBtn) = layout.tag as Triple<TextView, TextView, Button>
        
        // Animate logo first
        logo.postDelayed({
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 2000
                fillAfter = true
            }
            logo.startAnimation(fadeIn)
            
            // Voice introduction
            voicePlayer.postDelayed({
                voicePlayer.say("MazeWarden 3D. Grazie per aver giocato questa avventura epica.")
            }, 1000)
        }, 500)
        
        // Animate credits text with scroll effect
        creditsText.postDelayed({
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 3000
                fillAfter = true
            }
            
            val slideUp = TranslateAnimation(0f, 0f, 200f, 0f).apply {
                duration = 3000
                fillAfter = true
            }
            
            creditsText.startAnimation(fadeIn)
            creditsText.startAnimation(slideUp)
        }, 2500)
        
        // Animate back button
        backBtn.postDelayed({
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 1500
                fillAfter = true
            }
            backBtn.startAnimation(fadeIn)
        }, 5000)
        
        // Auto-scroll effect
        val scrollView = findViewById<ScrollView>(android.R.id.content)
        scrollView.postDelayed({
            scrollView.smoothScrollTo(0, scrollView.getChildAt(0).height)
        }, 6000)
    }
    
    private fun goBack() {
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
        goBack()
    }
}