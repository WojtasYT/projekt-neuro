package lab.neurotrino.app

import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.lastResumable
import lab.neurotrino.app.domain.matchesQuery
import lab.neurotrino.app.domain.searchThoughts
import lab.neurotrino.app.domain.splitFragments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {
    private fun thought(id: String, text: String, title: String = "", status: ThoughtStatus = ThoughtStatus.seed) =
        Thought(
            id = id, title = title, rawText = text, content = text, types = emptyList(), status = status,
            tags = emptyList(), createdAt = "2024-01-01T00:00:00Z", updatedAt = "2024-01-01T00:00:00Z",
            lastOpenedAt = "2024-01-01T00:00:00Z", provenance = Provenance.user,
            projectIds = emptyList(), sourceIds = emptyList(), readingItemIds = emptyList(),
        )

    @Test
    fun diacriticsInsensitive() {
        val items = listOf(thought("1", "Opsyny w siatkówce", "Światło"))
        assertEquals(listOf("1"), searchThoughts(items, "siatkowce").map { it.id })
    }

    @Test
    fun emptyQueryReturnsAll() {
        val items = listOf(thought("1", "a"), thought("2", "b"))
        assertEquals(2, searchThoughts(items, "  ").size)
    }

    @Test
    fun normalizePolish() {
        assertTrue(matchesQuery("gęstość zdarzeń", "gestosc"))
    }

    @Test
    fun lastResumableSkipsArchive() {
        val items = listOf(
            thought("old", "a").copy(lastOpenedAt = "2024-01-01T00:00:00Z", status = ThoughtStatus.exploring),
            thought("new", "b").copy(lastOpenedAt = "2024-05-01T00:00:00Z", status = ThoughtStatus.paused),
            thought("arch", "c").copy(lastOpenedAt = "2024-06-01T00:00:00Z", status = ThoughtStatus.archived),
        )
        assertEquals("new", lastResumable(items)?.id)
    }

    @Test
    fun splitBlankLineFragments() {
        assertEquals(listOf("pierwsza", "druga", "trzecia"), splitFragments("pierwsza\n\ndruga\n\n\ntrzecia"))
    }
}
