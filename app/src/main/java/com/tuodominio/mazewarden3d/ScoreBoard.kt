package com.marcone1983.mazewarden3d

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreBoard @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun declareWinner(player: Int) {
        val message = context.getString(R.string.player_won, player)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        
        // Trigger victory effects
        VoiceOver.speakVictory(context)
        GameAudioFX.playSkill() // Victory sound effect
    }
    
    fun announceRaceStart() {
        val message = context.getString(R.string.race_starting)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}