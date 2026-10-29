package stepup.app222shred.corpapp.ui.composable.approot.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import stepup.app222shred.corpapp.ui.composable.approot.background.BackgroundPattern
import stepup.app222shred.corpapp.ui.composable.approot.background.PlacedIcon
import stepup.app222shred.corpapp.ui.composable.approot.background.placeBackgroundIcons
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val SCALE_AMPLITUDE = 0.18f

/** The whole wave repeats every [PERIOD_SECONDS]; it divides the clock loop evenly. */
private const val PERIOD_SECONDS = 5f
private const val CLOCK_LOOP_SECONDS = 1_000f

/** How much of a cycle the wave takes to cross the screen diagonally. */
private const val WAVE_SPREAD = 0.5f

@Composable
fun AppBackgroundDecor(pattern: BackgroundPattern, modifier: Modifier = Modifier) {
    val items = remember(pattern) {
        placeBackgroundIcons(Random(System.nanoTime()), pattern)
    }

    val clock = rememberInfiniteTransition(label = "menuBackgroundWave").animateFloat(
        initialValue = 0f,
        targetValue = CLOCK_LOOP_SECONDS,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (CLOCK_LOOP_SECONDS * 1_000).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "clock",
    )

    Box(modifier = modifier) {
        items.forEach { item -> WaveDecorIcon(item, clock = { clock.value }) }
    }
}

@Composable
private fun BoxScope.WaveDecorIcon(item: PlacedIcon, clock: () -> Float) {
    // Position on the diagonal, from 0 at the upper left to 1 at the lower right
    val diagonal = (item.anchor.horizontalBias + item.anchor.verticalBias + 2f) / 4f

    Icon(
        imageVector = item.icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .align(item.anchor)
            .size(item.size)
            .graphicsLayer {
                val cycle = clock() / PERIOD_SECONDS - diagonal * WAVE_SPREAD
                val scale = 1f + SCALE_AMPLITUDE * sin(2f * PI.toFloat() * cycle)
                scaleX = scale
                scaleY = scale
                alpha = item.baseAlpha
            },
    )
}
