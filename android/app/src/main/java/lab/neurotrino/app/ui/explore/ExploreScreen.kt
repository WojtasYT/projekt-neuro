package lab.neurotrino.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.Spark
import lab.neurotrino.app.domain.SparksCatalog
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.ui.components.CaptureWell
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.SectionHeader
import lab.neurotrino.app.ui.theme.Amber
import lab.neurotrino.app.ui.theme.Cyan
import lab.neurotrino.app.ui.theme.Indigo

@Composable
fun ExploreScreen(
    thoughts: List<Thought>,
    reading: List<ReadingItem>,
    onSave: (Spark) -> Unit,
    onOpenThought: (String) -> Unit,
) {
    var avoid by remember { mutableStateOf(setOf<String>()) }
    var current by remember { mutableStateOf<Spark?>(null) }
    var deeper by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val picked = remember(thoughts, reading, avoid) { SparksCatalog.pick(thoughts, reading, avoid) }
    val spark = current ?: picked
    val domains = remember { SparksCatalog.items.flatMap { it.domains }.distinct().sorted() }

    LabField {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Kicker(stringResource(R.string.explore_kicker))
            Text(stringResource(R.string.explore_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.explore_lede), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))

            // ── the one spark, on the bench ────────────────────────────────────
            CaptureWell {
                Kicker(
                    stringResource(
                        if (spark.generatedBy == "catalog") R.string.explore_from_catalog else R.string.explore_from_notes,
                    ),
                    Cyan,
                )
                Spacer(Modifier.height(10.dp))
                Text(spark.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(spark.hook, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text(spark.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (deeper && spark.deeper.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Indigo.copy(alpha = 0.10f))
                            .border(1.dp, Indigo.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Text(spark.deeper, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (spark.why.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(spark.why, style = MaterialTheme.typography.bodySmall, color = Amber)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "${stringResource(R.string.explore_question)}: ${spark.question}",
                    style = MaterialTheme.typography.titleSmall,
                )
                if (spark.sourceNote.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(spark.sourceNote, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                if (saved) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = Cyan)
                        Text(stringResource(R.string.explore_saved), style = MaterialTheme.typography.titleSmall, color = Cyan)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        val linked = spark.linkedThoughtIds.firstOrNull()
                        if (linked != null) {
                            Button(onClick = { onOpenThought(linked) }) {
                                Text(stringResource(R.string.thoughts_open))
                            }
                        } else {
                            Button(onClick = { onSave(spark); saved = true }) {
                                Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(stringResource(R.string.action_save_as_thought))
                            }
                        }
                        OutlinedButton(onClick = {
                            avoid = avoid + spark.id
                            current = SparksCatalog.pick(thoughts, reading, avoid + spark.id)
                            deeper = false
                            saved = false
                        }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.action_other_trail))
                        }
                    }
                    if (spark.deeper.isNotBlank()) {
                        TextButton(onClick = { deeper = !deeper }, modifier = Modifier.padding(top = 2.dp)) {
                            Text(stringResource(if (deeper) R.string.action_hide_mechanism else R.string.action_show_mechanism))
                        }
                    }
                }
                Text(
                    stringResource(R.string.explore_sources_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            // ── finite catalog ─────────────────────────────────────────────────
            Spacer(Modifier.height(18.dp))
            SectionHeader(stringResource(R.string.explore_catalog), kickerColor = Indigo)
            Text(
                stringResource(R.string.explore_catalog_note, SparksCatalog.items.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            SparksCatalog.items.forEach { item ->
                InstrumentPanel(
                    Modifier
                        .padding(vertical = 5.dp)
                        .clickable { current = item; deeper = false; saved = false },
                    accent = if (current?.id == item.id) Cyan.copy(alpha = 0.6f) else Indigo.copy(alpha = 0.25f),
                ) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        item.hook,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        item.domains.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Indigo,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}
