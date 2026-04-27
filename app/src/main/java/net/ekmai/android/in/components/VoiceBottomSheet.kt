package net.ekmai.android.`in`.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.ekmai.android.`in`.utilities.VoiceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceBottomSheet(
    state: VoiceState,
    rmsLevel: Float,
    accentColor: Color,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onSend: (String) -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Text(
                text = when (state) {
                    is VoiceState.Idle       -> "Tap to speak"
                    is VoiceState.Listening  -> "Listening..."
                    is VoiceState.Processing -> "Processing..."
                    is VoiceState.Result     -> "Got it!"
                    is VoiceState.Error      -> "Try again"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            VoiceVisualizer(
                state = state,
                rmsLevel = rmsLevel,
                accentColor = accentColor,
                onStop = onStop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (state) {
                        is VoiceState.Idle       -> "Say something..."
                        is VoiceState.Listening  -> "Speak now, tap mic to stop"
                        is VoiceState.Processing -> "Processing your speech..."
                        is VoiceState.Result     -> state.text
                        is VoiceState.Error      -> state.message
                    },
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    color = when (state) {
                        is VoiceState.Error  -> MaterialTheme.colorScheme.error
                        is VoiceState.Result -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Outlined.Close, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cancel")
                }

                when (state) {
                    is VoiceState.Result -> {
                        Button(
                            onClick = { onSend(state.text) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.weight(2f).height(50.dp)
                        ) {
                            Icon(Icons.Outlined.Send, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Send", fontWeight = FontWeight.Medium)
                        }
                    }
                    is VoiceState.Error -> {
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.weight(2f).height(50.dp)
                        ) {
                            Icon(Icons.Outlined.Mic, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Try Again", fontWeight = FontWeight.Medium)
                        }
                    }
                    else -> {
                        Spacer(modifier = Modifier.weight(2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceVisualizer(
    state: VoiceState,
    rmsLevel: Float,
    accentColor: Color,
    onStop: () -> Unit
) {
    val isListening = state is VoiceState.Listening
    val isProcessing = state is VoiceState.Processing

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "idle"
    )

    val targetScale = when {
        isListening  -> 1f + (rmsLevel * 0.35f)
        isProcessing -> 1f
        else         -> idlePulse
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val ring1Alpha by animateFloatAsState(
        targetValue = if (isListening) rmsLevel * 0.4f else 0f,
        animationSpec = tween(100), label = "r1"
    )
    val ring2Alpha by animateFloatAsState(
        targetValue = if (isListening) rmsLevel * 0.2f else 0f,
        animationSpec = tween(150), label = "r2"
    )

    val processingRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "rot"
    )

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = ring2Alpha))
        )

        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = ring1Alpha))
        )

        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(90.dp),
                color = accentColor,
                strokeWidth = 2.dp,
                trackColor = accentColor.copy(alpha = 0.15f)
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(
                    when {
                        isListening  -> accentColor
                        isProcessing -> accentColor.copy(alpha = 0.6f)
                        else         -> accentColor.copy(alpha = 0.12f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isListening) {
                IconButton(onClick = onStop, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Outlined.MicOff,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = if (isProcessing) Icons.Outlined.Mic else Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = if (isProcessing) Color.White else accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}