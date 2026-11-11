package lab.neurotrino.app.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.RelationType
import lab.neurotrino.app.domain.Relationship
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Amber
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.Indigo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class GraphNode(val id: String, val label: String, val kind: String, val hrefKind: String)

@Composable
fun GraphScreen(
    thoughts: List<Thought>,
    projects: List<Project>,
    reading: List<ReadingItem>,
    relationships: List<Relationship>,
    onOpenThought: (String) -> Unit,
    onOpenProject: (String) -> Unit,
    onUnlink: (String) -> Unit,
) {
    var filter by remember { mutableStateOf<RelationType?>(null) }
    var focus by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val nodes = remember(thoughts, projects, reading) {
        thoughts.filter { it.status != ThoughtStatus.archived }.map {
            GraphNode(it.id, it.title.ifBlank { "myśl" }, "thought", "thought")
        } + projects.map { GraphNode(it.id, it.title, "project", "project") } +
            reading.map { GraphNode(it.id, it.title, "reading", "reading") }
    }
    val visible = relationships.filter { filter == null || it.relationType == filter }
        .filter { focus == null || it.fromId == focus || it.toId == focus }

    LabField {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Kicker(stringResource(R.string.graph_kicker))
            Text(stringResource(R.string.graph_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.graph_lede), color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChip(selected = focus == null, onClick = { focus = null }, label = { Text(stringResource(R.string.graph_all)) })
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .pointerInput(nodes, visible) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.6f, 2.4f)
                            offset += pan
                        }
                    }
                    .pointerInput(nodes) {
                        detectTapGestures { tap ->
                            val n = nodes.getOrNull(((tap.x / size.width) * nodes.size).toInt().coerceIn(0, (nodes.size - 1).coerceAtLeast(0)))
                            if (n != null) focus = n.id
                        }
                    },
            ) {
                val cx = size.width / 2 + offset.x
                val cy = size.height / 2 + offset.y
                fun pos(index: Int, kind: String): Offset {
                    val angle = (index / nodes.size.coerceAtLeast(1).toFloat()) * 2 * PI
                    val r = when (kind) {
                        "project" -> 90f
                        "reading" -> 150f
                        else -> 120f
                    } * scale
                    return Offset(cx + (cos(angle) * r).toFloat(), cy + (sin(angle) * r).toFloat())
                }
                val placed = nodes.mapIndexed { i, n -> n to pos(i, n.kind) }
                val map = placed.associate { it.first.id to it.second }
                visible.forEach { rel ->
                    val a = map[rel.fromId] ?: return@forEach
                    val b = map[rel.toId] ?: return@forEach
                    drawLine(Indigo.copy(alpha = 0.55f), a, b, strokeWidth = 2.2f)
                }
                placed.forEach { (node, p) ->
                    val color = when (node.kind) {
                        "project" -> Amber
                        "reading" -> Indigo
                        else -> Cyan
                    }
                    drawCircle(color.copy(alpha = 0.25f), radius = if (focus == node.id) 22f else 14f, center = p)
                    drawCircle(color, radius = if (focus == node.id) 10f else 6f, center = p)
                }
            }
            Kicker(stringResource(R.string.graph_list))
            if (visible.isEmpty()) {
                InstrumentPanel { Text(stringResource(R.string.graph_empty)) }
            } else {
                visible.forEach { rel ->
                    val from = nodes.find { it.id == rel.fromId }
                    val to = nodes.find { it.id == rel.toId }
                    InstrumentPanel(Modifier.padding(vertical = 6.dp)) {
                        Kicker(stringResource(rel.relationType.labelRes()))
                        Text("${from?.label ?: rel.fromId} → ${to?.label ?: rel.toId}")
                        Text(
                            rel.explanation.ifBlank { stringResource(rel.relationType.hintRes()) },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = {
                            if (from?.hrefKind == "thought") onOpenThought(rel.fromId)
                            if (from?.hrefKind == "project") onOpenProject(rel.fromId)
                        }) { Text(from?.label ?: "") }
                        TextButton(onClick = { onUnlink(rel.id) }) { Text(stringResource(R.string.action_remove_link)) }
                    }
                }
            }
        }
    }
}
