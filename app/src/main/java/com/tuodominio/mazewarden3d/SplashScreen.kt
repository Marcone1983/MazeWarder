package com.marcone1983.mazewarden3d

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.content.Intent

class SplashScreen : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
        }

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.mazewarden_logo)
            layoutParams = LinearLayout.LayoutParams(350, 200).apply {
                setMargins(0, 0, 0, 30)
            }
            alpha = 0f
        }

        val title = TextView(this).apply {
            text = "MazeWarden"
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            alpha = 0f
        }

        val subtitle = TextView(this).apply {
            text = "ULTRA AAA EDITION"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
            alpha = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 0)
            }
        }

        layout.addView(logo)
        layout.addView(title)
        layout.addView(subtitle)
        setContentView(layout)

        // Animazioni sequenziali
        val fadeInLogo = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }

        val fadeInTitle = AlphaAnimation(0f, 1f).apply {
            duration = 1500
            fillAfter = true
            startOffset = 500
        }

        val fadeInSubtitle = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
            startOffset = 1000
        }

        logo.startAnimation(fadeInLogo)
        title.startAnimation(fadeInTitle)
        subtitle.startAnimation(fadeInSubtitle)

        // Transizione a MainMenu dopo 3 secondi
        layout.postDelayed({
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }, 3000)
    }
}