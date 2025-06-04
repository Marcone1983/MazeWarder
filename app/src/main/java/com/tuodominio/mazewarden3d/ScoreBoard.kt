package com.tuodominio.mazewarden3d

import android.widget.Toast
import android.content.Context

object ScoreBoard {
    fun declareWinner(player: Int) {
        Toast.makeText(
            MazeApp.context,
            "Il giocatore $player ha vinto la sfida!",
            Toast.LENGTH_LONG
        ).show()
        // Potremmo aggiungere animazioni, effetti luce, ruggiti epici
    }
}