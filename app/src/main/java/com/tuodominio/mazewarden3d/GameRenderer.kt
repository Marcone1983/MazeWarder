package com.marcone1983.mazewarden3d

import android.content.Context
import android.view.Choreographer
import android.view.SurfaceView
import com.google.android.filament.*
import com.google.android.filament.gltfio.*
import com.google.android.filament.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import kotlin.math.min
import kotlin.math.abs
import kotlin.math.minOf

// Import placeholder per librerie engine proprietario
// import dev.immersive.fx.*
// import ai.warden.godmode.*

// Rappresenta lo stato di un muro (posizione su griglia, orientamento, tipo)
data class WallState(
    val r: Int, 
    val c: Int, 
    val orientation: Char, // 'h' o 'v'
    val type: String       // "normale", "invisibile", "rimbalzante", "teleportante", "distruggibile"
)

// Nodo A* con f = g + h
data class Node(
    val pos: Pair<Int, Int>, 
    val g: Int, 
    val f: Int
) : Comparable<Node> {
    override fun compareTo(other: Node): Int = this.f - other.f
}

// Funzione di validità di una mossa (muoversi da (r,c) a (nr,nc))
// controlla confini e muri che bloccano
fun isMoveValid(
    r: Int, c: Int, 
    nr: Int, nc: Int, 
    walls: List<WallState>
): Boolean {
    // Controllo bordi
    if (nr !in 0..8 || nc !in 0..8) return false

    // Controllo muro fra (r,c) e (nr,nc)
    // Se verticalmente adiacenti
    if (r == nr && abs(c - nc) == 1) {
        val minC = minOf(c, nc)
        // Esiste muro orizzontale in (r, minC)?
        walls.forEach { w ->
            if (w.orientation == 'h' && w.r == r && w.c == minC) {
                // Se è tipo che blocca ("normale", "distruggibile")
                if (w.type == "normale" || w.type == "distruggibile") return false
                // Se tipo speciale: gestirli a parte (per A* ignoriamo rimbalzi, ecc.)
                if (w.type == "invisibile" || w.type == "rimbalzante" || w.type == "teleportante") {
                    // Per calcolo percorso consideriamo come blocco : non attraversabile
                    return false
                }
            }
        }
    }
    // Se orizzontalmente adiacenti
    if (c == nc && abs(r - nr) == 1) {
        val minR = minOf(r, nr)
        walls.forEach { w ->
            if (w.orientation == 'v' && w.c == c && w.r == minR) {
                if (w.type == "normale" || w.type == "distruggibile") return false
                if (w.type == "invisibile" || w.type == "rimbalzante" || w.type == "teleportante") {
                    return false
                }
            }
        }
    }
    return true
}

// A* con euristica di Manhattan per calcolare distanza minima
fun computeShortestPath(
    start: Pair<Int, Int>, 
    walls: List<WallState>,
    goalRows: IntRange  // per Giocatore A: 8..8, per B: 0..0
): Int {
    val openSet = java.util.PriorityQueue<Node>()
    val cameFrom = mutableMapOf<Pair<Int,Int>, Pair<Int,Int>?>()
    val gScore = mutableMapOf<Pair<Int,Int>, Int>().withDefault { Int.MAX_VALUE }
    val fScore = mutableMapOf<Pair<Int,Int>, Int>().withDefault { Int.MAX_VALUE }

    fun heuristic(p: Pair<Int,Int>): Int {
        // Per A: dist a riga 8; per B: dist a riga 0
        val r = p.first
        val dr = if (goalRows.first == 8) (8 - r) else r
        return dr
    }

    gScore[start] = 0
    fScore[start] = heuristic(start)
    openSet.add(Node(start, 0, fScore[start]!!))

    val directions = listOf(Pair(-1,0), Pair(1,0), Pair(0,-1), Pair(0,1))

    while (openSet.isNotEmpty()) {
        val current = openSet.poll()
        val (cr, cc) = current.pos
        // Check if in riga obiettivo
        if (cr in goalRows) return gScore.getValue(current.pos)

        for ((dr, dc) in directions) {
            val nr = cr + dr
            val nc = cc + dc
            if (!isMoveValid(cr, cc, nr, nc, walls)) continue
            val tentativeG = gScore.getValue(current.pos) + 1
            val neighbor = Pair(nr, nc)
            if (tentativeG < gScore.getValue(neighbor)) {
                cameFrom[neighbor] = current.pos
                gScore[neighbor] = tentativeG
                val f = tentativeG + heuristic(neighbor)
                fScore[neighbor] = f
                if (openSet.none { it.pos == neighbor }) {
                    openSet.add(Node(neighbor, tentativeG, f))
                }
            }
        }
    }
    return Int.MAX_VALUE // Nessun percorso
}

/**
 * GameRenderer: si occupa di tutto il 3D (Filament).
 * - Carica la scena 3D (tabellone + personaggi + muri).
 * - Gestisce luci PBR, camera, materiali.
 * - Esegue il game loop (render continuo a ~60fps).
 * - Espone API onMove(), onAbility(), onPass() per gli input.
 */
class GameRenderer(private val context: Context) : Choreographer.FrameCallback {

    companion object {
        private const val TAG = "GameRenderer"
        val WALL_TYPES = listOf("normale", "invisibile", "rimbalzante", "teleportante", "distruggibile")
    }

    // Filament core objects
    private val engine: Engine = Engine.create()
    private val swapChain: SwapChain
    private val renderer: Renderer
    private val scene: Scene
    private val view: View
    private val camera: Camera

    // Mappa entità Filament per i modelli
    private var boardEntity: Int = 0
    private var cameraEntity: Int = 0
    private val lightEntity = IntArray(2)

    // Entity per personaggi e muri
    private val playerEntities = mutableListOf<Int>()   // entità Filament per i 3 personaggi
    private val wallEntities = mutableListOf<Int>()     // entità per muri dinamici

    // Materiali PBR
    private lateinit var material: Material

    // CoroutineScope per caricare asset in background - con cleanup
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isDestroyed = false

    // Contesto SurfaceView dove disegnare
    private val surfaceView: SurfaceView = SurfaceView(context)
    private val choreographer = Choreographer.getInstance()

    // Stato del gioco
    private var currentPlayerIndex = 0
    private var turnCount = 0

    init {
        // 1) Configurazione SurfaceView
        surfaceView.setOnTouchListener { _, _ -> true } // preveniamo touch diretti su SurfaceView
        surfaceView.holder.addCallback(object : android.view.SurfaceHolder.Callback {
            override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                swapChain.setAndroidSurface(swapChain, holder.surface, 0)
                choreographer.postFrameCallback(this@GameRenderer)
            }
            override fun surfaceChanged(holder: android.view.SurfaceHolder, format: Int, width: Int, height: Int) {
                view.camera.setProjection(45.0, width.toDouble() / height.toDouble(), 0.1, 100.0, Camera.Fov.VERTICAL)
                renderer.viewport = Viewport(0, 0, width, height)
            }
            override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                choreographer.removeFrameCallback(this@GameRenderer)
            }
        })

        // 2) Crea SwapChain, Renderer, Scene, Camera
        swapChain = engine.createSwapChain(surfaceView.holder.surface)
        renderer = engine.createRenderer()
        // Abilita post-processing avanzato
        renderer.enableBloom(true)
        renderer.enableAntiAliasing(true)
        scene = engine.createScene()

        // 3) Crea View e Camera
        cameraEntity = EntityManager.get().create()
        camera = engine.createCamera(cameraEntity)
        view = engine.createView().apply {
            setCamera(cameraEntity)
            setScene(scene)
            // Abilita environment lighting e skybox
            isPostProcessingEnabled = true
            dynamicResolutionOptions = View.DynamicResolutionOptions(0.85f)
        }

        // 4) Posiziona la camera (vista isometrica dall'alto)
        camera.setProjection(45.0, 1.0, 0.1, 200.0, Camera.Fov.VERTICAL)
        camera.lookAt(
            Float3(0f, 6f, 6f),   // eye (in alto e di lato)
            Float3(0f, 0f, 0f),   // center (centro del tabellone)
            Float3(0f, 1f, 0f)    // up vector
        )

        // 5) Aggiunge luci avanzate (sun, fill, point lights)
        val sun = createSunLight()
        lightEntity[0] = sun
        scene.addEntity(sun)

        val fill = createFillLight()
        lightEntity[1] = fill
        scene.addEntity(fill)

        // Aggiungi luci puntiformi dietro personaggi per highlights
        repeat(2) { idx ->
            val point = createPointLight( Float3( (idx*4f)-2f, 3f, 0f ) )
            scene.addEntity(point)
        }

        // 6) Carica i modelli in background
        loadModels()

        // Carica environment map HDR per riflessioni
        loadHdrEnvironment("env_hdr_pro.hdr")
        
        // Abilita effetti HD avanzati
        enableHighDefinitionEffects()

        // 7) Prepara altri asset (materiali, suoni, ecc.)
        prepareMaterials()
        prepareAudio()

        // 8) Inizializza Game State
        initializeGameState()
    }

    // Espone la SurfaceView per l'Activity
    fun getFilamentView(): SurfaceView = surfaceView

    // Main game loop (callback di Choreographer ~60fps)
    override fun doFrame(frameTimeNanos: Long) {
        if (isDestroyed) return
        
        try {
            if (renderer.beginFrame(swapChain)) {
                renderer.render(view)
                renderer.endFrame()
            }
            choreographer.postFrameCallback(this)
        } catch (e: Exception) {
            // Handle rendering errors gracefully
            if (!isDestroyed) {
                choreographer.postFrameCallback(this)
            }
        }
    }

    // Carica i modelli glTF (.glb) e li aggiunge alla scena
    private fun loadModels() {
        scope.launch {
            if (isDestroyed) return@launch
            // 1) Carica tabellone
            val boardStream = context.assets.open("raw/board_base.glb")
            val (buffer, size) = boardStream.use {
                val bytes = it.readBytes()
                Pair(ByteBuffer.allocateDirect(bytes.size).put(bytes).apply { flip() }, bytes.size)
            }
            val gltfViewer = FilamentAssetLoader.createAsset(engine, buffer, size)
            boardEntity = gltfViewer.root
            scene.addEntities(gltfViewer.entities)

            // Applica PBR environment map su materiali del tabellone
            gltfViewer.entities.forEach { ent ->
                val rm = engine.renderableManager
                if (rm.hasComponent(ent)) {
                    val instance = rm.getInstance(ent)
                    val mi = rm.getMaterialInstanceAt(instance, 0)
                    mi.setParameter("environment", 1) // slot 1 per envmap
                }
            }

            // 2) Carica tre personaggi (A, B, C)
            listOf("player_A.glb", "player_B.glb", "player_C.glb").forEachIndexed { index, fileName ->
                val stream = context.assets.open("raw/$fileName")
                val (buf, sz) = stream.use {
                    val b = it.readBytes()
                    Pair(ByteBuffer.allocateDirect(b.size).put(b).apply { flip() }, b.size)
                }
                val asset = FilamentAssetLoader.createAsset(engine, buf, sz)
                val ent = asset.root
                val transformManager = engine.transformManager
                val ti = transformManager.getInstance(ent).apply {
                    // Aggiungi rotazione casuale e scala animata
                    val scale = 0.5f + index * 0.1f
                    val rotAngle = 360f * (index / 3f)
                    transformManager.setTransform(this,
                        floatArrayOf(
                            scale, 0f, 0f, (index - 1).toFloat() * 2f,
                            0f, scale, 0f, 0f,
                            0f, 0f, scale, 0f,
                            0f, 0f, 0f, 1f))
                }
                
                // Abilita animazioni glTF (walk, idle)
                asset.animator?.let { animator ->
                    animator.applyAnimation(0, 0f)
                    animator.updateBoneMatrices()
                }
                
                playerEntities.add(ent)
                scene.addEntities(asset.entities)
            }

            // 3) Carica modello base del muro
            val wallStream = context.assets.open("raw/wall_x.glb")
            val (bufW, szW) = wallStream.use {
                val b = it.readBytes()
                Pair(ByteBuffer.allocateDirect(b.size).put(b).apply { flip() }, b.size)
            }
            val wallAsset = FilamentAssetLoader.createAsset(engine, bufW, szW)
            val wallBase = wallAsset.root
            val wTransform = engine.transformManager
            wTransform.getInstance(wallBase).apply {
                wTransform.setTransform(this,
                    floatArrayOf(
                        1f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f,
                        0f, 0f, 1f, 2f,  // solleva di 2 unità (altezza muro)
                        0f, 0f, 0f, 1f))
            }
            wallEntities.add(wallBase)
            scene.addEntities(wallAsset.entities)
        }
    }

    private fun prepareMaterials() {
        // Se hai material instances custom, creale qui (es. per muri colorati/animati)
        // Ad esempio: materialInstance = material.defaultInstance.createInstance()
    }

    private fun prepareAudio() {
        // Usa ExoPlayer per riprodurre musica di sottofondo e SFX 3D
        // Esempio (semplificato):
        //   val player = ExoPlayer.Builder(context).build()
        //   val uri = RawResourceDataSource.buildRawResourceUri(R.raw.background_music)
        //   player.setMediaItem(MediaItem.fromUri(uri))
        //   player.isLooping = true
        //   player.playWhenReady = true
        //   player.prepare()
        //
        // SFX: usa SoundPool per suoni brevi di muro, passi, vittoria
    }

    private fun initializeGameState() {
        turnCount = 0
        currentPlayerIndex = 0
        // Puoi memorizzare in variabili i contatori di muri rimanenti per ogni giocatore, ecc.
    }

    // Funzione chiamata da MainActivity/Compose per muovere il giocatore
    fun onMove(direction: String) {
        // Logica 3D: sposta il modello del giocatore nella scena
        val entity = playerEntities[currentPlayerIndex]
        val tm = engine.transformManager
        val inst = tm.getInstance(entity)

        // Calcola la nuova posizione basata su direzione
        val dr: Float; val dc: Float
        when (direction) {
            "up" -> { dr = 0f; dc = -1f }
            "down" -> { dr = 0f; dc = 1f }
            "left" -> { dr = -1f; dc = 0f }
            "right" -> { dr = 1f; dc = 0f }
            else -> { dr = 0f; dc = 0f }
        }
        val oldMat = FloatArray(16)
        tm.getWorldTransform(inst, oldMat)
        // Aggiorna solo x e z (assumiamo y verso l'alto fisso)
        val newMat = oldMat.copyOf()
        newMat[12] = oldMat[12] + dr * 2f   // spostamento su X
        newMat[14] = oldMat[14] + dc * 2f   // spostamento su Z
        tm.setTransform(inst, newMat)

        nextTurn()
    }

    fun onAbility() {
        // Esegui animazione speciale sul modello (es. salto rapido)
        val entity = playerEntities[currentPlayerIndex]
        // TODO: attiva animazione glTF (se presente)
        nextTurn()
    }

    fun onPass() {
        // Nessuna modifica, si passa turno
        nextTurn()
    }

    private fun nextTurn() {
        turnCount++
        currentPlayerIndex = (currentPlayerIndex + 1) % playerEntities.size

        // Logica IA: crea/aggiorna muri in 3D
        applyAIMove()
    }

    /**
     * Invece di piazzare muri casuali, questa versione
     * analizza il tabellone con A* e sceglie la mossa muro
     * che massimizza la difficoltà (score migliore).
     */
    private fun applyAIMove() {
        scope.launch(Dispatchers.IO) {
            if (isDestroyed) return@launch
            // 1) Costruisci lo stato corrente dei muri da wallEntities
            val currentWalls = mutableListOf<WallState>()
            val transformManager = engine.transformManager

            // Ottieni posizione e tipo di ogni muro
            wallEntities.forEach { entity ->
                val comp = engine.transformManager.getInstance(entity)
                val mat = FloatArray(16)
                transformManager.getWorldTransform(comp, mat)
                // Recupera riga e col dalla matrice world pos:
                val x = mat[12]  // posizione X
                val z = mat[14]  // posizione Z
                val r = (z / 2f).toInt().coerceIn(0, 8)
                val c = (x / 2f).toInt().coerceIn(0, 8)
                // Tipo del muro lo ricaviamo da una property (che devi mantenere nello stato WallState)
                val node = engine.renderableManager.getInstance(entity)
                val typeString = "normale" // Default per ora, poi miglioreremo
                val orientation = 'h' // Default per ora, poi miglioreremo
                currentWalls.add(WallState(r, c, orientation, typeString))
            }

            // 2) Calcola distanze correnti per A e B
            val posA = if (playerEntities.isNotEmpty()) {
                playerEntities[0].let { ent ->
                    val matA = FloatArray(16)
                    transformManager.getWorldTransform(transformManager.getInstance(ent), matA)
                    Pair((matA[14] / 2f).toInt(), (matA[12] / 2f).toInt()) // (r, c)
                }
            } else Pair(0, 0)
            
            val posB = if (playerEntities.size > 1) {
                playerEntities[1].let { ent ->
                    val matB = FloatArray(16)
                    transformManager.getWorldTransform(transformManager.getInstance(ent), matB)
                    Pair((matB[14] / 2f).toInt(), (matB[12] / 2f).toInt())
                }
            } else Pair(8, 8)

            val dA = computeShortestPath(posA, currentWalls, 8..8)
            val dB = computeShortestPath(posB, currentWalls, 0..0)

            // 3) Genera tutti i candidati di muro: 
            //    - Posizioni vuote dove non c'è muro, orientamento 'h' o 'v', tipo "normale"
            //    - Per ogni muro esistente, simulare l'evoluzione (incrementare tipo ciclico)
            data class Candidate(val wallsSim: List<WallState>, val action: Pair<Int,WallState>)

            val candidates = mutableListOf<Candidate>()

            // 3a) Piazzamento di nuovi muri
            for (r in 0..8) {
                for (c in 0..8) {
                    // Orizzontale:
                    if (r < 9 && c < 8 && currentWalls.none { it.r == r && it.c == c && it.orientation == 'h' }) {
                        val newWalls = currentWalls.toMutableList()
                        newWalls.add(WallState(r, c, 'h', "normale"))
                        candidates.add(Candidate(newWalls, Pair(0, WallState(r, c, 'h', "normale"))))
                    }
                    // Verticale:
                    if (r < 8 && c < 9 && currentWalls.none { it.r == r && it.c == c && it.orientation == 'v' }) {
                        val newWalls = currentWalls.toMutableList()
                        newWalls.add(WallState(r, c, 'v', "normale"))
                        candidates.add(Candidate(newWalls, Pair(0, WallState(r, c, 'v', "normale"))))
                    }
                }
            }

            // 3b) Evoluzione di muri esistenti (cambio tipo ciclico)
            currentWalls.forEachIndexed { index, w ->
                val idxType = WALL_TYPES.indexOf(w.type)
                val nextType = WALL_TYPES[(idxType + 1) % WALL_TYPES.size]
                val newWalls = currentWalls.toMutableList().apply {
                    set(index, WallState(w.r, w.c, w.orientation, nextType))
                }
                candidates.add(Candidate(newWalls, Pair(1, WallState(w.r, w.c, w.orientation, nextType))))
            }

            // 4) Usa ENGINE PROPRIETARIO per IA strategica divina
            val currentBoard = GameBoard(currentWalls, listOf(posA, posB))
            val godMove = GodWardenAI.calculateStrategicPlacement(currentBoard)
            
            var bestCandidate: Candidate? = null
            
            if (godMove.isWall && godMove.x >= 0 && godMove.z >= 0) {
                // Converte AIMove in WallState
                val wallState = WallState(godMove.z, godMove.x, 'h', godMove.wallType)
                val newWalls = currentWalls + wallState
                bestCandidate = Candidate(newWalls, Pair(0, wallState))
            } else {
                // Fallback al sistema A* precedente
                var bestScore = Int.MIN_VALUE
                candidates.forEach { cand ->
                    val simWalls = cand.wallsSim
                    val simDA = computeShortestPath(posA, simWalls, 8..8)
                    val simDB = computeShortestPath(posB, simWalls, 0..0)
                    val score = ( (simDA + simDB) - (dA + dB) )
                    if (score > bestScore) {
                        bestScore = score
                        bestCandidate = cand
                    }
                }
            }

            // 5) Applico l'azione migliore sulla scena Filament (aggiunta o cambio muro)
            bestCandidate?.let { cand ->
                val (actionType, wallState) = cand.action
                if (actionType == 0) {
                    // piazza nuovo muro con ENGINE PROPRIETARIO
                    withContext(Dispatchers.Main) {
                        // Simula: val wall = WallFX.spawnWallEntity("evo_wall.glb", x, z)
                        val wall = WallFX.spawnWallEntity("evo_wall.glb", wallState.c, wallState.r, engine, scene)
                        
                        // Simula: WallFX.animateRising(wall)
                        WallFX.animateRising(wall, engine)
                        
                        // Simula: ParticleSystem.play("wall_impact.spark", x, z)
                        WallFX.playImpactEffect(wallState.c.toFloat(), wallState.r.toFloat())
                        
                        // Simula: SoundFX.playImpact("wall_deploy.wav")
                        SoundFX.playImpact("wall_deploy.wav", Float3(wallState.c.toFloat(), 0f, wallState.r.toFloat()))
                        
                        wallEntities.add(wall)
                    }
                } else {
                    // evoluzione muro esistente: cerco entità corrispondente e cambio material/texture
                    withContext(Dispatchers.Main) {
                        val tMgr = engine.transformManager
                        // Trova l'entità Filament che ha posizione simile a (r,c) e orientamento
                        wallEntities.firstOrNull { ent ->
                            val ti = tMgr.getInstance(ent)
                            val mat = FloatArray(16)
                            tMgr.getWorldTransform(ti, mat)
                            val er = (mat[14] / 2f).toInt().coerceIn(0, 8)
                            val ec = (mat[12] / 2f).toInt().coerceIn(0, 8)
                            er == wallState.r && ec == wallState.c
                        }?.let { ent ->
                            // Applica un cambio visivo (es. colore o emissive)  
                            val renderable = engine.renderableManager
                            val instance = renderable.getInstance(ent)
                            if (renderable.hasComponent(ent)) {
                                val materialInst = renderable.getMaterialInstanceAt(instance, 0)
                                // Cambia colore emissive in base al tipo
                                when (wallState.type) {
                                    "invisibile"   -> materialInst.setParameter("baseColorFactor", 0.3f, 0.3f, 0.3f, 1.0f)
                                    "rimbalzante"  -> materialInst.setParameter("emissiveFactor", 0.0f, 0.7f, 1.0f, 1.0f)
                                    "teleportante" -> materialInst.setParameter("emissiveFactor", 1.0f, 1.0f, 0.0f, 1.0f)
                                    "distruggibile"-> materialInst.setParameter("emissiveFactor", 1.0f, 0.0f, 0.0f, 1.0f)
                                    else           -> materialInst.setParameter("emissiveFactor", 0.0f, 0.0f, 0.0f, 1.0f)
                                }
                            }
                            // Particolare effetto "flash" luce
                            playWallFlashEffect(ent)
                        }
                    }
                }
            }

            // 6) Fine turno IA: ritorno al main thread per ridisegnare se necessario
            withContext(Dispatchers.Main) {
                // Niente da fare, il Choreographer ridisegnerà automaticamente
            }
        }
    }

    private fun playParticleEffect(x: Float, z: Float) {
        // Implementa un semplice particle system 3D con punti luminosi
        // oppure carica una mesh animata di spark in glTF
    }

    private fun playWallFlashEffect(entity: Int) {
        // Accendi una luce emissiva per 0.2s sull'entità muro per segnalare l'evoluzione
        scope.launch(Dispatchers.Main) {
            if (isDestroyed) return@launch
            val renderable = engine.renderableManager
            if (renderable.hasComponent(entity)) {
                val instance = renderable.getInstance(entity)
                val materialInst = renderable.getMaterialInstanceAt(instance, 0)
                materialInst.setParameter("emissiveFactor", 1.0f, 1.0f, 1.0f, 1.0f)
                delay(200) // 0.2 secondi
                materialInst.setParameter("emissiveFactor", 0f, 0f, 0f, 1.0f)
            }
        }
    }

    fun onResume() {
        if (!isDestroyed) {
            choreographer.postFrameCallback(this)
        }
    }

    fun onPause() {
        choreographer.removeFrameCallback(this)
    }
    
    fun onDestroy() {
        isDestroyed = true
        cleanup()
    }
    
    private fun cleanup() {
        try {
            // Stop frame callbacks
            choreographer.removeFrameCallback(this)
            
            // Cancel coroutines
            scope.cancel()
            
            // Cleanup Filament entities
            cleanupEntities()
            
            // Destroy Filament objects in correct order
            destroyFilamentObjects()
            
        } catch (e: Exception) {
            // Log but don't crash during cleanup
        }
    }
    
    private fun cleanupEntities() {
        // Remove all entities from scene
        playerEntities.forEach { entity ->
            scene.removeEntity(entity)
            EntityManager.get().destroy(entity)
        }
        playerEntities.clear()
        
        wallEntities.forEach { entity ->
            scene.removeEntity(entity)
            EntityManager.get().destroy(entity)
        }
        wallEntities.clear()
        
        // Remove lights
        lightEntity.forEach { entity ->
            if (entity != 0) {
                scene.removeEntity(entity)
                EntityManager.get().destroy(entity)
            }
        }
        
        // Remove board
        if (boardEntity != 0) {
            scene.removeEntity(boardEntity)
            EntityManager.get().destroy(boardEntity)
        }
        
        // Remove camera
        if (cameraEntity != 0) {
            EntityManager.get().destroy(cameraEntity)
        }
    }
    
    private fun destroyFilamentObjects() {
        // Destroy in reverse order of creation
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyCamera(camera)
        engine.destroyRenderer(renderer)
        engine.destroySwapChain(swapChain)
        
        // Finally destroy the engine
        engine.destroy()
    }

    // Crea una luce direzionale (sole)
    private fun createSunLight(): Int {
        val entity = EntityManager.get().create()
        val light = LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 0.9f)
            .intensity(50_000f)
            .direction(0.1f, -1f, -0.3f)
            .build(engine, entity)
        return entity
    }

    // Crea una luce ambientale di riempimento
    private fun createFillLight(): Int {
        val entity = EntityManager.get().create()
        val light = LightManager.Builder(LightManager.Type.SPHERICAL)
            .color(0.6f, 0.7f, 1.0f)
            .intensity(1_000f)
            .position(0f, 5f, 0f)
            .falloff(10f)
            .build(engine, entity)
        return entity
    }
    
    // Crea una luce puntiforme per highlights
    private fun createPointLight(position: Float3): Int {
        val entity = EntityManager.get().create()
        val light = LightManager.Builder(LightManager.Type.POINT)
            .color(1.0f, 0.9f, 0.8f)
            .intensity(5_000f)
            .position(position.x, position.y, position.z)
            .falloff(8f)
            .build(engine, entity)
        return entity
    }
    
    // Carica environment map HDR per riflessioni
    private fun loadHdrEnvironment(fileName: String) {
        scope.launch {
            if (isDestroyed) return@launch
            try {
                // Carica HDR da assets per illuminazione ambientale
                // val hdrStream = context.assets.open("raw/$fileName")
                // val ibl = KTX1Loader.createTexture(engine, hdrStream)
                // scene.indirectLight = IndirectLightBuilder()
                //     .reflections(ibl)
                //     .intensity(30_000.0f)
                //     .build(engine)
            } catch (e: Exception) {
                // HDR file non trovato, usa default skybox
                e.printStackTrace()
            }
        }
    }
    
    // Anima la crescita del muro da scala 0 a 1
    private fun animateWallGrowth(entity: Int) {
        scope.launch(Dispatchers.Main) {
            if (isDestroyed) return@launch
            val tm = engine.transformManager
            val ti = tm.getInstance(entity)
            
            // Animazione di crescita in 0.5 secondi
            val steps = 30
            repeat(steps) { step ->
                val scale = (step + 1).toFloat() / steps
                val mat = FloatArray(16)
                tm.getWorldTransform(ti, mat)
                // Applica scala uniforme
                mat[0] = scale   // scale X
                mat[5] = scale   // scale Y  
                mat[10] = scale  // scale Z
                tm.setTransform(ti, mat)
                delay(16) // ~60fps
            }
        }
    }
    
    // Effetto particellare avanzato (sparks + fumo)
    private fun playAdvancedParticleEffect(x: Float, z: Float) {
        scope.launch {
            try {
                // Usa il particle system per caricare effetti glTF
                WallFX.playImpactEffect(x, z)
                SoundFX.playImpact("wall_deploy.wav")
            } catch (e: Exception) {
                // Fallback a effetto semplice
                playParticleEffect(x, z)
            }
        }
    }

    // Abilita effetti HD avanzati Vulkan-like con ENGINE PROPRIETARIO
    private fun enableHighDefinitionEffects() {
        // Anti-aliasing avanzato FXAA
        setAdvancedAntiAliasing()
        
        // Bloom con forza configurabile
        setBloomOptions(enabled = true, strength = 0.9f)
        
        // Dynamic lighting real-time
        setDynamicLighting(true)
        
        // Nebbia atmosferica volumetrica
        applyAtmosphericFog()
    }
    
    private fun setAdvancedAntiAliasing() {
        // Simulazione renderer.setAntiAliasing(Renderer.AntiAliasing.FXAA)
        view.antiAliasing = View.AntiAliasing.FXAA
    }
    
    private fun setBloomOptions(enabled: Boolean, strength: Float) {
        // Simulazione filamentRenderer.setBloomOptions(BloomOptions(...))
        view.bloomOptions = View.BloomOptions().apply {
            this.enabled = enabled
            this.strength = strength
            this.resolution = 1024  // HD resolution
        }
    }
    
    private fun setDynamicLighting(enabled: Boolean) {
        // Illuminazione dinamica avanzata
        view.dynamicLighting = enabled
        view.shadowingEnabled = enabled
    }

    private fun enableDynamicLighting() {
        // Abilita shadow mapping ad alta risoluzione
        view.shadowingEnabled = true
        
        // Configurazione bloom avanzato
        view.bloomOptions = View.BloomOptions().apply {
            enabled = true
            strength = 0.9f
            resolution = 512
        }
    }

    private fun applyAtmosphericFog() {
        // Nebbia volumetrica leggera secondo la patch ENGINE PROPRIETARIO
        setFogColor(Float3(0.7f, 0.75f, 0.8f))
        setFogDensity(0.01f)
        setFogFalloff(0.2f)
        enableFog()
    }
    
    private fun setFogColor(color: Float3) {
        // fog.setColor(Color(color.x, color.y, color.z))
        // Implementazione placeholder per fog color
    }
    
    private fun setFogDensity(density: Float) {
        // fog.setDensity(density)
        // Implementazione placeholder per fog density
    }
    
    private fun setFogFalloff(falloff: Float) {
        // fog.setFalloff(falloff)
        // Implementazione placeholder per fog falloff
    }
    
    private fun enableFog() {
        // fog.enable()
        // Implementazione placeholder per fog activation
    }
}

// IA divina strategica avanzata ENGINE PROPRIETARIO
object GodWardenAI {
    fun calculateStrategicPlacement(board: GameBoard): AIMove {
        // Simula: val walls = board.availableWalls()
        val walls = board.availableWalls()
        
        val evaluations = walls.map { wall ->
            // Simula: val sim = board.simulateWithWall(wall)  
            val sim = board.simulateWithWall(wall)
            
            // Simula: val score = AIHeuristic.evaluateWallPlacement(sim)
            val score = AIHeuristic.evaluateWallPlacement(sim)
            wall to score
        }
        
        // Converte WallState in AIMove
        val bestWall = evaluations.maxByOrNull { it.second }?.first
        return if (bestWall != null) {
            AIMove(bestWall.c, bestWall.r, true, bestWall.type)
        } else {
            AIMove.pass()
        }
    }

    private fun generateCandidateWalls(currentWalls: List<WallState>): List<WallState> {
        val candidates = mutableListOf<WallState>()
        
        // Genera tutti i possibili piazzamenti
        for (r in 0..8) {
            for (c in 0..8) {
                // Orizzontale
                if (c < 8 && currentWalls.none { it.r == r && it.c == c && it.orientation == 'h' }) {
                    candidates.add(WallState(r, c, 'h', "normale"))
                }
                // Verticale
                if (r < 8 && currentWalls.none { it.r == r && it.c == c && it.orientation == 'v' }) {
                    candidates.add(WallState(r, c, 'v', "normale"))
                }
            }
        }
        
        return candidates
    }

    private fun evaluateWallPlacement(
        wall: WallState,
        currentWalls: List<WallState>,
        playerPositions: List<Pair<Int, Int>>
    ): Int {
        val simulatedWalls = currentWalls + wall
        
        // Calcola impatto sui percorsi di tutti i giocatori
        val pathImpacts = playerPositions.mapIndexed { index, pos ->
            val goalRange = if (index == 0) 8..8 else 0..0
            computeShortestPath(pos, simulatedWalls, goalRange)
        }
        
        // Strategia: massimizza difficoltà totale
        return pathImpacts.sum()
    }
}

// Sistema effetti visivi avanzati ENGINE PROPRIETARIO
object WallFX {
    fun spawnWallEntity(asset: String, x: Int, z: Int, engine: Engine, scene: Scene): Int {
        // Simula: val entity = AssetLoader.loadGlb(asset)
        val entity = EntityManager.get().create()
        
        // entity.position = Vector3(x, 0f, z)
        val tm = engine.transformManager
        val ti = tm.getInstance(entity)
        tm.setTransform(ti, floatArrayOf(
            0f, 0f, 0f, x.toFloat() * 2f,    // scale = 0 inizialmente
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, z.toFloat() * 2f,
            0f, 0f, 0f, 1f
        ))
        
        scene.addEntity(entity)
        return entity
    }

    fun animateRising(entity: Int, engine: Engine) {
        // Simula: Animator.animateScale(entity, Vector3(1f, 1f, 1f), duration = 0.5f)
        GlobalScope.launch(Dispatchers.Main) {
            val tm = engine.transformManager
            val ti = tm.getInstance(entity)
            
            // Animazione di crescita in 30 frame (0.5s)
            repeat(30) { frame ->
                val scale = (frame + 1).toFloat() / 30f
                val mat = FloatArray(16)
                tm.getWorldTransform(ti, mat)
                
                // Applica scaling uniforme
                mat[0] = scale   // X scale
                mat[5] = scale   // Y scale  
                mat[10] = scale  // Z scale
                
                tm.setTransform(ti, mat)
                delay(16) // ~60fps
            }
            
            // Flash emission finale
            flashEmission(entity, engine, duration = 0.2f)
        }
    }
    
    private fun flashEmission(entity: Int, engine: Engine, duration: Float) {
        // Simula: Shader.flashEmission(entity, Color.WHITE, duration = 0.2f)
        GlobalScope.launch(Dispatchers.Main) {
            val rm = engine.renderableManager
            if (rm.hasComponent(entity)) {
                val instance = rm.getInstance(entity)
                val materialInst = rm.getMaterialInstanceAt(instance, 0)
                
                // Flash bianco
                materialInst.setParameter("emissiveFactor", 1.0f, 1.0f, 1.0f, 1.0f)
                delay((duration * 1000).toLong())
                materialInst.setParameter("emissiveFactor", 0f, 0f, 0f, 1.0f)
            }
        }
    }

    fun playImpactEffect(x: Float, z: Float) {
        // Simula: ParticleSystem.play("wall_impact.spark", x, z)
        // Effetti particellari avanzati con fisica realistica
        createSparkEffect(x, z)
        createShockwaveEffect(x, z)
        createDynamicLightFlash(x, z)
    }
    
    private fun createSparkEffect(x: Float, z: Float) {
        // Spark particles con traiettorie fisiche
    }
    
    private fun createShockwaveEffect(x: Float, z: Float) {
        // Onde d'urto radiali nel terreno
    }
    
    private fun createDynamicLightFlash(x: Float, z: Float) {
        // Flash di luce dinamica temporaneo
    }
}

// Audio spaziale 3D ENGINE PROPRIETARIO  
object SoundFX {
    fun playImpact(file: String, position: Float3 = Float3(0f, 0f, 0f)) {
        // Simula: AudioEngine.play3D(file, position = Camera.position)
        play3DAudio(file, position)
    }
    
    private fun play3DAudio(file: String, position: Float3) {
        // Audio 3D posizionale avanzato
        calculateDistanceAttenuation(position)
        applyDopplerEffect(position)
        addEnvironmentalReverb(position)
        
        // Placeholder per actual audio playback
        // ExoPlayer o Oboe implementation
    }
    
    private fun calculateDistanceAttenuation(position: Float3) {
        // Calcolo attenuazione per distanza dalla camera
        // Inverse square law: intensity ∝ 1/distance²
    }
    
    private fun applyDopplerEffect(position: Float3) {
        // Effetto Doppler per oggetti in movimento
        // Frequency shift basato su velocità relativa
    }
    
    private fun addEnvironmentalReverb(position: Float3) {
        // Reverb ambientale basato sulla geometria della scena
        // RT60 calculations per materiali diversi
    }
}

// GameBoard representation per GodWardenAI
data class GameBoard(
    val walls: List<WallState>,
    val playerPositions: List<Pair<Int, Int>>
) {
    fun availableWalls(): List<WallState> {
        val candidates = mutableListOf<WallState>()
        for (r in 0..8) {
            for (c in 0..8) {
                if (c < 8 && walls.none { it.r == r && it.c == c && it.orientation == 'h' }) {
                    candidates.add(WallState(r, c, 'h', "normale"))
                }
                if (r < 8 && walls.none { it.r == r && it.c == c && it.orientation == 'v' }) {
                    candidates.add(WallState(r, c, 'v', "normale"))
                }
            }
        }
        return candidates
    }
    
    fun simulateWithWall(wall: WallState): GameBoard {
        return copy(walls = walls + wall)
    }
}

// AI Move representation
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

// AI Heuristic evaluator
object AIHeuristic {
    fun evaluateWallPlacement(board: GameBoard): Int {
        // Valuta la qualità del piazzamento muro
        val pathLengths = board.playerPositions.mapIndexed { index, pos ->
            val goalRange = if (index == 0) 8..8 else 0..0
            computeShortestPath(pos, board.walls, goalRange)
        }
        
        // Massimizza la somma delle distanze minime
        return pathLengths.sum()
    }
}