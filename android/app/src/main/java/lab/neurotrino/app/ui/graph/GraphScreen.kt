package lab.neurotrino.app.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clip
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
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
import lab.neurotrino.app.ui.components.SectionHeader
import lab.neurotrino.app.ui.hintRes
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Amber
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.Indigo
import lab.neurotrino.app.ui.theme.PanelShape

private data class GraphNode(
    val id: String,
    val label: String,
    val kind: GraphNodeKind,
)

private enum class GraphNodeKind { Thought, Project, Reading }

private fun GraphNodeKind.color(): Color = when (this) {
    GraphNodeKind.Thought -> Cyan
    GraphNodeKind.Project -> Amber
    GraphNodeKind.Reading -> Indigo
}

@Composable
fun GraphScreen(
    thoughts: List<Thought>,
    projects: List<Project>,
    reading: List<ReadingItem>,
    relationships: List<Relationship>,
    onOpenThought: (String) -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenBook: (String) -> Unit,
    onUnlink: (String) -> Unit,
) {
    var typeFilter by remember { mutableStateOf<RelationType?>(null) }
    var focus by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    val nodes = remember(thoughts, projects, reading) {
        buildList {
            projects.forEach { add(GraphNode(it.id, it.title, GraphNodeKind.Project)) }
            thoughts
                .filter { it.status != ThoughtStatus.archived }
                .sortedBy { it.createdAt }
                .forEach { add(GraphNode(it.id, it.title.ifBlank { "myśl" }, GraphNodeKind.Thought)) }
            reading.forEach { add(GraphNode(it.id, it.title, GraphNodeKind.Reading)) }
        }
    }
    val filtered = relationships
        .filter { typeFilter == null || it.relationType == typeFilter }
        .filter { focus == null || it.fromId == focus || it.toId == focus }
    val visibleIds = if (focus == null) nodes.map { it.id }.toSet()
        else buildSet {
            add(focus!!)
            filtered.forEach { add(it.fromId); add(it.toId) }
        }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface)

    LabField {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Kicker(stringResource(R.string.graph_kicker))
            Text(stringResource(R.string.graph_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.graph_lede), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))

            // relation type filter
            Kicker(stringResource(R.string.graph_filter))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = typeFilter == null, onClick = { typeFilter = null }, label = { Text(stringResource(R.string.graph_all)) })
                RelationType.entries.forEach { option ->
                    FilterChip(
                        selected = typeFilter == option,
                        onClick = { typeFilter = if (typeFilter == option) null else option },
                        label = { Text(stringResource(option.labelRes())) },
                    )
                }
            }

            // legend
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                LegendDot(GraphNodeKind.Thought.color(), stringResource(R.string.graph_legend_thought))
                LegendDot(GraphNodeKind.Project.color(), stringResource(R.string.graph_legend_project))
                LegendDot(GraphNodeKind.Reading.color(), stringResource(R.string.graph_legend_reading))
            }

            // ── canvas ────────────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(vertical = 10.dp)
                    .clip(PanelShape)
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.surface)))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, PanelShape)
                    .pointerInput(nodes) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.55f, 2.8f)
                            offset += pan
                        }
                    }
                    .pointerInput(nodes, scale, offset, typeFilter, focus) {
                        detectTapGestures(
                            onDoubleTap = { scale = 1f; offset = Offset.Zero },
                            onTap = { tap ->
                                val layout = layoutNodesPx(nodes, size.width.toFloat(), size.height.toFloat(), scale, offset)
                                val hit = layout.entries
                                    .map { (id, pos) -> id to (pos - tap).getDistance() }
                                    .filter { it.second <= with(density) { 44.dp.toPx() } }
                                    .minByOrNull { it.second }
                                    ?.first
                                focus = if (hit == focus) null else hit
                            },
                        )
                    },
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val positions = layoutNodesPx(nodes, size.width, size.height, scale, offset)
                    // edges first (under nodes)
                    filtered.forEach { rel ->
                        val a = positions[rel.fromId] ?: return@forEach
                        val b = positions[rel.toId] ?: return@forEach
                        val emph = focus == null || rel.fromId == focus || rel.toId == focus
                        drawLine(
                            color = Indigo.copy(alpha = if (emph) 0.75f else 0.18f),
                            start = a,
                            end = b,
                            strokeWidth = if (emph) 2.6f else 1.4f,
                        )
                    }
                    nodes.forEach { node ->
                        val p = positions[node.id] ?: return@forEach
                        val dim = node.id !in visibleIds
                        val color = node.kind.color().copy(alpha = if (dim) 0.25f else 1f)
                        val isFocus = focus == node.id
                        // halo
                        drawCircle(color.copy(alpha = if (isFocus) 0.32f else 0.18f), radius = if (isFocus) 22f else 15f, center = p)
                        drawCircle(color, radius = if (isFocus) 9.5f else 6.5f, center = p)
                        if (isFocus) {
                            drawCircle(color.copy(alpha = 0.9f), radius = 13f, center = p, style = androidx.compose.ui.graphics.drawscope.Stroke(1.6f))
                        }
                        // label
                        val label = node.label.take(18)
                        val measured = textMeasurer.measure(label, labelStyle, maxLines = 1)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                (p.x - measured.size.width / 2f).coerceIn(2f, size.width - measured.size.width - 2f),
                                (p.y + 14f).coerceAtMost(size.height - measured.size.height - 2f),
                            ),
                        )
                    }
                }
                if (focus != null) {
                    TextButton(
                        onClick = { focus = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    ) { Text(stringResource(R.string.action_clear_focus)) }
                }
            }

            if (nodes.isEmpty()) {
                InstrumentPanel(Modifier.padding(vertical = 6.dp)) {
                    Text(stringResource(R.string.graph_empty_all), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.graph_empty_all_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── list (accessible alternative, same data) ──────────────────────
            SectionHeader(
                stringResource(R.string.graph_list),
                Modifier.padding(top = 4.dp),
                kickerColor = Indigo,
            )
            Spacer(Modifier.height(6.dp))
            if (filtered.isEmpty()) {
                InstrumentPanel {
                    Text(stringResource(R.string.graph_empty), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                filtered.forEach { rel ->
                    val byId = nodes.associateBy { it.id }
                    val from = byId[rel.fromId]
                    val to = byId[rel.toId]
                    InstrumentPanel(Modifier.padding(vertical = 5.dp)) {
                        Kicker(stringResource(rel.relationType.labelRes()), Indigo)
                        Spacer(Modifier.height(4.dp))
                        EndpointLink(from, rel.fromId, onOpenThought, onOpenProject, onOpenBook)
                        Text("→", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        EndpointLink(to, rel.toId, onOpenThought, onOpenProject, onOpenBook)
                        if (rel.explanation.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(rel.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(stringResource(rel.relationType.hintRes()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { onUnlink(rel.id) }) { Text(stringResource(R.string.action_remove_link)) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(9.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(percent = 50))
                .background(color),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EndpointLink(
    node: GraphNode?,
    id: String,
    onOpenThought: (String) -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenBook: (String) -> Unit,
) {
    val label = node?.label ?: id
    TextButton(
        onClick = {
            when (node?.kind) {
                GraphNodeKind.Thought -> onOpenThought(id)
                GraphNodeKind.Project -> onOpenProject(id)
                GraphNodeKind.Reading -> onOpenBook(id)
                null -> Unit
            }
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Stable, deterministic layout: projects on the inner ring, thoughts in the
 * middle, reading on the outer ring — spread evenly, rotated per ring so the
 * composition never collapses into one axis. Pure trigonometry, no physics.
 */
private fun layoutNodesPx(
    nodes: List<GraphNode>,
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset,
): Map<String, Offset> {
    if (nodes.isEmpty()) return emptyMap()
    val cx = width / 2f + offset.x
    val cy = height / 2f + offset.y
    val base = minOf(width, height)
    val radii = mapOf(
        GraphNodeKind.Project to base * 0.16f * scale,
        GraphNodeKind.Thought to base * 0.30f * scale,
        GraphNodeKind.Reading to base * 0.44f * scale,
    )
    val startAngle = mapOf(
        GraphNodeKind.Project to -90f,
        GraphNodeKind.Thought to -90f + 14f,
        GraphNodeKind.Reading to -90f + 27f,
    )
    val result = HashMap<String, Offset>()
    GraphNodeKind.entries.forEach { kind ->
        val items = nodes.filter { it.kind == kind }
        if (items.isEmpty()) return@forEach
        val radius = radii.getValue(kind)
        val step = 360f / items.size
        items.forEachIndexed { index, node ->
            val angle = Math.toRadians((index * step + startAngle.getValue(kind)).toDouble())
            result[node.id] = Offset(
                x = cx + (kotlin.math.cos(angle) * radius).toFloat(),
                y = cy + (kotlin.math.sin(angle) * radius * 0.92f).toFloat(),
            )
        }
    }
    return result
}
