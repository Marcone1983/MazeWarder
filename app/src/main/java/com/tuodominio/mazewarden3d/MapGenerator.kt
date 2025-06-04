package com.marcone1983.mazewarden3d

data class Tile(
    val x: Int,
    val z: Int,
    var type: TileType = TileType.FLOOR
)

enum class TileType {
    FLOOR, WALL, START_A, START_B, TRAP, SPECIAL
}

class MapGenerator(private val size: Int = 9) {
    val map = mutableListOf<Tile>()

    fun generateMap(theme: Biome): List<Tile> {
        map.clear()

        for (x in 0 until size) {
            for (z in 0 until size) {
                val tile = Tile(x, z, TileType.FLOOR)

                // Blocca bordi
                if (x == 0 || z == 0 || x == size - 1 || z == size - 1) {
                    tile.type = TileType.WALL
                }

                // Spargi muri
                if ((0..100).random() < theme.wallDensity && tile.type == TileType.FLOOR) {
                    tile.type = TileType.WALL
                }

                // Spargi trappole
                if ((0..100).random() < theme.trapDensity && tile.type == TileType.FLOOR) {
                    tile.type = TileType.TRAP
                }

                map.add(tile)
            }
        }

        // Posizioni iniziali
        map.first { it.x == 1 && it.z == size / 2 }.type = TileType.START_A
        map.first { it.x == size - 2 && it.z == size / 2 }.type = TileType.START_B

        return map
    }
}

data class Biome(
    val name: String,
    val wallDensity: Int,
    val trapDensity: Int,
    val backgroundColor: Int
)

val FORESTA_MAGICA = Biome("Foresta Magica", wallDensity = 25, trapDensity = 10, backgroundColor = 0xFF006600.toInt())
val DESERTO_ANTICO = Biome("Deserto Antico", wallDensity = 35, trapDensity = 5, backgroundColor = 0xFFFFCC66.toInt())
val CITTA_FUTURISTICA = Biome("Città Futuristica", wallDensity = 20, trapDensity = 15, backgroundColor = 0xFF101030.toInt())