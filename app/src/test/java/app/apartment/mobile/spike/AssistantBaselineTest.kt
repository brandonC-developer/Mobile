package app.apartment.mobile.spike

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** THROWAWAY SPIKE (OI-049). Measures the rule baseline on the phrase set and writes app/build/assistant-baseline.txt. */
class AssistantBaselineTest {
    private class Row(val id: String, val text: String, val intent: String, val slots: Set<String>, val split: String)

    private val writes = setOf("mark_paid", "unmark_paid", "change_day", "change_amount", "end_rental", "delete_rental")

    private fun load(): List<Row> {
        val f = listOf(File("../docs/06-quality/assistant-phrase-set.psv"), File("docs/06-quality/assistant-phrase-set.psv")).first { it.exists() }
        return f.readLines().drop(1).filter { it.isNotBlank() }.map {
            val p = it.split("|")
            Row(p[0], p[1], p[2], p[3].split(",").filter(String::isNotBlank).toSet(), p[5])
        }
    }

    @Test
    fun measureTheBaseline() {
        val rows = load()
        val out = StringBuilder()
        for (split in listOf("test", "dev")) {
            val set = rows.filter { it.split == split }
            var intentOk = 0; var exact = 0; var harmful = 0; var slotTp = 0; var slotExp = 0; var slotPred = 0
            val failures = mutableListOf<String>()
            val perIntent = sortedMapOf<String, IntArray>()
            for (r in set) {
                val got = AssistantBaseline.parse(r.text)
                val iOk = got.intent == r.intent
                val sOk = got.slots == r.slots
                if (iOk) intentOk++
                if (iOk && sOk) exact++
                if (iOk) { slotTp += got.slots.intersect(r.slots).size; slotExp += r.slots.size; slotPred += got.slots.size }
                else { slotExp += r.slots.size; slotPred += got.slots.size }
                if (got.intent in writes && !(iOk && sOk)) harmful++
                perIntent.getOrPut(r.intent) { IntArray(2) }.also { it[0]++; if (iOk && sOk) it[1]++ }
                if (!(iOk && sOk)) failures += "${r.id} \"${r.text}\"  expected ${r.intent} ${r.slots.sorted()}  got ${got.intent} ${got.slots.sorted()}" + if (got.intent in writes) "  [HARMFUL]" else ""
            }
            val prec = if (slotPred == 0) 0.0 else slotTp.toDouble() / slotPred
            val rec = if (slotExp == 0) 0.0 else slotTp.toDouble() / slotExp
            val f1 = if (prec + rec == 0.0) 0.0 else 2 * prec * rec / (prec + rec)
            out.appendLine("== split=$split  n=${set.size}")
            out.appendLine("intent accuracy: %.1f%% (%d/%d)".format(100.0 * intentOk / set.size, intentOk, set.size))
            out.appendLine("exact match (intent+slots): %.1f%% (%d/%d)".format(100.0 * exact / set.size, exact, set.size))
            out.appendLine("slot F1: %.1f%%".format(100 * f1))
            out.appendLine("harmful errors (a write predicted wrongly): $harmful")
            out.appendLine("per intent exact (ok/total):")
            perIntent.forEach { (k, v) -> out.appendLine("  %-18s %d/%d".format(k, v[1], v[0])) }
            out.appendLine("failures:")
            failures.forEach { out.appendLine("  $it") }
            out.appendLine()
        }
        File("build/assistant-baseline.txt").writeText(out.toString())
        println(out)
        assertTrue(rows.size == 90)
    }
}
