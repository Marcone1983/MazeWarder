package com.marcone1983.mazewarden3d

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.random.Random

class MazeView(context: Context) : View(context) {

    private val mazeSize = context.resources.getInteger(R.dimen.maze_size)
    private var maze = MazeGenerator.generate(mazeSize, mazeSize)

    private val player = Point(1, 1)
    private val exit = Point(mazeSize - 2, mazeSize - 2)

    private val cellSize get() = width / mazeSize

    private val wallPaint = Paint().apply { color = Color.DKGRAY }
    private val floorPaint = Paint().apply { color = Color.BLACK }
    private var playerPaint = SkinSystem.getPaint(SkinSystem.Skin.WARRIOR)
    private val exitPaint = Paint().apply { color = Color.GREEN }
    
    init {
        // Inizializza audio FX
        GameAudioFX.init(context)
        
        // Add accessibility support
        contentDescription = context.getString(R.string.desc_maze_view)
        isClickable = true
        isFocusable = true
        
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = (event.x / cellSize).toInt()
                val y = (event.y / cellSize).toInt()
                movePlayerToward(x, y)
                true
            } else false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0 until mazeSize) {
            for (j in 0 until mazeSize) {
                val paint = when (maze[i][j]) {
                    2 -> wallPaint
                    else -> floorPaint
                }
                canvas.drawRect(
                    (i * cellSize).toFloat(), (j * cellSize).toFloat(),
                    ((i + 1) * cellSize).toFloat(), ((j + 1) * cellSize).toFloat(),
                    paint
                )
            }
        }

        // Draw player and exit
        val playerRadius = cellSize * context.resources.getDimension(R.dimen.player_radius_ratio) / context.resources.displayMetrics.density
        canvas.drawCircle(
            (player.x + 0.5f) * cellSize,
            (player.y + 0.5f) * cellSize,
            playerRadius,
            playerPaint
        )

        canvas.drawRect(
            (exit.x * cellSize).toFloat(), (exit.y * cellSize).toFloat(),
            ((exit.x + 1) * cellSize).toFloat(), ((exit.y + 1) * cellSize).toFloat(),
            exitPaint
        )
    }

    private fun movePlayerToward(x: Int, y: Int) {
        if (x !in 0 until mazeSize || y !in 0 until mazeSize) return
        if (Math.abs(x - player.x) + Math.abs(y - player.y) != 1) return
        
        if (maze[x][y] == 2) {
            // Hit a wall - play sound and animate
            GameAudioFX.playWallHit()
            PlayerAnimator.animateWallHit(this)
            return
        }

        // Successful move - play step sound
        GameAudioFX.playStep()
        player.set(x, y)
        invalidate()

        // Victory check
        if (player == exit) {
            Toast.makeText(context, "Hai vinto!", Toast.LENGTH_SHORT).show()
        } else {
            AIWallEngine.placeWall(maze, player.toPair(), exit.toPair())
            invalidate()
        }
    }

    fun destroyRandomWall() {
        GameAudioFX.playSkill()
        PlayerAnimator.animateSkillUse(this)
        
        val walls = mutableListOf<Point>()
        for (i in maze.indices) {
            for (j in maze[0].indices) {
                if (maze[i][j] == 2) walls.add(Point(i, j))
            }
        }
        if (walls.isNotEmpty()) {
            val rand = walls.random()
            maze[rand.x][rand.y] = 0
            invalidate()
        }
    }

    fun teleportRandom() {
        GameAudioFX.playSkill()
        PlayerAnimator.animateSkillUse(this)
        
        val empty = mutableListOf<Point>()
        for (i in 1 until mazeSize - 1) {
            for (j in 1 until mazeSize - 1) {
                if (maze[i][j] == 0) empty.add(Point(i, j))
            }
        }
        if (empty.isNotEmpty()) {
            val rand = empty.random()
            player.set(rand.x, rand.y)
            invalidate()
        }
    }

    fun scanAndRevealExit() {
        GameAudioFX.playSkill()
        PlayerAnimator.animateSkillUse(this)
        
        // Flash the exit tile (change color briefly) using Handler for thread safety
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        var flashCount = 0
        
        val flashRunnable = object : Runnable {
            override fun run() {
                if (flashCount <= 2) {
                    exitPaint.color = if (flashCount % 2 == 0) Color.RED else Color.GREEN
                    invalidate()
                    flashCount++
                    handler.postDelayed(this, context.resources.getInteger(R.integer.flash_delay).toLong())
                } else {
                    // Reset to original color
                    exitPaint.color = Color.GREEN
                    invalidate()
                }
            }
        }
        
        handler.post(flashRunnable)
    }
    
    fun updatePlayerSkin(character: String) {
        val skin = SkinSystem.getSkinByCharacter(character)
        playerPaint = SkinSystem.getPaint(skin)
        invalidate()
    }

    private fun Point.toPair() = Pair(this.x, this.y)
}