package lab.neurotrino.app.ui.thoughts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lab.neurotrino.app.R
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.ThoughtType
import lab.neurotrino.app.domain.searchThoughts
import lab.neurotrino.app.ui.components.InstrumentPanel
import lab.neurotrino.app.ui.components.Kicker
import lab.neurotrino.app.ui.components.LabField
import lab.neurotrino.app.ui.labelRes

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
            .filter { status == null || it.status == status }
            .filter { type == null || it.types.contains(type) }
            .sortedByDescending { it.updatedAt }
    }
    LabField {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Kicker(stringResource(R.string.thoughts_kicker))
            Text(stringResource(R.string.thoughts_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.thoughts_lede), color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                placeholder = { Text(stringResource(R.string.thoughts_search)) },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = archive, onClick = { archive = !archive }, label = { Text(stringResource(R.string.thoughts_show_archive)) })
            }
            if (filtered.isEmpty()) {
                InstrumentPanel(Modifier.padding(top = 12.dp)) {
                    Text(stringResource(R.string.thoughts_empty_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.thoughts_empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onCapture) { Text(stringResource(R.string.action_capture)) }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                    items(filtered, key = { it.id }) { thought ->
                        InstrumentPanel(Modifier.clickable { onOpen(thought.id) }) {
                            Text(thought.title.ifBlank { stringResource(R.string.thoughts_untitled) }, style = MaterialTheme.typography.titleLarge)
                            Text((thought.content.ifBlank { thought.rawText }).take(180), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (thought.isSample) AssistChip(onClick = {}, label = { Text(stringResource(R.string.sample_badge)) })
                                AssistChip(onClick = {}, label = { Text(stringResource(thought.status.labelRes())) })
                                thought.types.take(2).forEach { AssistChip(onClick = {}, label = { Text(stringResource(it.labelRes())) }) }
                            }
                        }
                    }
                    item { androidx.compose.foundation.layout.Spacer(Modifier.padding(40.dp)) }
                }
            }
        }
    }
}
