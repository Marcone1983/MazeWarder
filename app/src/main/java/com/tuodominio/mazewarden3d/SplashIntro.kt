package com.marcone1983.mazewarden3d

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.media.MediaPlayer
import android.content.Intent

class SplashIntro : Activity() {

    private lateinit var logoView: ImageView
    private var introSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = RelativeLayout(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        logoView = ImageView(this).apply {
            setImageResource(R.drawable.mazewarden_logo)
            visibility = View.INVISIBLE
        }

        layout.addView(logoView, RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        ))

        setContentView(layout)

        // Inizializza suono epico
        try {
            introSound = MediaPlayer.create(this, R.raw.maze_intro)
            introSound?.start()
        } catch (e: Exception) {
            // Fallback se file audio non esiste
        }

        // Logo animato dopo 1s
        Handler().postDelayed({
            logoView.visibility = View.VISIBLE
            val anim = AlphaAnimation(0f, 1f).apply {
                duration = 2000
                fillAfter = true
            }
            logoView.startAnimation(anim)
        }, 1000)

        // Vai al menù dopo 4s
        Handler().postDelayed({
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }, 4000)
    }

    override fun onDestroy() {
        super.onDestroy()
        introSound?.release()
    }
}