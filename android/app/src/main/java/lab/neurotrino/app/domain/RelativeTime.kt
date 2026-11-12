package lab.neurotrino.app.domain

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private val MONTHS_PL = listOf(
    "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
    "lipca", "sierpnia", "września", "października", "listopada", "grudnia",
)

/** Krótki, naturalny opis „jak dawno” po polsku. Nigdy nie ocenia — tylko informuje. */
fun relativeTimePl(iso: String, now: Instant = Instant.now()): String {
    val parsed = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
    val days = ChronoUnit.DAYS.between(parsed.atZone(ZoneId.systemDefault()).toLocalDate(), now.atZone(ZoneId.systemDefault()).toLocalDate())
    return when {
        days <= 0 -> "dziś"
        days == 1L -> "wczoraj"
        days < 7 -> "$days dni temu"
        days < 30 -> "${days / 7} tyg. temu"
        else -> {
            val date = parsed.atZone(ZoneId.systemDefault()).toLocalDate()
            "${date.dayOfMonth} ${MONTHS_PL[date.monthValue - 1]}"
        }
    }
}
