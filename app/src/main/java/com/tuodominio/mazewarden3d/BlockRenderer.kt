package com.tuodominio.mazewarden3d

import com.google.android.filament.*
import com.google.android.filament.gltfio.*
import com.google.android.filament.utils.*
import android.content.Context
import java.nio.ByteBuffer

class BlockRenderer(
    private val context: Context,
    private val engine: Engine,
    private val scene: Scene
) {
    private val entityManager = EntityManager.get()

    private val glbAssets = mutableMapOf<TileType, String>().apply {
        put(TileType.FLOOR, "floor_tile.glb")
        put(TileType.WALL, "wall_block.glb")
        put(TileType.TRAP, "trap_spike.glb")
        put(TileType.START_A, "start_blue.glb")
        put(TileType.START_B, "start_red.glb")
        put(TileType.SPECIAL, "magic_root.glb")
    }

    fun renderMap(tileMap: List<Tile>) {
        tileMap.forEach { tile ->
            val assetName = glbAssets[tile.type] ?: return@forEach
            val entity = loadGlbAsset(assetName)
            val posX = tile.x.toFloat() * 2f
            val posZ = tile.z.toFloat() * 2f

            val transformManager = engine.transformManager
            val instance = transformManager.getInstance(entity)
            transformManager.setTransform(
                instance,
                floatArrayOf(
                    1f, 0f, 0f, posX,
                    0f, 1f, 0f, 0f,
                    0f, 0f, 1f, posZ,
                    0f, 0f, 0f, 1f
                )
            )

            scene.addEntity(entity)
        }
    }

    fun loadGlb(fileName: String, x: Int, z: Int): Int {
        val entity = loadGlbAsset(fileName)
        val posX = x.toFloat() * 2f
        val posZ = z.toFloat() * 2f

        val transformManager = engine.transformManager
        val instance = transformManager.getInstance(entity)
        transformManager.setTransform(
            instance,
            floatArrayOf(
                1f, 0f, 0f, posX,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, posZ,
                0f, 0f, 0f, 1f
            )
        )

        return entity
    }

    private fun loadGlbAsset(fileName: String): Int {
        try {
            val input = context.assets.open("raw/$fileName")
            val bytes = input.readBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size).put(bytes).apply { flip() }
            
            val asset = FilamentAssetLoader.createAsset(engine, buffer, bytes.size)
            asset?.let {
                scene.addEntities(asset.entities)
                return asset.root
            }
            throw RuntimeException("Failed to load GLB: $fileName")
        } catch (e: Exception) {
            // Fallback: crea entità vuota
            val fallbackEntity = entityManager.create()
            return fallbackEntity
        }
    }
}