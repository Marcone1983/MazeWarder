package com.marcone1983.mazewarden3d.repository

import android.content.Context
import com.marcone1983.mazewarden3d.SaveSystem
import com.marcone1983.mazewarden3d.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saveSystem: SaveSystem
) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun saveGameState(gameState: GameState) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(gameState)
            saveSystem.saveProgress(context, GAME_STATE_KEY, jsonString)
        } catch (e: Exception) {
            // Handle serialization errors gracefully
        }
    }

    suspend fun loadGameState(): GameState? = withContext(Dispatchers.IO) {
        try {
            val jsonString = saveSystem.loadProgress(context, GAME_STATE_KEY)
            jsonString?.let { json.decodeFromString<GameState>(it) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveMazeState(mazeState: MazeState) = withContext(Dispatchers.IO) {
        try {
            val simplifiedMaze = SimplifiedMazeState(
                size = mazeState.size,
                wallPositions = mazeState.grid.flatMapIndexed { y, row ->
                    row.mapIndexedNotNull { x, cell ->
                        if (cell == CellType.WALL) Position(x, y) else null
                    }
                },
                dynamicWalls = mazeState.walls,
                exitPosition = mazeState.exitPosition
            )
            val jsonString = json.encodeToString(simplifiedMaze)
            saveSystem.saveProgress(context, MAZE_STATE_KEY, jsonString)
        } catch (e: Exception) {
            // Handle serialization errors gracefully
        }
    }

    suspend fun loadMazeState(): MazeState? = withContext(Dispatchers.IO) {
        try {
            val jsonString = saveSystem.loadProgress(context, MAZE_STATE_KEY)
            jsonString?.let { 
                val simplified = json.decodeFromString<SimplifiedMazeState>(it)
                simplified.toMazeState()
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun savePlayerState(playerState: PlayerState) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(playerState)
            saveSystem.saveProgress(context, PLAYER_STATE_KEY, jsonString)
        } catch (e: Exception) {
            // Handle serialization errors gracefully
        }
    }

    suspend fun loadPlayerState(): PlayerState? = withContext(Dispatchers.IO) {
        try {
            val jsonString = saveSystem.loadProgress(context, PLAYER_STATE_KEY)
            jsonString?.let { json.decodeFromString<PlayerState>(it) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveAudioSettings(audioSettings: AudioSettings) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(audioSettings)
            saveSystem.saveProgress(context, AUDIO_SETTINGS_KEY, jsonString)
        } catch (e: Exception) {
            // Handle serialization errors gracefully
        }
    }

    suspend fun loadAudioSettings(): AudioSettings = withContext(Dispatchers.IO) {
        try {
            val jsonString = saveSystem.loadProgress(context, AUDIO_SETTINGS_KEY)
            jsonString?.let { json.decodeFromString<AudioSettings>(it) } ?: AudioSettings.default()
        } catch (e: Exception) {
            AudioSettings.default()
        }
    }

    suspend fun calculateAIMove(mazeState: MazeState, playerState: PlayerState): AIMove? = withContext(Dispatchers.IO) {
        try {
            // Convert states to legacy format for AI engine
            val walls = mazeState.walls.map { wall ->
                WallState(
                    r = wall.position.y,
                    c = wall.position.x,
                    orientation = if (wall.orientation == WallOrientation.HORIZONTAL) 'h' else 'v',
                    type = wall.type.name.lowercase()
                )
            }
            
            val board = GameBoard(walls, listOf(playerState.position.x to playerState.position.y))
            
            // Use the existing AI engine
            GodWardenAI.calculateStrategicPlacement(board)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveHighScore(score: Int) = withContext(Dispatchers.IO) {
        val currentBest = saveSystem.loadInt(context, HIGH_SCORE_KEY, 0)
        if (score > currentBest) {
            saveSystem.saveInt(context, HIGH_SCORE_KEY, score)
        }
    }

    suspend fun getHighScore(): Int = withContext(Dispatchers.IO) {
        saveSystem.loadInt(context, HIGH_SCORE_KEY, 0)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        saveSystem.resetProgress(context)
    }

    companion object {
        private const val GAME_STATE_KEY = "game_state"
        private const val MAZE_STATE_KEY = "maze_state"
        private const val PLAYER_STATE_KEY = "player_state"
        private const val AUDIO_SETTINGS_KEY = "audio_settings"
        private const val HIGH_SCORE_KEY = "high_score"
    }
}

// Simplified maze state for easier serialization
@kotlinx.serialization.Serializable
private data class SimplifiedMazeState(
    val size: Int,
    val wallPositions: List<Position>,
    val dynamicWalls: List<WallData>,
    val exitPosition: Position
) {
    fun toMazeState(): MazeState {
        val grid = Array(size) { Array(size) { CellType.FLOOR } }
        wallPositions.forEach { pos ->
            if (pos.y < size && pos.x < size) {
                grid[pos.y][pos.x] = CellType.WALL
            }
        }
        return MazeState(size, grid, dynamicWalls, exitPosition)
    }
}

// Legacy compatibility classes
data class WallState(
    val r: Int, 
    val c: Int, 
    val orientation: Char,
    val type: String
)

data class GameBoard(
    val walls: List<WallState>,
    val playerPositions: List<Pair<Int, Int>>
)

// Placeholder for existing AI engine
object GodWardenAI {
    fun calculateStrategicPlacement(board: GameBoard): AIMove {
        // Return a pass move for now - the actual AI logic exists in GameRenderer
        return AIMove.pass()
    }
}