package com.tuodominio.mazewarden3d

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameHUD(
    skillAvailable: Boolean = true,
    skillCooldown: Float = 0f,
    playerHealth: Int = 100,
    turnsRemaining: Int = 50,
    currentCharacter: String = "Guerriero",
    onSkillUse: () -> Unit = {},
    onPauseGame: () -> Unit = {}
) {
    // HUD trasparente in overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top HUD - Info personaggio e salute
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info personaggio
            Column {
                Text(
                    text = currentCharacter,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Turni: $turnsRemaining",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            // Barra vita
            Column {
                Text(
                    text = "Vita: $playerHealth%",
                    color = if (playerHealth > 50) Color.Green else Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = playerHealth / 100f,
                    modifier = Modifier.width(120.dp),
                    color = if (playerHealth > 50) Color.Green else Color.Red,
                    trackColor = Color.Gray
                )
            }
        }
        
        // Right HUD - Skill e controlli
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(
                    Color.Black.copy(alpha = 0.8f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pulsante Skill
            Button(
                onClick = onSkillUse,
                enabled = skillAvailable && skillCooldown == 0f,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (currentCharacter) {
                        "Guerriero" -> Color.Red
                        "Maga" -> Color.Magenta
                        "Robot" -> Color.Cyan
                        else -> Color.Gray
                    },
                    disabledContainerColor = Color.Gray
                ),
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text(
                    text = when (currentCharacter) {
                        "Guerriero" -> "⚔️"
                        "Maga" -> "✨"
                        "Robot" -> "⚡"
                        else -> "?"
                    },
                    fontSize = 24.sp
                )
            }
            
            // Status skill
            Text(
                text = if (skillAvailable) {
                    if (skillCooldown > 0f) "Cooldown: ${skillCooldown.toInt()}s"
                    else "PRONTA"
                } else {
                    "USATA"
                },
                color = if (skillAvailable && skillCooldown == 0f) Color.Green else Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pulsante Pausa
            Button(
                onClick = onPauseGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                ),
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "⏸️",
                    fontSize = 20.sp
                )
            }
        }
        
        // Bottom HUD - Suggerimenti
        if (turnsRemaining <= 10) {
            Text(
                text = "⚠️ Attenzione! Pochi turni rimasti!",
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        Color.Black.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            )
        }
    }
}

// Versione Activity per compatibilità legacy
import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.graphics.Color as AndroidColor

class GameHUDActivity : Activity() {

    private lateinit var skillBtn: Button
    private lateinit var skillStatus: TextView
    private lateinit var healthBar: ProgressBar
    private lateinit var turnsText: TextView
    private lateinit var characterText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = RelativeLayout(this)

        // Top layout
        val topLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            id = 1001
            setBackgroundColor(AndroidColor.argb(180, 0, 0, 0))
            setPadding(20, 20, 20, 20)
        }

        characterText = TextView(this).apply {
            text = "Guerriero"
            setTextColor(AndroidColor.WHITE)
            textSize = 18f
        }

        turnsText = TextView(this).apply {
            text = "Turni: 50"
            setTextColor(AndroidColor.LTGRAY)
            textSize = 14f
        }

        healthBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            progress = 100
            max = 100
            layoutParams = LinearLayout.LayoutParams(200, 40)
        }

        val topLeftLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(characterText)
            addView(turnsText)
        }

        val topRightLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@GameHUDActivity).apply {
                text = "Vita"
                setTextColor(AndroidColor.WHITE)
            })
            addView(healthBar)
        }

        topLayout.addView(topLeftLayout)
        topLayout.addView(topRightLayout)

        // Right layout - Skill
        val rightLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(AndroidColor.argb(200, 0, 0, 0))
            setPadding(20, 20, 20, 20)
            id = 1002
        }

        skillBtn = Button(this).apply {
            text = "⚔️ SKILL"
            textSize = 16f
            setBackgroundColor(AndroidColor.RED)
            setTextColor(AndroidColor.WHITE)
            layoutParams = LinearLayout.LayoutParams(150, 150)
        }

        skillStatus = TextView(this).apply {
            text = "PRONTA"
            setTextColor(AndroidColor.GREEN)
            textSize = 14f
            gravity = Gravity.CENTER
        }

        rightLayout.addView(skillBtn)
        rightLayout.addView(skillStatus)

        // Layout params
        val topParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_TOP)
        }

        val rightParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            addRule(RelativeLayout.CENTER_VERTICAL)
        }

        layout.addView(topLayout, topParams)
        layout.addView(rightLayout, rightParams)

        setContentView(layout)
    }

    fun updateSkillStatus(available: Boolean, character: String) {
        skillStatus.text = if (available) "PRONTA" else "USATA"
        skillStatus.setTextColor(if (available) AndroidColor.GREEN else AndroidColor.RED)
        
        skillBtn.text = when (character) {
            "Guerriero" -> "⚔️ SMASH"
            "Maga" -> "✨ TELEPORT"
            "Robot" -> "⚡ HACK"
            else -> "SKILL"
        }
    }

    fun updateHealth(health: Int) {
        healthBar.progress = health
    }

    fun updateTurns(turns: Int) {
        turnsText.text = "Turni: $turns"
        turnsText.setTextColor(
            if (turns <= 10) AndroidColor.RED else AndroidColor.LTGRAY
        )
    }
}