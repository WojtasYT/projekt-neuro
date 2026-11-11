package lab.neurotrino.app.data.db

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val jsonPretty = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

val jsonSilent = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun encodeList(values: List<String>): String = jsonSilent.encodeToString(values)

fun decodeList(raw: String): List<String> =
    if (raw.isBlank()) emptyList() else runCatching { jsonSilent.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
