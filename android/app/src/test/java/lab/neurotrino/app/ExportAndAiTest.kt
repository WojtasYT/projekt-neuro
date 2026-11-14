package lab.neurotrino.app

import lab.neurotrino.app.data.export.parseExport
import lab.neurotrino.app.domain.EXPORT_FORMAT
import lab.neurotrino.app.domain.EXPORT_VERSION
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import org.junit.Assert.assertEquals
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
    fun acceptsLegacyV1ExportWithoutConversations() {
        val json = """
            {
              "format": "$EXPORT_FORMAT",
              "version": 1,
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
        assertEquals(1, parsed.version)
        assertTrue(parsed.conversations.isEmpty())
        assertTrue(parsed.messages.isEmpty())
    }

    @Test
    fun acceptsCurrentV2Export() {
        val json = """
            {
              "format": "$EXPORT_FORMAT",
              "version": $EXPORT_VERSION,
              "exportedAt": "2026-11-14T00:00:00Z",
              "thoughts": [],
              "projects": [],
              "relationships": [],
              "sources": [],
              "readingItems": [],
              "sparks": [],
              "conversations": [
                { "id": "cv1", "title": "Test", "sourceThoughtId": "th1", "mode": "impuls",
                  "archived": false, "createdAt": "2026-11-14T00:00:00Z", "updatedAt": "2026-11-14T00:00:00Z" }
              ],
              "messages": [
                { "id": "m1", "conversationId": "cv1", "role": "user", "content": "hej",
                  "provenance": "user", "createdAt": "2026-11-14T00:00:00Z" }
              ],
              "profile": {
                "id": "local", "displayName": "", "interests": [], "currentFocus": "",
                "onboarded": true, "theme": "dark", "aiDefaultMode": "impuls",
                "aiConfig": { "gatewayUrl": "" }, "remindersEnabled": false,
                "notesToSelf": "", "updatedAt": "2026-11-14T00:00:00Z"
              }
            }
        """.trimIndent()
        val parsed = parseExport(json)
        assertEquals(2, parsed.version)
        assertEquals(1, parsed.conversations.size)
        assertEquals(1, parsed.messages.size)
    }

    @Test
    fun rejectsMessagePointingAtUnknownConversation() {
        val json = """
            {
              "format": "$EXPORT_FORMAT",
              "version": $EXPORT_VERSION,
              "exportedAt": "2026-11-14T00:00:00Z",
              "thoughts": [], "projects": [], "relationships": [], "sources": [],
              "readingItems": [], "sparks": [],
              "conversations": [],
              "messages": [
                { "id": "m1", "conversationId": "duch", "role": "user", "content": "hej",
                  "provenance": "user", "createdAt": "2026-11-14T00:00:00Z" }
              ],
              "profile": {
                "id": "local", "displayName": "", "interests": [], "currentFocus": "",
                "onboarded": true, "theme": "dark", "aiDefaultMode": "impuls",
                "aiConfig": { "gatewayUrl": "" }, "remindersEnabled": false,
                "notesToSelf": "", "updatedAt": "2026-11-14T00:00:00Z"
              }
            }
        """.trimIndent()
        try {
            parseExport(json)
            throw AssertionError("expected rejection")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("rozmow"))
        }
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
    fun rejectsMixedVersionConversationsClaim() {
        // v1 nie może zawierać rozmów — ale parser toleruje brak pola (domyślna lista pusta)
        val json = """
            {
              "format": "$EXPORT_FORMAT",
              "version": 99,
              "exportedAt": "2026-11-14T00:00:00Z",
              "conversations": [], "messages": [],
              "profile": {
                "id": "local", "displayName": "", "interests": [], "currentFocus": "",
                "onboarded": true, "theme": "dark", "aiDefaultMode": "impuls",
                "aiConfig": {}, "remindersEnabled": false, "notesToSelf": "", "updatedAt": "2026-11-14T00:00:00Z"
              }
            }
        """.trimIndent()
        try {
            parseExport(json)
            throw AssertionError("expected rejection of unknown version")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("99"))
        }
    }
}
