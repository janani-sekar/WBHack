package org.wbhack.coach

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CoachApiException(message: String, val retryable: Boolean = false) : IOException(message)

data class Health(val model: String, val modelInstalled: Boolean)
data class AiTurn(val sessionId: String, val guestMessage: String)
data class Assessment(
    val turnId: String, val strength: String, val improvement: String, val evidence: String,
    val uncertain: Boolean, val scores: Map<String, Int>,
)

/** Maps the Android scenario fixtures onto the backend's skill identifiers. */
val SKILL_FOR_SCENARIO = mapOf(
    "time_request" to "duration",
    "directions" to "directions",
    "duration_availability" to "expectations",
)

val SCORE_KEYS = listOf("answers_request", "factual_accuracy", "clarifies_unknowns", "next_step")

object CoachApiParser {
    fun health(json: JSONObject) = Health(json.getString("model"), json.optBoolean("model_installed"))
    fun session(json: JSONObject) = AiTurn(json.getString("id"), json.getString("guest_message"))
    fun turn(json: JSONObject): Assessment {
        val a = json.getJSONObject("assessment")
        return Assessment(json.getString("id"), a.getString("strength_local"), a.getString("improvement_local"),
            a.getString("evidence_quote"), a.getBoolean("uncertain"), SCORE_KEYS.associateWith { a.getInt(it) })
    }
}

/** Client for the loopback-only desktop backend in `hospitality/server.py`. Blocking: call off the main thread. */
class CoachApi(private val baseUrl: String, private val readTimeoutMs: Int = 120_000) {
    fun health() = CoachApiParser.health(call("GET", "health"))

    fun ensureLanguage(language: String) {
        val profile = call("GET", "profile")
        if (profile.optString("language") == language) return
        call("POST", "profile", JSONObject().put("name", profile.getString("name"))
            .put("facts", profile.getJSONObject("facts")).put("language", language).put("approved", true))
    }

    fun start(scenarioId: String) = CoachApiParser.session(
        call("POST", "sessions", JSONObject().put("skill", SKILL_FOR_SCENARIO[scenarioId] ?: "duration")))

    fun respond(sessionId: String, response: String) = CoachApiParser.turn(
        call("POST", "sessions/$sessionId/responses", JSONObject().put("response", response)))

    fun next(sessionId: String) = CoachApiParser.session(call("POST", "sessions/$sessionId/next", JSONObject()))

    fun decide(sessionId: String, turnId: String, approved: Boolean) {
        call("POST", "sessions/$sessionId/assessment", JSONObject().put("turn_id", turnId).put("approved", approved))
    }

    fun remember(original: String, preferred: String, context: String) {
        call("POST", "memory", JSONObject().put("original", original.take(150)).put("preferred", preferred.take(150))
            .put("context", context).put("approved", true))
    }

    private fun call(method: String, path: String, body: JSONObject? = null): JSONObject {
        val conn = URL("${baseUrl.trimEnd('/')}/$path").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 3_000
            conn.readTimeout = readTimeoutMs
            if (body != null) {
                val bytes = body.toString().toByteArray()
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setFixedLengthStreamingMode(bytes.size)
                conn.outputStream.use { it.write(bytes) }
            }
            val code = conn.responseCode
            val text = (if (code < 400) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = if (text.isBlank()) JSONObject() else JSONObject(text)
            if (code >= 400) throw CoachApiException(json.optString("error", "HTTP $code"), json.optBoolean("retryable"))
            return json
        } finally {
            conn.disconnect()
        }
    }
}
