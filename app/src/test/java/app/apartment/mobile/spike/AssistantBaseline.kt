package app.apartment.mobile.spike

import java.text.Normalizer

/**
 * THROWAWAY SPIKE (OI-049, DOC-TFA-001). Rule-based baseline "option A": turns a Spanish phrase into an
 * intent plus slots with keyword rules, no model. It exists only to measure how far rules go on the
 * phrase set (docs/06-quality/assistant-phrase-set.psv). Not product code; the Owner decides whether it stays.
 */
data class Parsed(val intent: String, val slots: Set<String>)

object AssistantBaseline {
    private val weekdays = mapOf(
        "lunes" to "monday", "martes" to "tuesday", "miercoles" to "wednesday", "jueves" to "thursday",
        "viernes" to "friday", "sabado" to "saturday", "domingo" to "sunday",
    )
    private val numberWords = mapOf(
        "cero" to 0, "uno" to 1, "un" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4, "cinco" to 5, "seis" to 6,
        "siete" to 7, "ocho" to 8, "nueve" to 9, "diez" to 10, "once" to 11, "doce" to 12, "trece" to 13,
        "catorce" to 14, "quince" to 15, "dieciseis" to 16, "diecisiete" to 17, "dieciocho" to 18,
        "diecinueve" to 19, "veinte" to 20, "veintiuno" to 21, "veintidos" to 22, "veintitres" to 23,
        "veinticuatro" to 24, "veinticinco" to 25, "veintiseis" to 26, "veintisiete" to 27, "veintiocho" to 28,
        "veintinueve" to 29, "treinta" to 30, "cuarenta" to 40, "cincuenta" to 50, "sesenta" to 60,
        "setenta" to 70, "ochenta" to 80, "noventa" to 90, "cien" to 100, "ciento" to 100,
        "doscientos" to 200, "trescientos" to 300, "cuatrocientos" to 400, "quinientos" to 500,
        "seiscientos" to 600, "setecientos" to 700, "ochocientos" to 800, "novecientos" to 900,
    )

    fun normalize(text: String): String {
        val noAccents = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
        val cleaned = noAccents.replace("₡", " ").replace(Regex("[¿?¡!,.;:]"), " ")
        var s = cleaned.replace(Regex("\\s+"), " ").trim()
        s = s.replace(Regex("\\b(e+h+|m+m+|e+m+)\\b"), " ").replace(Regex("\\s+"), " ").trim()
        s = Regex("(\\d{1,3}) (\\d{3})\\b").replace(s) { it.groupValues[1] + it.groupValues[2] }
        return s
    }

    /** First money amount of 1000 or more: digits, "300 mil" or number words. */
    private fun amount(t: String): Int? {
        Regex("\\b(\\d+)( mil)?\\b").findAll(t).forEach { m ->
            var v = m.groupValues[1].toLong()
            if (m.groupValues[2].isNotEmpty() && v < 1000) v *= 1000
            if (v >= 1000) return v.toInt()
        }
        val tokens = t.split(" ")
        var i = 0
        while (i < tokens.size) {
            if (tokens[i] in numberWords) {
                var total = 0
                var cur = 0
                var j = i
                var sawMil = false
                while (j < tokens.size && (tokens[j] in numberWords || tokens[j] == "mil" || tokens[j] == "y")) {
                    val w = tokens[j]
                    when {
                        w == "mil" -> { total += (if (cur == 0) 1 else cur) * 1000; cur = 0; sawMil = true }
                        w == "y" -> {}
                        else -> cur += numberWords.getValue(w)
                    }
                    j++
                }
                total += cur
                if (sawMil && total >= 1000) return total
                i = j
            } else i++
        }
        return null
    }

    /** Day of month 1..31 (last one named, so "al 20 no al 22" gives 22), or "month_end". */
    private fun dayOfMonth(t: String): String? {
        if (Regex("ultimo dia|fin de mes").containsMatchIn(t)) return "month_end"
        var found: Int? = null
        t.split(" ").forEach { w ->
            val d = w.toIntOrNull() ?: numberWords[w]
            if (d != null && d in 1..31) found = d
        }
        return found?.toString()
    }

    /** Data that in the app comes from the local database: tenant names and property names. Never hard-coded in rules. */
    class Roster(val tenantNames: List<String>, val propertyNames: List<String>) {
        val byToken: Map<String, String> = tenantNames.flatMap { n -> normalize(n).split(" ").map { it to n.split(" ").first() } }.toMap()
    }

    private fun tenantOf(t: String, roster: Roster): String? = t.split(" ").firstNotNullOfOrNull { roster.byToken[it] }

    /** A property mentioned by its name, e.g. "apartamento 3": returns the last word of the stored name ("3"). */
    private fun propertyOf(t: String, roster: Roster): String? =
        roster.propertyNames.firstOrNull { Regex("\\b" + Regex.escape(normalize(it)) + "\\b").containsMatchIn(t) }?.split(" ")?.last()

    fun parse(text: String, roster: Roster): Parsed {
        val t0 = normalize(text)
        val prop = propertyOf(t0, roster)
        val pn = prop?.let { "property=$it" }
        val t = roster.propertyNames.fold(t0) { acc, p -> acc.replace(normalize(p), " ") }.replace(Regex("\\s+"), " ").trim()
        val tenant = tenantOf(t, roster)
        val hasRef = tenant != null || prop != null
        fun slots(vararg s: String?) = s.filterNotNull().toSet()
        val tn = tenant?.let { "tenant=$it" }

        if (t.startsWith("cancel") || t.startsWith("no dejalo")) return Parsed("cancel", emptySet())
        if (t.startsWith("si confirm") || t == "confirmalo") return Parsed("confirm", emptySet())

        if (t.contains("desmarc") || Regex("\\bno (ha )?pago\\b").containsMatchIn(t)) return Parsed("unmark_paid", slots(tn, pn))
        if (hasRef && (t.contains("pagado") || t.contains("pagada") ||
                Regex("\\b(ya pago|me pago|anote que \\w+ pago)\\b").containsMatchIn(t))
        ) return Parsed("mark_paid", slots(tn, pn))

        val deleteVerb = Regex("\\b(borra|borrar|borre)\\b").containsMatchIn(t)
        val endVerb = Regex("\\b(termina|terminar|quita|quitalo|saca|se va|se fue|elimina|elimine|eliminar)\\b|de baja").containsMatchIn(t)
        if (deleteVerb || endVerb) {
            val intent = if (deleteVerb) "delete_rental" else "end_rental"
            if (!hasRef) {
                val m = Regex("\\b(quita|elimina|borra|termina|saca|baja) a (\\w+)").find(t)
                if (m != null) return Parsed("unknown_tenant", slots("tenant=" + m.groupValues[2].replaceFirstChar { it.uppercase() }))
            } else {
                val end = if (intent == "end_rental" && t.contains("fin de mes")) "end=month_end" else null
                return Parsed(intent, slots(tn, pn, end))
            }
        }

        val changeVerb = Regex("\\b(sube|suba|subale|baja|aumenta|cambia|cambiale|cambiele|mueve|pasa)\\b").containsMatchIn(t)
        val questionWord = Regex("\\b(cuanto|cuando|que dia|desde cuando|cual es)\\b|monto de").containsMatchIn(t)
        if (hasRef && questionWord && !changeVerb) {
            val field = when {
                t.contains("desde cuando") -> "start_date"
                (t.contains("cuanto") || t.contains("monto") || t.contains("alquiler")) && (t.contains("que dia") || t.contains("cuando")) -> "amount+day"
                t.contains("cuanto") || t.contains("monto") || t.contains("alquiler") -> "amount"
                else -> "day"
            }
            return Parsed("tenant_info", slots(tn, pn, "field=$field"))
        }

        val money = amount(t)
        if (hasRef && money != null) {
            val relative = Regex("\\b(aumenta|sube)\\b").containsMatchIn(t) && !Regex("\\ba $money|\\ba \\d+ mil").containsMatchIn(t) &&
                !Regex("\\ba [a-z ]*(mil)\\b").containsMatchIn(t.substringAfter(normalize(tenant ?: "")))
            return Parsed("change_amount", slots(tn, pn, if (relative) "delta=$money" else "amount=$money"))
        }
        val payDay = Regex("\\bpagu?e el\\b|\\bpaga el\\b|\\bpagar el\\b").containsMatchIn(t)
        if (hasRef && (changeVerb || payDay)) {
            val day = dayOfMonth(t)
            return if (day != null) Parsed("change_day", slots(tn, pn, "day=$day")) else Parsed("clarify", slots(tn, pn, "missing=day"))
        }
        if (!hasRef && changeVerb && t.contains("alquiler")) return Parsed("clarify", slots("missing=tenant", "missing=amount"))

        if (hasRef && t.contains("llam")) return Parsed("call", slots(tn, pn))
        if (hasRef && Regex("whatsapp|wasap|mensaje").containsMatchIn(t)) return Parsed("whatsapp", slots(tn, pn))

        val collection = Regex("cobr|paga|vence|toca|recib|entra|falta|hay algo|total").containsMatchIn(t)
        if (!collection) return Parsed("out_of_scope", emptySet())
        val range = when {
            t.contains("proxima semana") || t.contains("otra semana") || t.contains("siguiente semana") -> "next_week"
            t.contains("este mes") || t.contains("del mes") -> "this_month"
            t.contains("semana") -> "this_week"
            else -> null
        }
        if (Regex("\\bcuanto\\b").containsMatchIn(t) || t.contains("total")) {
            val pending = if (t.contains("falta")) "only=pending" else null
            return Parsed("month_total", slots("range=" + (range ?: "this_month"), pending))
        }
        if (t.contains("pasado manana")) return Parsed("date_collections", slots("range=day_after_tomorrow"))
        if (Regex("\\bhoy\\b").containsMatchIn(t)) return Parsed("week_collections", slots("range=today"))
        if (Regex("\\bmanana\\b").containsMatchIn(t)) return Parsed("week_collections", slots("range=tomorrow"))
        t.split(" ").firstNotNullOfOrNull { weekdays[it] }?.let { return Parsed("date_collections", slots("weekday=$it")) }
        if (t.contains("fin de mes")) return Parsed("date_collections", slots("range=month_end"))
        Regex("\\b(el|dia) (\\w+)\\b").findAll(t).forEach { m ->
            val w = m.groupValues[2]
            val d = w.toIntOrNull() ?: numberWords[w]
            if (d != null && d in 1..31) return Parsed("date_collections", slots("day=$d"))
        }
        if (range != null) return Parsed("week_collections", slots("range=$range"))
        return Parsed("out_of_scope", emptySet())
    }
}
