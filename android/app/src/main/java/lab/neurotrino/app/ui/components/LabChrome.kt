package lab.neurotrino.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.ui.theme.Amber
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.Graphite
import lab.neurotrino.app.ui.theme.Indigo

@Composable
fun LabField(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(bg)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Cyan.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.12f, size.height * 0.02f),
                    radius = size.minDimension * 0.85f,
                ),
                radius = size.minDimension * 0.85f,
                center = Offset(size.width * 0.12f, 0f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Indigo.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.95f, size.height * 0.08f),
                    radius = size.minDimension * 0.7f,
                ),
                radius = size.minDimension * 0.7f,
                center = Offset(size.width, size.height * 0.05f),
            )
            val cx = size.width * 0.82f
            val cy = size.height * 0.18f
            for (i in 1..3) {
                drawCircle(
                    color = Cyan.copy(alpha = 0.07f),
                    radius = 48f * i,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2f),
                )
            }
            for (x in 0..8) {
                val px = size.width * (x / 8f)
                drawLine(Color.White.copy(alpha = 0.03f), Offset(px, 0f), Offset(px, size.height), 1f)
            }
        }
        content()
    }
}

@Composable
fun Kicker(text: String, color: Color = Amber) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun InstrumentPanel(
    modifier: Modifier = Modifier,
    accent: Color = Cyan.copy(alpha = 0.35f),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                    ),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(18.dp),
        content = content,
    )
}

@Composable
fun CaptureWell(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Cyan.copy(alpha = 0.10f), Graphite.copy(alpha = 0.55f)),
                ),
            )
            .border(1.dp, Cyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .padding(16.dp),
        content = content,
    )
}
