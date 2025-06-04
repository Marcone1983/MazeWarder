package com.tuodominio.mazewarden3d

object MazeGenerator {

    fun generate(width: Int, height: Int): Array<Array<Int>> {
        val maze = Array(width) { Array(height) { 0 } }

        for (i in maze.indices) {
            for (j in maze[0].indices) {
                if (i % 2 == 0 || j % 2 == 0) {
                    maze[i][j] = 2 // muri fissi ai bordi
                }
            }
        }

        // Corridoio iniziale
        maze[1][1] = 0
        maze[width - 2][height - 2] = 0

        return maze
    }
}