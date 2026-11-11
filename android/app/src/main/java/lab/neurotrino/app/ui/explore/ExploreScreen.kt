package lab.neurotrino.app.ui.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import lab.neurotrino.app.ui.theme.Amber

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
    val spark = current ?: SparksCatalog.pick(thoughts, reading, avoid)
    LabField {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Kicker(stringResource(R.string.explore_kicker))
            Text(stringResource(R.string.explore_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.explore_lede), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            CaptureWell {
                Kicker(
                    stringResource(
                        if (spark.generatedBy == "catalog") R.string.explore_from_catalog else R.string.explore_from_notes,
                    ),
                )
                Text(spark.title, style = MaterialTheme.typography.headlineMedium)
                Text(spark.hook)
                Text(spark.explanation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (deeper && spark.deeper.isNotBlank()) Text(spark.deeper)
                if (spark.why.isNotBlank()) Text(spark.why, color = Amber)
                Text("${stringResource(R.string.explore_question)}: ${spark.question}")
                if (spark.sourceNote.isNotBlank()) Text(spark.sourceNote, style = MaterialTheme.typography.labelMedium)
                Button(onClick = {
                    val linked = spark.linkedThoughtIds.firstOrNull()
                    if (linked != null) onOpenThought(linked) else onSave(spark)
                }) { Text(stringResource(R.string.action_save_as_thought)) }
                if (spark.deeper.isNotBlank()) {
                    OutlinedButton(onClick = { deeper = !deeper }) {
                        Text(stringResource(if (deeper) R.string.action_hide_mechanism else R.string.action_show_mechanism))
                    }
                }
                OutlinedButton(onClick = {
                    avoid = avoid + spark.id
                    current = SparksCatalog.pick(thoughts, reading, avoid + spark.id)
                    deeper = false
                }) { Text(stringResource(R.string.action_other_trail)) }
                Text(stringResource(R.string.explore_sources_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            Kicker(stringResource(R.string.explore_catalog))
            SparksCatalog.items.forEach { item ->
                InstrumentPanel(Modifier.padding(vertical = 6.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleLarge)
                    Text(item.hook, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { current = item; deeper = false }) { Text(stringResource(R.string.action_spark)) }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
