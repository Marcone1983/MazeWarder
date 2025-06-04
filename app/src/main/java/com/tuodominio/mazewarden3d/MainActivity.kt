package com.marcone1983.mazewarden3d

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.filament.utils.Options
import com.tuodominio.mazewarden3d.ui.ControlPanel
import com.marcone1983.mazewarden3d.viewmodel.GameViewModel
import com.marcone1983.mazewarden3d.model.Direction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var renderer: GameRenderer
    private lateinit var voicePlayer: VoicePlayer
    private var shouldStartTutorial = false
    
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Modalità full-screen immersiva - API moderna
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - usa WindowInsetsController
            window.insetsController?.let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 e precedenti - usa la vecchia API
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }

        // Controlla se deve avviare il tutorial
        shouldStartTutorial = intent.getBooleanExtra("start_tutorial", false)

        // Inizializziamo il renderer Filament e VoicePlayer
        renderer = GameRenderer(this)
        voicePlayer = VoicePlayer(this)

        setContent {
            MazeWardenApp()
        }
    }

    override fun onResume() {
        super.onResume()
        renderer.onResume()
    }

    override fun onPause() {
        super.onPause()
        renderer.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        renderer.onDestroy()
        if (::voicePlayer.isInitialized) {
            voicePlayer.cleanup()
        }
        GameAudioFX.cleanup()
    }

    @Composable
    fun MazeWardenApp() {
        val gameState by gameViewModel.gameState.collectAsStateWithLifecycle()
        val playerState by gameViewModel.playerState.collectAsStateWithLifecycle()
        val audioState by gameViewModel.audioState.collectAsStateWithLifecycle()
        
        // Handle audio state changes
        LaunchedEffect(audioState.lastSound) {
            audioState.lastSound?.let { sound ->
                when (sound) {
                    com.marcone1983.mazewarden3d.model.SoundEffect.STEP -> GameAudioFX.playStep()
                    com.marcone1983.mazewarden3d.model.SoundEffect.WALL_HIT -> GameAudioFX.playWallHit()
                    com.marcone1983.mazewarden3d.model.SoundEffect.SKILL_USE -> GameAudioFX.playSkill()
                    com.marcone1983.mazewarden3d.model.SoundEffect.VICTORY -> {
                        GameAudioFX.playSkill()
                        voicePlayer.announceVictory()
                    }
                }
            }
        }
        
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. SurfaceView di Filament occupa tutto lo schermo
                AndroidView(factory = { context ->
                    renderer.getFilamentView().apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                })

                // 2. Sovrapponiamo i controlli Compose in trasparenza
                ControlPanel(
                    onMove = { dir -> 
                        val direction = when (dir) {
                            "up" -> Direction.UP
                            "down" -> Direction.DOWN
                            "left" -> Direction.LEFT
                            "right" -> Direction.RIGHT
                            else -> Direction.UP
                        }
                        gameViewModel.movePlayer(direction)
                        renderer.onMove(dir)
                    },
                    onAbility = { 
                        gameViewModel.useSkill(playerState.character.skills.first())
                        renderer.onAbility() 
                    },
                    onPass = { renderer.onPass() }
                )
                
                // 3. Game over/victory overlay
                if (gameState.isGameWon) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "VITTORIA!",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Punteggio: ${gameState.finalScore}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { gameViewModel.resetGame() }) {
                                Text("Gioca Ancora")
                            }
                        }
                    }
                }
            }
        }
    }
}