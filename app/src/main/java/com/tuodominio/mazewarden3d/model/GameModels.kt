package com.marcone1983.mazewarden3d.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val isLoading: Boolean = false,
    val isPaused: Boolean = false,
    val isGameWon: Boolean = false,
    val isExitRevealed: Boolean = false,
    val turnCount: Int = 0,
    val elapsedTimeMs: Long = 0,
    val finalScore: Int = 0,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val error: String? = null
) {
    companion object {
        fun initial() = GameState()
    }
}

data class MazeState(
    val size: Int,
    val grid: Array<Array<CellType>>,
    val walls: List<WallData>,
    val exitPosition: Position
) {
    companion object {
        fun empty() = MazeState(
            size = 11,
            grid = Array(11) { Array(11) { CellType.FLOOR } },
            walls = emptyList(),
            exitPosition = Position(9, 9)
        )
        
        fun generate(size: Int): MazeState {
            val maze = MazeGenerator.generate(size, size)
            return MazeState(
                size = size,
                grid = Array(size) { row ->
                    Array(size) { col ->
                        when (maze[row][col]) {
                            2 -> CellType.WALL
                            else -> CellType.FLOOR
                        }
                    }
                },
                walls = emptyList(),
                exitPosition = Position(size - 2, size - 2)
            )
        }
    }
    
    fun isPositionValid(position: Position): Boolean {
        return position.x in 0 until size && position.y in 0 until size
    }
    
    fun hasWallAt(position: Position): Boolean {
        if (!isPositionValid(position)) return true
        return grid[position.y][position.x] == CellType.WALL
    }
    
    fun destroyRandomWall(): MazeState {
        val wallPositions = mutableListOf<Position>()
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (grid[y][x] == CellType.WALL) {
                    wallPositions.add(Position(x, y))
                }
            }
        }
        
        if (wallPositions.isEmpty()) return this
        
        val randomWall = wallPositions.random()
        val newGrid = grid.map { it.clone() }.toTypedArray()
        newGrid[randomWall.y][randomWall.x] = CellType.FLOOR
        
        return copy(grid = newGrid)
    }
    
    fun getRandomEmptyPosition(): Position {
        val emptyPositions = mutableListOf<Position>()
        for (y in 1 until size - 1) {
            for (x in 1 until size - 1) {
                if (grid[y][x] == CellType.FLOOR) {
                    emptyPositions.add(Position(x, y))
                }
            }
        }
        return emptyPositions.randomOrNull() ?: Position(1, 1)
    }
    
    fun applyAIMove(aiMove: AIMove): MazeState {
        if (!aiMove.isWall) return this
        
        val newWalls = walls + WallData(
            position = Position(aiMove.x, aiMove.z),
            type = WallType.valueOf(aiMove.wallType.uppercase()),
            orientation = WallOrientation.HORIZONTAL
        )
        
        return copy(walls = newWalls)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MazeState
        return size == other.size && 
               grid.contentDeepEquals(other.grid) && 
               walls == other.walls &&
               exitPosition == other.exitPosition
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + grid.contentDeepHashCode()
        result = 31 * result + walls.hashCode()
        result = 31 * result + exitPosition.hashCode()
        return result
    }
}

@Serializable
data class PlayerState(
    val position: Position,
    val character: Character,
    val moveCount: Int,
    val skillCooldowns: Map<Skill, Int>
) {
    companion object {
        fun initial() = PlayerState(
            position = Position(1, 1),
            character = Character.WARRIOR,
            moveCount = 0,
            skillCooldowns = emptyMap()
        )
    }
}

@Serializable
data class AudioState(
    val settings: AudioSettings,
    val lastSound: SoundEffect?
) {
    companion object {
        fun initial() = AudioState(
            settings = AudioSettings.default(),
            lastSound = null
        )
    }
}

@Serializable
data class Position(
    val x: Int,
    val y: Int
)

@Serializable
data class WallData(
    val position: Position,
    val type: WallType,
    val orientation: WallOrientation
)

@Serializable
data class AudioSettings(
    val masterVolume: Float,
    val sfxVolume: Float,
    val musicVolume: Float,
    val voiceVolume: Float,
    val isMuted: Boolean
) {
    companion object {
        fun default() = AudioSettings(
            masterVolume = 1.0f,
            sfxVolume = 0.8f,
            musicVolume = 0.6f,
            voiceVolume = 0.9f,
            isMuted = false
        )
    }
}

enum class CellType {
    FLOOR, WALL
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

enum class Character(val displayName: String, val skills: List<Skill>) {
    WARRIOR("Guerriero", listOf(Skill.WALL_DESTROYER)),
    MAGE("Mago", listOf(Skill.TELEPORT)),
    SCOUT("Esploratore", listOf(Skill.EXIT_SCANNER))
}

enum class Skill(val displayName: String, val cooldownTurns: Int) {
    WALL_DESTROYER("Distruggi Muro", 3),
    TELEPORT("Teletrasporto", 5),
    EXIT_SCANNER("Scanner Uscita", 7)
}

enum class WallType {
    NORMALE, INVISIBILE, RIMBALZANTE, TELEPORTANTE, DISTRUGGIBILE
}

enum class WallOrientation {
    HORIZONTAL, VERTICAL
}

enum class SoundEffect {
    STEP, WALL_HIT, SKILL_USE, VICTORY
}

enum class Difficulty {
    EASY, NORMAL, HARD, NIGHTMARE
}

data class AIMove(
    val x: Int,
    val z: Int,
    val isWall: Boolean,
    val wallType: String = "normale"
) {
    companion object {
        fun pass() = AIMove(-1, -1, false)
    }
}