package lab.neurotrino.app.data.ai

import lab.neurotrino.app.domain.AiMode
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

class AiUnavailableException(message: String) : Exception(message)

@Serializable
data class AiPayload(
    val mode: String,
    val language: String = "pl",
    val constraints: Map<String, String>,
    val thought: Map<String, String>,
    val extra: String = "",
    val model: String? = null,
)

object AiGateway {
    fun configured(profile: UserProfile): Boolean = profile.gatewayUrl.isNotBlank()

    fun consentPreview(thought: Thought): String {
        val body = thought.content.ifBlank { thought.rawText }.trim()
        val clipped = if (body.length > 1200) body.take(1200) + "…" else body
        return listOf(thought.title, clipped).filter { it.isNotBlank() }.joinToString("\n\n")
    }

    suspend fun request(mode: AiMode, thought: Thought, profile: UserProfile): String =
        withContext(Dispatchers.IO) {
            val url = profile.gatewayUrl.trim()
            if (url.isEmpty()) throw AiUnavailableException("Asystent nie jest skonfigurowany.")
            val payload = Json.encodeToString(
                JsonObject(
                    mapOf(
                        "mode" to JsonPrimitive(mode.name),
                        "language" to JsonPrimitive("pl"),
                        "model" to JsonPrimitive(profile.model),
                        "thought" to JsonObject(
                            mapOf(
                                "id" to JsonPrimitive(thought.id),
                                "title" to JsonPrimitive(thought.title),
                                "rawText" to JsonPrimitive(thought.rawText),
                                "content" to JsonPrimitive(thought.content),
                            ),
                        ),
                    ),
                ),
            )
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 20000
                readTimeout = 45000
            }
            try {
                conn.outputStream.use { it.write(payload.toByteArray()) }
                if (conn.responseCode !in 200..299) {
                    throw AiUnavailableException("Serwer asystenta odpowiedział kodem ${conn.responseCode}.")
                }
                val body = conn.inputStream.bufferedReader().readText()
                extractText(body) ?: throw AiUnavailableException("Serwer nie zwrócił tekstu w oczekiwanym formacie.")
            } catch (e: AiUnavailableException) {
                throw e
            } catch (_: Exception) {
                throw AiUnavailableException("Nie udało się połączyć z serwerem asystenta.")
            } finally {
                conn.disconnect()
            }
        }

    fun extractText(body: String): String? {
        val element = runCatching { Json.parseToJsonElement(body) }.getOrNull() ?: return null
        return readText(element)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun readText(element: JsonElement): String? {
        val obj = element as? JsonObject ?: return null
        obj["text"]?.jsonPrimitive?.contentOrNull?.let { return it }
        obj["content"]?.jsonPrimitive?.contentOrNull?.let { return it }
        val choices = obj["choices"]?.jsonArray
        val first = choices?.firstOrNull() as? JsonObject
        first?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull?.let { return it }
        first?.get("text")?.jsonPrimitive?.contentOrNull?.let { return it }
        return null
    }
}
