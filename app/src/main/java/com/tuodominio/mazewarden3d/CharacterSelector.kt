package com.tuodominio.mazewarden3d

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.graphics.Color

class CharacterSelector : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "Scegli il tuo Guardiano"
            textSize = 26f
            setTextColor(Color.WHITE)
        }

        val characters = listOf("Guerriero", "Maga", "Robot")
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@CharacterSelector,
                android.R.layout.simple_spinner_dropdown_item,
                characters
            )
        }

        val confirm = Button(this).apply {
            text = "Conferma"
            setOnClickListener {
                val choice = spinner.selectedItem.toString()
                SaveSystem.saveProgress(this@CharacterSelector, "character", choice)
                finish()
            }
        }

        layout.addView(title)
        layout.addView(spinner)
        layout.addView(confirm)

        setContentView(layout)
    }
}