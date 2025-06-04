package com.marcone1983.mazewarden3d

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.Gravity

class MainGame : Activity() {

    private lateinit var mazeView: MazeView
    private lateinit var hud: LinearLayout
    private lateinit var character: String
    private var skillUsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        character = SaveSystem.loadProgress(this, "character") ?: "Guerriero"

        mazeView = MazeView(this)

        val skillBtn = Button(this).apply {
            text = "Usa Skill"
            setOnClickListener {
                if (!skillUsed) {
                    SkillSystem.useSkill(character, mazeView)
                    skillUsed = true
                    text = "Skill Usata"
                }
            }
        }

        hud = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            gravity = Gravity.TOP
            addView(skillBtn)
        }

        val layout = FrameLayout(this).apply {
            addView(mazeView)
            addView(hud)
        }

        setContentView(layout)
    }
}