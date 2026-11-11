package lab.neurotrino.app

import lab.neurotrino.app.data.ai.AiGateway
import lab.neurotrino.app.data.export.parseExport
import lab.neurotrino.app.domain.EXPORT_FORMAT
import lab.neurotrino.app.domain.EXPORT_VERSION
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportAndAiTest {
    @Test
    fun rejectsForeignJson() {
        try {
            parseExport("""{"hello":true}""")
            throw AssertionError("expected failure")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("neurotrino") || e.message!!.contains("zapis"))
        }
    }

    @Test
    fun acceptsValidPwaShapedExport() {
        val json = """
            {
              "format": "$EXPORT_FORMAT",
              "version": $EXPORT_VERSION,
              "exportedAt": "2024-05-01T00:00:00Z",
              "thoughts": [],
              "projects": [],
              "relationships": [],
              "sources": [],
              "readingItems": [],
              "sparks": [],
              "profile": {
                "id": "local",
                "displayName": "",
                "interests": [],
                "currentFocus": "",
                "onboarded": true,
                "theme": "dark",
                "aiDefaultMode": "iskra",
                "aiConfig": { "gatewayUrl": "" },
                "remindersEnabled": false,
                "notesToSelf": "",
                "updatedAt": "2024-05-01T00:00:00Z"
              }
            }
        """.trimIndent()
        val parsed = parseExport(json)
        assertEquals(EXPORT_FORMAT, parsed.format)
        assertTrue(parsed.thoughts.isEmpty())
    }

    @Test
    fun rawTextIsSeparateField() {
        val thought = Thought(
            id = "th", title = "t", rawText = "oryginał", content = "późniejsza edycja",
            types = emptyList(), status = ThoughtStatus.seed, tags = emptyList(),
            createdAt = "a", updatedAt = "a", lastOpenedAt = "a", provenance = Provenance.user,
            projectIds = emptyList(), sourceIds = emptyList(), readingItemIds = emptyList(),
        )
        assertEquals("oryginał", thought.rawText)
        assertTrue(thought.content != thought.rawText)
    }

    @Test
    fun aiExtractsGatewayText() {
        assertEquals("Jedno pytanie", AiGateway.extractText("""{"text":"Jedno pytanie"}"""))
        assertEquals("treść", AiGateway.extractText("""{"choices":[{"message":{"content":" treść "}}]}"""))
        assertNull(AiGateway.extractText("""{"ok":true}"""))
    }
}
