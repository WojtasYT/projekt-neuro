package lab.neurotrino.app.ui.thoughts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.ThoughtType
import lab.neurotrino.app.domain.relativeTimePl
import lab.neurotrino.app.domain.searchThoughts
import lab.neurotrino.app.ui.components.EmptyState
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabBadge
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.components.PressablePanel
import lab.neurotrino.app.ui.labelRes
import lab.neurotrino.app.ui.theme.Amber
import lab.neurotrino.app.ui.theme.Cyan

@Composable
fun ThoughtsScreen(
    thoughts: List<Thought>,
    onOpen: (String) -> Unit,
    onCapture: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<ThoughtStatus?>(null) }
    var type by remember { mutableStateOf<ThoughtType?>(null) }
    var archive by remember { mutableStateOf(false) }
    val filtered = remember(thoughts, query, status, type, archive) {
        searchThoughts(thoughts, query)
            .filter { archive || it.status != ThoughtStatus.archived }
            .filter { archive || status == null || it.status == status }
            .filter { type == null || it.types.contains(type) }
            .sortedByDescending { it.updatedAt }
    }
    val activeCount = thoughts.count { it.status != ThoughtStatus.archived }

    LabField {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Kicker(stringResource(R.string.thoughts_kicker))
                    Text(stringResource(R.string.thoughts_title), style = MaterialTheme.typography.headlineLarge)
                }
                IconButton(onClick = onCapture) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.action_capture))
                }
            }
            Text(
                stringResource(R.string.thoughts_count, activeCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                placeholder = { Text(stringResource(R.string.thoughts_search)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                singleLine = true,
            )
            // status filter
            Kicker(stringResource(R.string.thoughts_filter_status))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = status == null, onClick = { status = null }, label = { Text(stringResource(R.string.thoughts_filter_any_status)) })
                ThoughtStatus.entries.filter { it != ThoughtStatus.archived || archive }.forEach { st ->
                    FilterChip(
                        selected = status == st,
                        onClick = { status = if (status == st) null else st },
                        label = { Text(stringResource(st.labelRes())) },
                    )
                }
            }
            // type filter
            Kicker(stringResource(R.string.thoughts_filter_type))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = type == null, onClick = { type = null }, label = { Text(stringResource(R.string.thoughts_filter_any_type)) })
                ThoughtType.entries.forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = if (type == t) null else t },
                        label = { Text(stringResource(t.labelRes())) },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                FilterChip(selected = archive, onClick = { archive = !archive; if (status == ThoughtStatus.archived) status = null }, label = { Text(stringResource(R.string.thoughts_show_archive)) })
                Spacer(Modifier.width(8.dp))
                if (status != null || type != null || query.isNotBlank()) {
                    TextButton(onClick = { status = null; type = null; query = "" }) {
                        Text(stringResource(R.string.action_clear_filters))
                    }
                }
            }

            if (filtered.isEmpty()) {
                if (thoughts.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.thoughts_empty_title),
                        body = stringResource(R.string.thoughts_empty_body),
                        modifier = Modifier.padding(top = 16.dp),
                        actionLabel = stringResource(R.string.action_capture),
                        onAction = onCapture,
                    )
                } else {
                    EmptyState(
                        title = stringResource(R.string.thoughts_no_match_title),
                        body = stringResource(R.string.thoughts_no_match_body),
                        modifier = Modifier.padding(top = 16.dp),
                        actionLabel = stringResource(R.string.action_clear_filters),
                        onAction = { status = null; type = null; query = ""; archive = false },
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    items(filtered, key = { it.id }) { thought ->
                        PressablePanel(onClick = { onOpen(thought.id) }) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    thought.title.ifBlank { stringResource(R.string.thoughts_untitled) },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    relativeTimePl(thought.updatedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                (thought.content.ifBlank { thought.rawText }).take(180),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(
                                Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (thought.isSample) LabBadge(stringResource(R.string.sample_badge), Amber)
                                LabBadge(stringResource(thought.status.labelRes()), if (thought.status == ThoughtStatus.exploring) Cyan else Amber.copy(alpha = 0.8f))
                                thought.types.take(2).forEach { LabBadge(stringResource(it.labelRes()), MaterialTheme.colorScheme.tertiary) }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(90.dp)) }
                }
            }
        }
    }
}
