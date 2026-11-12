package lab.neurotrino.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import lab.neurotrino.app.ui.theme.LocalLabColors
import lab.neurotrino.app.ui.theme.PanelShape
import lab.neurotrino.app.ui.theme.WellShape

/**
 * Backdrop: a quiet observatory field — vertical gradient, two restrained
 * light blooms, a fine dot grid, orbital rings in the upper right and a
 * soft vignette at the bottom. Light theme mirrors the same structure with
 * paper-like tones. No neon, no noise.
 */
@Composable
fun LabField(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val lab = LocalLabColors.current
    Box(modifier.fillMaxSize().background(lab.backdropBottom)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    colors = listOf(lab.backdropTop, lab.backdropBottom),
                    startY = 0f,
                    endY = size.height,
                ),
            )
            // dot grid — the "bench mat" of the laboratory
            val step = 30.dp.toPx()
            var x = step / 2
            while (x < size.width) {
                var y = step / 2
                while (y < size.height) {
                    drawCircle(lab.dotGrid, radius = 1.1f, center = Offset(x, y))
                    y += step
                }
                x += step
            }
            // field lines — long, faint diagonals, like etched reference guides
            val lineColor = lab.fieldLine
            val diag = size.width * 0.9f
            drawLine(lineColor, Offset(0f, size.height * 0.32f), Offset(diag, 0f), 1.4f)
            drawLine(lineColor, Offset(0f, size.height * 0.62f), Offset(size.width, size.height * 0.08f), 1.4f)
            drawLine(lineColor, Offset(size.width * 0.18f, size.height), Offset(size.width, size.height * 0.42f), 1.4f)
            // top-right orbital rings with tick marks
            val cx = size.width * 0.88f
            val cy = size.height * 0.085f
            listOf(42.dp, 74.dp, 106.dp).forEachIndexed { index, radius ->
                val r = radius.toPx()
                drawCircle(lab.ring, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.6f))
                val ticks = 4 + index * 2
                for (i in 0 until ticks) {
                    val angle = (i * 360f / ticks + index * 17f) * Math.PI.toFloat() / 180f
                    val inner = Offset(cx + kotlin.math.cos(angle) * (r - 6f), cy + kotlin.math.sin(angle) * (r - 6f))
                    val outer = Offset(cx + kotlin.math.cos(angle) * (r + 6f), cy + kotlin.math.sin(angle) * (r + 6f))
                    drawLine(lab.ring, inner, outer, 1.6f)
                }
            }
            // light blooms
            drawCircle(
                Brush.radialGradient(
                    colors = listOf(lab.glowCyan, Color.Transparent),
                    center = Offset(size.width * 0.06f, size.height * 0.04f),
                    radius = size.minDimension * 0.95f,
                ),
                radius = size.minDimension * 0.95f,
                center = Offset(size.width * 0.06f, size.height * 0.04f),
            )
            drawCircle(
                Brush.radialGradient(
                    colors = listOf(lab.glowIndigo, Color.Transparent),
                    center = Offset(size.width * 1.02f, size.height * 0.10f),
                    radius = size.minDimension * 0.75f,
                ),
                radius = size.minDimension * 0.75f,
                center = Offset(size.width * 1.02f, size.height * 0.10f),
            )
            // bottom vignette — keeps the action zone calm
            drawRect(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, lab.backdropBottom.copy(alpha = 0.85f)),
                    startY = size.height * 0.72f,
                    endY = size.height,
                ),
            )
        }
        content()
    }
}

@Composable
fun Kicker(text: String, color: Color = MaterialTheme.colorScheme.secondary, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
fun LabMark(modifier: Modifier = Modifier, size: Dp = 34.dp) {
    val lab = LocalLabColors.current
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val s = w / 48f
        // outer orbit
        drawCircle(
            color = Color(0xFF63E2CE).copy(alpha = 0.9f),
            radius = 20f * s,
            center = Offset(w / 2f, w / 2f),
            style = Stroke(width = 2.6f * s),
        )
        // orbit ring (elliptical)
        drawArc(
            color = Color(0xFF8FA8FF).copy(alpha = 0.55f),
            startAngle = -35f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(4f * s, 15f * s),
            size = androidx.compose.ui.geometry.Size(40f * s, 18f * s),
            style = Stroke(width = 1.8f * s, cap = StrokeCap.Round),
        )
        // nucleus
        drawCircle(Color(0xFFE4B56A), radius = 6.4f * s, center = Offset(w / 2f, w / 2f))
        drawCircle(Color(0xFF0C1016), radius = 2.6f * s, center = Offset(w / 2f, w / 2f))
    }
}

/**
 * Panel: the main surface of the app. Layered vertical gradient, hairline
 * border with an energized top edge, and small "instrument" corner ticks.
 */
@Composable
fun InstrumentPanel(
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFF63E2CE),
    strong: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val lab = LocalLabColors.current
    val border = if (strong) lab.panelBorderStrong else lab.panelBorder
    Column(
        modifier
            .clip(PanelShape)
            .background(Brush.verticalGradient(listOf(lab.panelTop, lab.panelBottom)))
            .border(1.dp, border, PanelShape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        content = content,
    )
}

/** The capture surface — a lit well on the bench. */
@Composable
fun CaptureWell(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val lab = LocalLabColors.current
    Column(
        modifier
            .fillMaxWidth()
            .clip(WellShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        lab.wellTop,
                        lab.wellBottom,
                    ),
                ),
            )
            .border(1.dp, lab.panelBorderStrong, WellShape)
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun SectionHeader(
    kicker: String,
    modifier: Modifier = Modifier,
    kickerColor: Color = MaterialTheme.colorScheme.secondary,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Kicker(kicker, kickerColor)
        action?.invoke()
    }
}

@Composable
fun LabDivider(modifier: Modifier = Modifier, indent: Dp = 0.dp) {
    val lab = LocalLabColors.current
    Canvas(modifier.fillMaxWidth().height(1.dp).padding(start = indent)) {
        drawLine(lab.panelBorder, Offset(0f, 0.5f), Offset(size.width, 0.5f), 1f)
    }
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    InstrumentPanel(modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        if (actionLabel != null && onAction != null) {
            androidx.compose.material3.TextButton(onClick = onAction, modifier = Modifier.padding(top = 4.dp)) {
                Text(actionLabel)
            }
        }
    }
}

/** Borderless text-field colors for the manuscript-style capture/title fields. */
@Composable
fun transparentTextFieldColors() = androidx.compose.material3.TextFieldDefaults.colors(
    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
)

/** Small pill badge for provenance/sample/status markers. */
@Composable
fun LabBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(percent = 50))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.4f), androidx.compose.foundation.shape.RoundedCornerShape(percent = 50))
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
        )
    }
}

/** Row with press feedback for list items — subtle scale, no bounce. */
@Composable
fun PressablePanel(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFF63E2CE),
    content: @Composable ColumnScope.() -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, tween(120), label = "press")
    InstrumentPanel(
        modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() },
                )
            },
        accent = accent,
        content = content,
    )
}

