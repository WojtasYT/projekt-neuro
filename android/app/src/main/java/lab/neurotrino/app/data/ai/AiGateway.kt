package lab.neurotrino.app.data.ai

import lab.neurotrino.app.domain.AssistantContextItem
import lab.neurotrino.app.domain.AssistantMode
import lab.neurotrino.app.domain.UserProfile
import lab.neurotrino.app.domain.normalizeGatewayBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/** Błąd bramki zgodny z AI_GATEWAY_CONTRACT: {code,message,retryable}. */
class AiGatewayException(val code: String, message: String, val retryable: Boolean) : Exception(message)

@Serializable
data class HealthProviderDto(
    val configured: Boolean = false,
    val reachable: Boolean = false,
    val detail: String = "",
)

@Serializable
data class HealthResponse(
    val status: String = "",
    val service: String = "",
    val version: String = "",
    val provider: HealthProviderDto = HealthProviderDto(),
)

@Serializable
data class StatusResponse(
    val configured: Boolean = false,
    val reachable: Boolean = false,
    val provider: String = "",
    val model: String = "",
    val detail: String = "",
)

@Serializable
data class ModelsResponse(val models: List<String> = emptyList())

@Serializable
private data class ContextItemDto(val kind: String, val id: String, val title: String, val text: String)

@Serializable
private data class ContextDto(val currentThought: String, val items: List<ContextItemDto>)

@Serializable
private data class ChatRequestDto(
    val conversationId: String? = null,
    val mode: String,
    val model: String? = null,
    val messages: List<MessageDto>,
    val context: ContextDto,
    val parameters: ParametersDto = ParametersDto(),
)

@Serializable
private data class MessageDto(val role: String, val content: String)

@Serializable
private data class PreviewRequestDto(
    val mode: String,
    val model: String? = null,
    val messages: List<MessageDto>,
    val context: ContextDto,
)

@Serializable
private data class ParametersDto(val temperature: Double = 0.4, val maxOutputTokens: Int = 800)

@Serializable
data class ChatResponseDto(
    val conversationId: String? = null,
    val messageId: String? = null,
    val role: String = "assistant",
    val content: String = "",
    val model: String? = null,
)

@Serializable
data class PreviewMessageDto(val role: String, val content: String)

@Serializable
data class ContextPreviewDto(
    val mode: String = "",
    val model: String? = null,
    val contextChars: Int = 0,
    val messages: List<PreviewMessageDto> = emptyList(),
)

@Serializable
private data class ErrorEnvelope(val error: ErrorDto? = null)

@Serializable
private data class ErrorDto(
    val code: String = "INTERNAL_ERROR",
    val message: String = "",
    val retryable: Boolean = false,
)

/** Wynik realnego sprawdzenia bramki (nigdy statyczny tekst). */
data class AiHealth(
    val configured: Boolean,
    val reachable: Boolean,
    val model: String,
    val detail: String,
)

private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

private fun contextDto(currentThought: String, items: List<AssistantContextItem>) =
    ContextDto(currentThought, items.map { ContextItemDto(it.kind, it.id, it.title, it.text) })

private fun friendlyForCode(code: String): String = when (code) {
    "INVALID_REQUEST" -> "Żądanie zostało odrzucone przez bramkę."
    "CONTEXT_TOO_LARGE" -> "Kontekst jest zbyt duży. Usuń część elementów z kontekstu."
    "RATE_LIMITED" -> "Limit żądań — odczekaj chwilę i spróbuj ponownie."
    "PROVIDER_AUTH_FAILED" -> "Klucz dostawcy odrzucony. Sprawdź AI_API_KEY na serwerze bramki."
    "PROVIDER_UNAVAILABLE" -> "Dostawca modelu jest niedostępny albo bramka nie jest skonfigurowana."
    "MODEL_NOT_FOUND" -> "Model nie istnieje u dostawcy. Sprawdź nazwę modelu."
    "TIMEOUT" -> "Dostawca nie odpowiedział na czas. Spróbuj ponownie."
    else -> "Nieoczekiwany błąd bramki."
}

object AiGateway {

    fun configured(profile: UserProfile): Boolean = profile.gatewayUrl.isNotBlank()

    /** Realne GET {base}/health. Nigdy nie rzuca — zwraca stan z opisem (PL). */
    suspend fun health(profile: UserProfile): AiHealth = withContext(Dispatchers.IO) {
        val base = normalizeGatewayBase(profile.gatewayUrl)
        if (base.isEmpty()) {
            return@withContext AiHealth(false, false, profile.model, "Asystent nie jest skonfigurowany — uzupełnij adres bramki w Ustawieniach.")
        }
        runCatching {
            val res = httpCall("GET", "$base/health", null)
            if (res.code !in 200..299) {
                return@withContext AiHealth(true, false, profile.model, "Bramka odpowiedziała kodem ${res.code}.")
            }
            val parsed = json.decodeFromString<HealthResponse>(res.body)
            AiHealth(
                configured = parsed.provider.configured,
                reachable = parsed.provider.reachable,
                model = profile.model,
                detail = parsed.provider.detail.ifBlank { if (parsed.provider.reachable) "Bramka i dostawca odpowiadają." else "Dostawca nieosiągalny." },
            )
        }.getOrElse { AiHealth(true, false, profile.model, "Nie udało się połączyć z bramką (${it.message ?: "błąd sieci"}).") }
    }

    /** GET {base}/v1/models — lista modeli dostawcy (do wyboru w Ustawieniach). */
    suspend fun models(profile: UserProfile): List<String> = withContext(Dispatchers.IO) {
        val base = normalizeGatewayBase(profile.gatewayUrl)
        require(base.isNotEmpty()) { "Asystent nie jest skonfigurowany." }
        val res = httpCall("GET", "$base/v1/models", null)
        if (res.code !in 200..299) throw errorFrom(res)
        runCatching { json.decodeFromString<ModelsResponse>(res.body).models }.getOrDefault(emptyList())
    }

    /** POST {base}/v1/chat/completions — pełna rozmowa z kontekstem. */
    suspend fun sendMessage(
        profile: UserProfile,
        conversationId: String?,
        mode: AssistantMode,
        messages: List<Pair<String, String>>, // (role, content)
        currentThought: String,
        contextItems: List<AssistantContextItem>,
    ): ChatResponseDto = withContext(Dispatchers.IO) {
        val base = normalizeGatewayBase(profile.gatewayUrl)
        require(base.isNotEmpty()) { "Asystent nie jest skonfigurowany." }
        val payload = ChatRequestDto(
            conversationId = conversationId,
            mode = mode.name,
            model = profile.model.takeIf { it.isNotBlank() },
            messages = messages.map { MessageDto(it.first, it.second) },
            context = contextDto(currentThought, contextItems),
        )
        val body = json.encodeToString(payload)
        val res = httpCall("POST", "$base/v1/chat/completions", body, readTimeoutMs = 45_000)
        if (res.code !in 200..299) throw errorFrom(res)
        runCatching { json.decodeFromString<ChatResponseDto>(res.body) }
            .getOrElse { throw AiGatewayException("INTERNAL_ERROR", "Bramka zwróciła nieoczekiwany kształt odpowiedzi.", false) }
    }

    /** POST {base}/v1/context/preview — pokaż dokładnie, co pójdzie do modelu. */
    suspend fun previewContext(
        profile: UserProfile,
        mode: AssistantMode,
        messages: List<Pair<String, String>>,
        currentThought: String,
        contextItems: List<AssistantContextItem>,
    ): ContextPreviewDto = withContext(Dispatchers.IO) {
        val base = normalizeGatewayBase(profile.gatewayUrl)
        require(base.isNotEmpty()) { "Asystent nie jest skonfigurowany." }
        val payload = PreviewRequestDto(
            mode = mode.name,
            model = profile.model.takeIf { it.isNotBlank() },
            messages = messages.map { MessageDto(it.first, it.second) },
            context = contextDto(currentThought, contextItems),
        )
        val body = json.encodeToString(payload)
        val res = httpCall("POST", "$base/v1/context/preview", body)
        if (res.code !in 200..299) throw errorFrom(res)
        runCatching { json.decodeFromString<ContextPreviewDto>(res.body) }
            .getOrElse { throw AiGatewayException("INTERNAL_ERROR", "Bramka zwróciła nieoczekiwany podgląd kontekstu.", false) }
    }

    private fun errorFrom(res: HttpResult): AiGatewayException {
        val envelope = runCatching { json.decodeFromString<ErrorEnvelope>(res.body) }.getOrNull()
        val err = envelope?.error
        if (err != null && err.code.isNotBlank()) {
            val msg = err.message.ifBlank { friendlyForCode(err.code) }
            return AiGatewayException(err.code, msg, err.retryable)
        }
        val code = when (res.code) {
            400 -> "INVALID_REQUEST"; 413 -> "CONTEXT_TOO_LARGE"; 429 -> "RATE_LIMITED"
            404 -> "MODEL_NOT_FOUND"; 504 -> "TIMEOUT"; 503 -> "PROVIDER_UNAVAILABLE"
            else -> "INTERNAL_ERROR"
        }
        return AiGatewayException(code, friendlyForCode(code), res.code == 429 || res.code >= 500)
    }

    private data class HttpResult(val code: Int, val body: String)

    private fun httpCall(method: String, urlString: String, body: String?, connectTimeoutMs: Int = 10_000, readTimeoutMs: Int = 20_000): HttpResult {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (body != null) conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.readText().orEmpty()
            return HttpResult(code, text)
        } catch (e: java.net.SocketTimeoutException) {
            throw AiGatewayException("TIMEOUT", friendlyForCode("TIMEOUT"), true)
        } catch (e: java.io.IOException) {
            throw AiGatewayException("PROVIDER_UNAVAILABLE", "Nie udało się połączyć z bramką (${e.message ?: "błąd sieci"}).", true)
        } finally {
            conn.disconnect()
        }
    }
}
