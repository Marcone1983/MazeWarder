package com.tuodominio.mazewarden3d

import kotlin.math.abs

object MapManager {
    lateinit var currentMap: List<Tile>

    fun setMap(map: List<Tile>) {
        currentMap = map
    }

    fun getTile(x: Int, z: Int): Tile? {
        return currentMap.find { it.x == x && it.z == z }
    }

    fun getTileInFront(character: Character): Tile? {
        val offset = when (character.type) {
            CharacterClass.GUERRIERO -> 1
            CharacterClass.MAGA -> 2
            CharacterClass.ROBOT -> 1
        }
        return getTile(character.positionX + offset, character.positionZ)
    }

    fun getNearbyWall(character: Character): Tile? {
        return currentMap.find {
            it.type == TileType.WALL &&
            abs(it.x - character.positionX) <= 1 &&
            abs(it.z - character.positionZ) <= 1
        }
    }

    fun getNearbyEmptyTile(character: Character): Tile? {
        return currentMap.find {
            it.type == TileType.FLOOR &&
            abs(it.x - character.positionX) <= 2 &&
            abs(it.z - character.positionZ) <= 2
        }
    }

    fun getSafeTeleport(character: Character): Tile {
        return currentMap.first {
            it.type == TileType.FLOOR && abs(it.x - character.positionX) == 2
        }
    }

    fun removeTile(tile: Tile) {
        tile.type = TileType.FLOOR
    }

    fun moveWall(from: Tile, to: Tile) {
        from.type = TileType.FLOOR
        to.type = TileType.WALL
    }

    // Metodi avanzati per gestione mappa
    fun isWalkable(x: Int, z: Int): Boolean {
        val tile = getTile(x, z)
        return tile?.type == TileType.FLOOR || tile?.type == TileType.START_A || tile?.type == TileType.START_B
    }

    fun getWalkableTiles(): List<Tile> {
        return currentMap.filter { 
            it.type == TileType.FLOOR || it.type == TileType.START_A || it.type == TileType.START_B 
        }
    }

    fun getWallTiles(): List<Tile> {
        return currentMap.filter { it.type == TileType.WALL }
    }

    fun getTrapTiles(): List<Tile> {
        return currentMap.filter { it.type == TileType.TRAP }
    }

    fun getSpecialTiles(): List<Tile> {
        return currentMap.filter { it.type == TileType.SPECIAL }
    }

    fun getTilesInRadius(centerX: Int, centerZ: Int, radius: Int): List<Tile> {
        return currentMap.filter { tile ->
            val distance = abs(tile.x - centerX) + abs(tile.z - centerZ) // Manhattan distance
            distance <= radius
        }
    }

    fun findPath(startX: Int, startZ: Int, endX: Int, endZ: Int): List<Tile> {
        // Implementazione A* semplificata per pathfinding
        val openSet = mutableListOf<PathNode>()
        val closedSet = mutableSetOf<Pair<Int, Int>>()
        val start = PathNode(startX, startZ, 0, heuristic(startX, startZ, endX, endZ))
        
        openSet.add(start)
        
        while (openSet.isNotEmpty()) {
            val current = openSet.minByOrNull { it.f } ?: break
            openSet.remove(current)
            
            if (current.x == endX && current.z == endZ) {
                return reconstructPath(current)
            }
            
            closedSet.add(Pair(current.x, current.z))
            
            // Esplora vicini (4-directional)
            val neighbors = listOf(
                Pair(current.x + 1, current.z),
                Pair(current.x - 1, current.z),
                Pair(current.x, current.z + 1),
                Pair(current.x, current.z - 1)
            )
            
            for ((nx, nz) in neighbors) {
                if (closedSet.contains(Pair(nx, nz)) || !isWalkable(nx, nz)) continue
                
                val g = current.g + 1
                val h = heuristic(nx, nz, endX, endZ)
                val neighbor = PathNode(nx, nz, g, g + h, current)
                
                val existing = openSet.find { it.x == nx && it.z == nz }
                if (existing == null || g < existing.g) {
                    if (existing != null) openSet.remove(existing)
                    openSet.add(neighbor)
                }
            }
        }
        
        return emptyList() // Nessun percorso trovato
    }

    private fun heuristic(x1: Int, z1: Int, x2: Int, z2: Int): Int {
        return abs(x1 - x2) + abs(z1 - z2) // Manhattan distance
    }

    private fun reconstructPath(node: PathNode): List<Tile> {
        val path = mutableListOf<Tile>()
        var current: PathNode? = node
        
        while (current != null) {
            getTile(current.x, current.z)?.let { path.add(0, it) }
            current = current.parent
        }
        
        return path
    }

    fun isValidPosition(x: Int, z: Int): Boolean {
        return x in 0..8 && z in 0..8
    }

    fun getRandomEmptyTile(): Tile? {
        val emptyTiles = getWalkableTiles()
        return if (emptyTiles.isNotEmpty()) {
            emptyTiles.random()
        } else null
    }

    fun countTilesByType(type: TileType): Int {
        return currentMap.count { it.type == type }
    }
}

// Data class per pathfinding A*
data class PathNode(
    val x: Int,
    val z: Int,
    val g: Int, // Costo dal nodo di partenza
    val f: Int, // g + h (costo totale stimato)
    val parent: PathNode? = null
)