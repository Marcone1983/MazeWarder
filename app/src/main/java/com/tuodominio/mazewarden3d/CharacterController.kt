package com.tuodominio.mazewarden3d

import com.google.android.filament.*
import kotlinx.coroutines.*
import kotlin.math.abs

enum class CharacterClass {
    GUERRIERO, MAGA, ROBOT
}

data class Character(
    val name: String,
    val type: CharacterClass,
    val modelFile: String,
    var positionX: Int,
    var positionZ: Int,
    var skillUsed: Boolean = false,
    var entity: Int = 0
)

class CharacterController(
    private val renderer: BlockRenderer,
    private val engine: Engine,
    private val scene: Scene
) {
    val characters = mutableListOf<Character>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadCharacters() {
        characters.clear()

        characters.add(Character("Guerriero", CharacterClass.GUERRIERO, "warrior.glb", 1, 4))
        characters.add(Character("Maga", CharacterClass.MAGA, "sorceress.glb", 4, 4))
        characters.add(Character("Robot", CharacterClass.ROBOT, "robot.glb", 7, 4))

        characters.forEach { character ->
            character.entity = renderer.loadGlb(character.modelFile, character.positionX, character.positionZ)
            scene.addEntity(character.entity)
            Animator.playIdle(character.entity)
        }
    }

    fun move(character: Character, newX: Int, newZ: Int) {
        if (abs(newX - character.positionX) > 1 || abs(newZ - character.positionZ) > 1) return

        character.positionX = newX
        character.positionZ = newZ

        Animator.playWalk(character.entity)
        Animator.translateTo(character.entity, newX.toFloat() * 2f, 0f, newZ.toFloat() * 2f, onComplete = {
            Animator.playIdle(character.entity)
        })
    }

    fun activateSkill(character: Character) {
        if (character.skillUsed) return

        when (character.type) {
            CharacterClass.GUERRIERO -> {
                // Distrugge un muro davanti a sé
                val targetTile = MapManager.getTileInFront(character)
                if (targetTile?.type == TileType.WALL) {
                    ParticleSystem.play("smash.glb", targetTile.x, targetTile.z)
                    SoundFX.play("smash.wav")
                    MapManager.removeTile(targetTile)
                    character.skillUsed = true
                }
            }

            CharacterClass.MAGA -> {
                // Teletrasporto di 2 celle in qualsiasi direzione
                val destination = MapManager.getSafeTeleport(character)
                Animator.playCast(character.entity)
                Animator.fadeOutIn(character.entity, onMid = {
                    move(character, destination.x, destination.z)
                })
                SoundFX.play("teleport.wav")
                character.skillUsed = true
            }

            CharacterClass.ROBOT -> {
                // Sposta un muro vicino a una nuova posizione
                val oldWall = MapManager.getNearbyWall(character)
                val newPosition = MapManager.getNearbyEmptyTile(character)
                if (oldWall != null && newPosition != null) {
                    MapManager.moveWall(oldWall, newPosition)
                    ParticleSystem.play("electric_shift.glb", newPosition.x, newPosition.z)
                    SoundFX.play("zap.wav")
                    character.skillUsed = true
                }
            }
        }
    }
}



// ParticleSystem extension
object ParticleSystem {
    fun play(effectFile: String, x: Int, z: Int) {
        // Riproduce effetto particellare
        WallFX.playImpactEffect(x.toFloat(), z.toFloat())
    }
}

