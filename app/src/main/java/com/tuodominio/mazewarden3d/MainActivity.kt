package com.tuodominio.mazewarden3d

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.google.android.filament.utils.Options
import com.tuodominio.mazewarden3d.ui.ControlPanel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var renderer: GameRenderer
    private lateinit var voicePlayer: VoicePlayer
    private var shouldStartTutorial = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Modalità full-screen immersiva
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

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

    @Composable
    fun MazeWardenApp() {
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
                    onMove = { dir -> renderer.onMove(dir) },
                    onAbility = { renderer.onAbility() },
                    onPass = { renderer.onPass() }
                )
            }
        }
    }
}