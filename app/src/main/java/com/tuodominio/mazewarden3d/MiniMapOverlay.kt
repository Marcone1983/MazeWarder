package com.marcone1983.mazewarden3d

import android.content.Context
import android.graphics.*
import android.view.View

class MiniMapOverlay(context: Context) : View(context) {

    var opponentGhost = PlayerGhost()
    var maze: Array<Array<Int>> = MazeGenerator.generate(11, 11)

    private val wallPaint = Paint().apply { color = Color.GRAY }
    private val floorPaint = Paint().apply { color = Color.BLACK }

    override fun onDraw(canvas: Canvas) {
        val cellSize = width / maze.size
        val scale = 0.25f

        for (i in maze.indices) {
            for (j in maze[i].indices) {
                val paint = if (maze[i][j] == 2) wallPaint else floorPaint
                canvas.drawRect(
                    i * cellSize * scale,
                    j * cellSize * scale,
                    (i + 1) * cellSize * scale,
                    (j + 1) * cellSize * scale,
                    paint
                )
            }
        }

        opponentGhost.draw(canvas, cellSize, scale)
    }
}