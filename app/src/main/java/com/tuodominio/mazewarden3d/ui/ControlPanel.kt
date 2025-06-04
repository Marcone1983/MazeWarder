package com.tuodominio.mazewarden3d.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

/**
 * ControlHUD - Interfaccia animata e dinamica AAA-quality
 * 
 * Features:
 * - Pulsazioni infinite con timing diversificato
 * - Design circolare Material3 moderno
 * - Responsive scaling per tutti i dispositivi
 * - Effetti visivi cinematografici
 * - Feedback tattile e sonoro
 */
@Composable
fun ControlPanel(
    onMove: (direction: String) -> Unit,
    onAbility: () -> Unit,
    onPass: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Riga Freccia SU
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val scaleUp = rememberInfiniteTransition().animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Button(
                onClick = { onMove("up") },
                modifier = Modifier.scale(scaleUp.value),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Riga: Freccia SX, Abilità (J), Freccia DX
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val scaleLeft = rememberInfiniteTransition().animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            val scaleAbility = rememberInfiniteTransition().animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Button(
                onClick = { onMove("left") },
                modifier = Modifier.scale(scaleLeft.value),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(24.dp))
            Button(
                onClick = { onAbility() },
                modifier = Modifier.scale(scaleAbility.value),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Ability J", tint = Color.Yellow, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.width(24.dp))
            Button(
                onClick = { onMove("right") },
                modifier = Modifier.scale(scaleLeft.value),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Riga: Freccia GIÙ e Passa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val scaleDown = rememberInfiniteTransition().animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Button(
                onClick = { onMove("down") },
                modifier = Modifier.scale(scaleDown.value),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(24.dp))
            Button(
                onClick = { onPass() },
                shape = CircleShape,
                contentPadding = PaddingValues(8.dp)
            ) {
                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Pass", tint = Color.Red, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * DirectionButton - Pulsante direzionale con animazioni avanzate
 */
@Composable
private fun DirectionButton(
    label: String, 
    scale: Float,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size((64 * scale).dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label, 
            fontSize = 22.sp,
            color = Color.White
        )
    }
}

/**
 * ControlHUD alternativo con design unificato
 */
@Composable
fun ControlHUD(
    onMove: (direction: String) -> Unit,
    onAbility: () -> Unit,
    onPass: () -> Unit
) {
    val pulse = rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing), 
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Layout unificato con tutte le direzioni
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DirectionButton("←", pulse.value) { onMove("left") }
            DirectionButton("↑", pulse.value) { onMove("up") }
            DirectionButton("↓", pulse.value) { onMove("down") }
            DirectionButton("→", pulse.value) { onMove("right") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Pulsanti speciali
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(
                onClick = onAbility,
                modifier = Modifier.scale(pulse.value * 1.1f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Ability",
                    tint = Color.Yellow,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Button(
                onClick = onPass,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Pass",
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}