package com.tuodominio.mazewarden3d

import kotlin.random.Random

object AIWallEngine {

    fun placeWall(maze: Array<Array<Int>>, playerPos: Pair<Int, Int>, enemyPos: Pair<Int, Int>) {
        val directions = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))

        for (dir in directions.shuffled()) {
            val wallX = playerPos.first + dir.first
            val wallY = playerPos.second + dir.second
            if (wallX in maze.indices && wallY in maze[0].indices && maze[wallX][wallY] == 0) {
                maze[wallX][wallY] = 2 // 2 = muro
                break
            }
        }
    }
}