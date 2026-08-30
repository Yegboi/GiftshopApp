package com.example.steamfun.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** One scrap of paper. Positions are 0..1 of the canvas, so no size is needed up front. */
private class Piece(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var rotation: Float,
    val spin: Float,
    val color: Color,
    val scale: Float,
)

private val Palette = listOf(
    Color(0xFF66C0F4),
    Color(0xFFA4D007),
    Color(0xFFFFC400),
    Color(0xFFFF6E8A),
    Color(0xFFB388FF),
    Color(0xFFFFFFFF),
)

/**
 * A burst of confetti for a correct guess.
 *
 * Pieces fly outward from just below the middle, biased upward, then gravity
 * takes them down past the bottom edge, at which point the animation stops on
 * its own. Drawing reads [tick], so each frame repaints without recomposing
 * the rest of the screen.
 */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier, pieceCount: Int = 120) {
    val pieces = remember {
        val random = Random(System.nanoTime())
        List(pieceCount) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = LAUNCH_SPEED_MIN + random.nextFloat() * LAUNCH_SPEED_SPREAD
            Piece(
                x = 0.5f,
                y = 0.58f,
                velocityX = cos(angle) * speed,
                velocityY = sin(angle) * speed - UPWARD_BIAS,
                rotation = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 2f * MAX_SPIN,
                color = Palette[random.nextInt(Palette.size)],
                scale = 0.6f + random.nextFloat() * 0.9f,
            )
        }
    }
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(pieces) {
        var previous = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            // Cap the step so a stalled frame cannot teleport everything offscreen.
            val dt = ((now - previous) / 1_000_000_000.0).toFloat().coerceIn(0f, 0.05f)
            previous = now

            var anyVisible = false
            pieces.forEach { piece ->
                piece.velocityY += GRAVITY * dt
                piece.velocityX -= piece.velocityX * DRAG * dt
                piece.x += piece.velocityX * dt
                piece.y += piece.velocityY * dt
                piece.rotation += piece.spin * dt
                if (piece.y < BOTTOM_LIMIT) anyVisible = true
            }
            tick++
            if (!anyVisible) break
        }
    }

    Canvas(modifier) {
        @Suppress("UNUSED_EXPRESSION")
        tick // read in the draw phase so every frame repaints

        val base = size.minDimension * PIECE_SIZE_FACTOR
        pieces.forEach { piece ->
            val center = Offset(piece.x * size.width, piece.y * size.height)
            val width = base * piece.scale
            val height = width * 1.8f
            rotate(degrees = piece.rotation, pivot = center) {
                drawRect(
                    color = piece.color,
                    topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
                    size = Size(width, height),
                )
            }
        }
    }
}

private const val GRAVITY = 1.7f
private const val DRAG = 0.9f
private const val UPWARD_BIAS = 0.55f
private const val LAUNCH_SPEED_MIN = 0.35f
private const val LAUNCH_SPEED_SPREAD = 0.75f
private const val MAX_SPIN = 520f
private const val BOTTOM_LIMIT = 1.25f
private const val PIECE_SIZE_FACTOR = 0.018f
