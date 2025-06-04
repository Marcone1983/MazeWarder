package com.tuodominio.mazewarden3d

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

class PlayerGhost {

    var x: Int = 1
    var y: Int = 1

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    fun update(pos: Pair<Int, Int>) {
        x = pos.first
        y = pos.second
    }

    fun draw(canvas: Canvas, cellSize: Int, scale: Float) {
        val cx = (x * cellSize * scale)
        val cy = (y * cellSize * scale)
        val r = (cellSize * scale / 3f)
        canvas.drawCircle(cx + r, cy + r, r, paint)
    }
}