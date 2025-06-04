package com.marcone1983.mazewarden3d.ui

import com.google.android.filament.*
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

/**
 * ParticleSystem: gestisce un emitter di particelle 3D in Filament
 */
class ParticleSystem(
    private val engine: Engine,
    private val scene: Scene,
    private val assetLoader: AssetLoader,
    private val resourceLoader: ResourceLoader
) {
    private val particleEntity = EntityManager.get().create()

    /**
     * Carica e riproduce un effetto particellare glTF
     * fileName sotto assets/raw (es. "spark_effect.glb")
     */
    suspend fun playEffect(fileName: String, x: Float, z: Float) {
        withContext(Dispatchers.IO) {
            try {
                // Carica bytes e crea asset glTF con particelle
                val buffer = ByteBuffer.allocate(0) // semplificazione per ora
                val asset: FilamentAsset = assetLoader.createAsset(buffer)
                resourceLoader.loadResources(asset)
                
                // Aggiungi entità alla scena
                asset.entities.forEach { entity ->
                    scene.addEntity(entity)
                }

                // Posiziona al mondo
                val tm = engine.transformManager
                val ti = tm.getInstance(asset.root)
                val mat = FloatArray(16)
                tm.getWorldTransform(ti, mat)
                mat[12] = x
                mat[14] = z
                tm.setTransform(ti, mat)

                // L'asset gestisce la sua animazione di particelle in loop breve
                asset.animator?.let { animator ->
                    animator.applyAnimation(0, 0f)
                    animator.updateBoneMatrices()
                }
            } catch (e: Exception) {
                // Gestisci errori di caricamento particelle
                e.printStackTrace()
            }
        }
    }

    /**
     * Crea un effetto particellare procedurale semplice
     */
    fun createSimpleSparkEffect(x: Float, y: Float, z: Float) {
        // Crea particelle procedurali usando primitive Filament
        repeat(10) { i ->
            val sparkEntity = EntityManager.get().create()
            
            // Crea una piccola sfera luminosa
            val rm = engine.renderableManager
            val builder = RenderableManager.Builder(1)
                .boundingBox(Box(-0.1f, -0.1f, -0.1f, 0.1f, 0.1f, 0.1f))
                .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, 
                    engine.createVertexBuffer(12), 
                    engine.createIndexBuffer(12))
            
            builder.build(engine, sparkEntity)
            
            // Posiziona con offset casuale
            val tm = engine.transformManager
            val ti = tm.getInstance(sparkEntity)
            val offsetX = (Math.random() - 0.5).toFloat() * 2f
            val offsetZ = (Math.random() - 0.5).toFloat() * 2f
            
            tm.setTransform(ti, floatArrayOf(
                0.1f, 0f, 0f, x + offsetX,
                0f, 0.1f, 0f, y + 1f,
                0f, 0f, 0.1f, z + offsetZ,
                0f, 0f, 0f, 1f
            ))
            
            scene.addEntity(sparkEntity)
            
            // Rimuovi dopo 1 secondo
            // (implementazione semplificata)
        }
    }
}