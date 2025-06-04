package com.marcone1983.mazewarden3d

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MazeRaceEngine @Inject constructor(
    private val scoreBoard: ScoreBoard
) {

    lateinit var player1Maze: Array<Array<Int>>
    lateinit var player2Maze: Array<Array<Int>>
    lateinit var player1Pos: Pair<Int, Int>
    lateinit var player2Pos: Pair<Int, Int>
    lateinit var exit: Pair<Int, Int>

    var raceOver = false

    fun startRace(size: Int) {
        player1Maze = MazeGenerator.generate(size, size)
        player2Maze = MazeGenerator.generate(size, size)
        player1Pos = Pair(1, 1)
        player2Pos = Pair(1, 1)
        exit = Pair(size - 2, size - 2)
        raceOver = false
        
        scoreBoard.announceRaceStart()
    }

    fun updatePlayerPos(player: Int, pos: Pair<Int, Int>) {
        if (raceOver) return

        if (player == 1) player1Pos = pos else player2Pos = pos

        // Applica IA bastarda a entrambi
        AIWallEngine.placeWall(player1Maze, player1Pos, exit)
        AIWallEngine.placeWall(player2Maze, player2Pos, exit)

        // Controlla vittoria
        if (player1Pos == exit) {
            raceOver = true
            scoreBoard.declareWinner(1)
        } else if (player2Pos == exit) {
            raceOver = true
            scoreBoard.declareWinner(2)
        }
    }
}