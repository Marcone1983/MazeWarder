package com.marcone1983.mazewarden3d

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.graphics.Color

class OptionsMenu : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "Opzioni"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val volumeLabel = TextView(this).apply {
            text = "Volume Musica"
            setTextColor(Color.LTGRAY)
        }

        val volumeSeek = SeekBar(this).apply {
            max = 100
            progress = 70
        }

        val langLabel = TextView(this).apply {
            text = "Lingua"
            setTextColor(Color.LTGRAY)
        }

        val langSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@OptionsMenu,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Italiano", "English")
            )
        }

        val themeLabel = TextView(this).apply {
            text = "Tema"
            setTextColor(Color.LTGRAY)
        }

        val themeSwitch = Switch(this).apply {
            text = "Modalità Oscura"
            isChecked = true
        }

        layout.addView(title)
        layout.addView(volumeLabel)
        layout.addView(volumeSeek)
        layout.addView(langLabel)
        layout.addView(langSpinner)
        layout.addView(themeLabel)
        layout.addView(themeSwitch)

        setContentView(layout)
    }
}