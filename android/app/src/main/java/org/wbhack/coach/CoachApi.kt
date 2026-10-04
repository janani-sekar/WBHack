package org.wbhack.coach

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CoachApiException(message: String, val retryable: Boolean = false) : IOException(message)

val SCORE_KEYS = listOf("answers_request", "factual_accuracy", "clarifies_unknowns", "next_step", "customer_tone")

data class Health(val model: String, val modelInstalled: Boolean)
data class Fact(val key: String, val value: String)
data class Case(
    val id: String, val skill: String, val difficulty: Int, val title: String, val guest: String,
    val variants: Map<String, Map<String, String>>, val facts: List<Fact>, val goals: List<String>,
) {
    fun guestFor(language: String, style: String) = variants[language]?.get(style) ?: guest
}
data class Assessment(
    val strength: String, val improvement: String, val evidence: String,
    val uncertain: Boolean, val scores: Map<String, Int>,
)
data class Turn(
    val id: String, val guestMessage: String, val response: String, val status: String, val issue: String?,
    val questionIndex: Int, val assessment: Assessment, val variants: Map<String, Pair<String, String>>,
) {
    /** Strength and improvement in the requested coaching language, falling back to the original. */
    fun feedback(language: String) = variants[language] ?: (assessment.strength to assessment.improvement)
}
data class Recap(val replyCount: Int, val earlyFinish: Boolean, val means: Map<String, Double>, val focus: String?)
data class LiveSession(
    val id: String, val skill: String, val scenarioId: String?, val lessonId: String?, val case: Case?,
    val guestMessage: String, val guestLanguage: String, val coachLanguage: String, val independent: Boolean,
    val questionIndex: Int, val completed: Boolean, val turns: List<Turn>, val recap: Recap?,
    val reviewQuote: String?, val reviewFocus: String?, val observationFocus: String?,
) {
    /** True while the current guest message has no reply yet. */
    val awaitingReply get() = !completed && (turns.isEmpty() || turns.last().questionIndex != questionIndex)
}
data class SkillPlan(
    val skill: String, val acceptedSessions: Int, val independentCases: Int, val state: String,
    val weakest: String, val means: Map<String, Double>,
)
data class Plan(val skills: List<SkillPlan>, val recommendedId: String, val recommendedTitle: String)
data class LearningSettings(val guestLanguage: String, val style: String, val coachLanguage: String, val support: String)
data class MemorySource(val sessionId: String, val turnId: String, val quote: String)
data class Observation(val skill: String, val focus: String?, val acceptedSessions: Int, val sources: List<MemorySource>)
data class LearnerMemory(val preferences: LearningSettings, val observations: List<Observation>)
data class ReviewText(val original: String, val translation: String)
data class ReviewFinding(
    val id: String, val topic: String, val action: String, val explanation: String, val quotes: List<String>,
    val reviewCount: Int, val canPractice: Boolean, val uncertain: Boolean,
)
data class ReviewBatch(val id: String, val language: String, val reviews: List<ReviewText>,
    val findings: List<ReviewFinding>, val createdAt: String)
data class Lesson(val id: String, val batchId: String?, val groupId: String?, val topic: String?, val skill: String,
    val scenarioId: String?, val evidence: String?, val reason: String)
data class Draft(val id: String, val text: String)

private fun JSONObject.str(key: String): String? = if (has(key) && !isNull(key)) getString(key) else null
private fun JSONObject.numbers(): Map<String, Double> = keys().asSequence().associateWith { getDouble(it) }
private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
private fun factValue(value: Any): String =
    if (value is JSONArray) (0 until value.length()).joinToString(", ") { value.get(it).toString() } else value.toString()

object CoachApiParser {
    fun health(json: JSONObject) = Health(json.getString("model"), json.optBoolean("model_installed"))

    fun case(j: JSONObject) = Case(
        j.getString("id"), j.getString("skill"), j.optInt("difficulty", 1), j.getString("title"), j.getString("guest"),
        j.optJSONObject("guest_variants")?.let { all ->
            all.keys().asSequence().associateWith { lang ->
                all.getJSONObject(lang).let { styles -> styles.keys().asSequence().associateWith { styles.getString(it) } }
            }
        } ?: emptyMap(),
        j.optJSONObject("facts")?.let { f -> f.keys().asSequence().map { Fact(it, factValue(f.get(it))) }.toList() } ?: emptyList(),
        j.optString("goal").split(';').map { it.trim() }.filter { it.isNotEmpty() },
    )

    fun assessment(a: JSONObject) = Assessment(
        a.optString("strength_local"), a.optString("improvement_local"), a.optString("evidence_quote"),
        a.optBoolean("uncertain"), SCORE_KEYS.filter(a::has).associateWith { a.getInt(it) },
    )

    fun turn(j: JSONObject) = Turn(
        j.getString("id"), j.optString("guest_message"), j.optString("response"), j.optString("status", "pending"),
        j.str("assessment_issue"), j.optInt("question_index"), assessment(j.getJSONObject("assessment")),
        j.optJSONObject("feedback_variants")?.let { v ->
            v.keys().asSequence().associateWith { lang ->
                v.getJSONObject(lang).let { it.optString("strength_local") to it.optString("improvement_local") }
            }
        } ?: emptyMap(),
    )

    fun session(j: JSONObject): LiveSession {
        val personal = j.optJSONObject("personalization")
        return LiveSession(
            j.getString("id"), j.optString("skill"), j.str("scenario_id"), j.str("lesson_id"),
            j.optJSONObject("scenario")?.let(::case), j.getString("guest_message"),
            j.optString("guest_language", "en"), j.optString("coach_language", "en"), j.optBoolean("independent"),
            j.optInt("question_index"), j.optBoolean("completed"),
            j.optJSONArray("turns")?.objects()?.map(::turn) ?: emptyList(),
            j.optJSONObject("summary")?.let {
                Recap(it.optInt("reply_count"), it.optBoolean("early_finish"),
                    it.optJSONObject("criterion_means")?.numbers() ?: emptyMap(), it.str("focus"))
            },
            personal?.optJSONArray("review_sources")?.objects()?.firstOrNull()?.str("quote"),
            personal?.str("review_focus"), personal?.optJSONObject("observation")?.str("practice_focus"),
        )
    }

    fun plan(j: JSONObject) = Plan(
        j.getJSONArray("skills").objects().map {
            SkillPlan(it.getString("skill"), it.optInt("accepted_sessions"), it.optInt("independent_cases"),
                it.optString("state"), it.optString("weakest_dimension"),
                it.optJSONObject("criterion_means")?.numbers() ?: emptyMap())
        },
        j.getString("recommended_scenario_id"), j.optString("recommended_title"),
    )

    fun settings(j: JSONObject) = LearningSettings(j.optString("guest_language", "en"), j.optString("style", "chat"),
        j.optString("coach_language", "en"), j.optString("support", "brief"))

    fun memory(j: JSONObject) = LearnerMemory(
        settings(j.getJSONObject("preferences")),
        j.optJSONArray("skill_observations")?.objects()?.map { o ->
            Observation(o.getString("skill"), o.str("practice_focus"), o.optInt("accepted_sessions"),
                o.optJSONArray("sources")?.objects()?.map {
                    MemorySource(it.getString("session_id"), it.getString("turn_id"), it.optString("quote"))
                } ?: emptyList())
        } ?: emptyList(),
    )

    fun batch(j: JSONObject) = ReviewBatch(
        j.getString("id"), j.optString("language", "en"),
        j.getJSONArray("reviews").objects().map {
            ReviewText(it.getString("original"), it.optJSONObject("analysis")?.optString("translation_local").orEmpty())
        },
        j.getJSONArray("groups").objects().map { g ->
            val sources = g.getJSONArray("sources").objects()
            ReviewFinding(g.getString("id"), g.optString("topic"), g.optString("action"),
                sources.firstOrNull()?.optString("explanation_local").orEmpty(), sources.map { it.optString("quote") },
                g.optInt("review_count", sources.size), g.optBoolean("can_practice"), g.optBoolean("uncertain"))
        },
        j.optString("created_at"),
    )

    fun lesson(j: JSONObject) = Lesson(j.getString("id"), j.str("batch_id"), j.str("group_id"), j.str("topic"),
        j.getString("skill"), j.str("scenario_id"), j.str("evidence_quote"), j.optString("reason_local"))

    fun draft(j: JSONObject) = Draft(j.getString("id"), j.optString("text"))
}

/** Client for the loopback-only desktop backend in `hospitality/server.py`. Blocking: call off the main thread. */
class CoachApi(private val baseUrl: String, private val readTimeoutMs: Int = 300_000) {
    private val p = CoachApiParser

    fun health() = p.health(call("GET", "health"))
    fun curriculum() = callArray("GET", "curriculum").objects().map(p::case)
    fun plan() = p.plan(call("GET", "learning-plan"))
    fun memory() = p.memory(call("GET", "learner-memory"))
    fun settings() = p.settings(call("GET", "learning-settings"))
    fun saveSettings(s: LearningSettings) = p.settings(call("POST", "learning-settings", JSONObject()
        .put("guest_language", s.guestLanguage).put("style", s.style).put("coach_language", s.coachLanguage)
        .put("support", s.support).put("approved", true)))

    fun start(scenarioId: String, lessonId: String? = null, independent: Boolean = false) = p.session(call("POST", "sessions",
        JSONObject().put("scenario_id", scenarioId).put("independent", independent).apply { if (lessonId != null) put("lesson_id", lessonId) }))
    fun session(id: String) = p.session(call("GET", "sessions/$id"))
    fun respond(id: String, response: String) = p.turn(call("POST", "sessions/$id/responses", JSONObject().put("response", response)))
    fun next(id: String) = p.session(call("POST", "sessions/$id/next", JSONObject()))
    fun finish(id: String) = p.session(call("POST", "sessions/$id/finish", JSONObject()))
    fun guestLanguage(id: String, language: String) =
        p.session(call("POST", "sessions/$id/language", JSONObject().put("language", language)))
    fun coachLanguage(id: String, language: String) =
        p.session(call("POST", "sessions/$id/coach-language", JSONObject().put("language", language)))
    fun decide(sessionId: String, turnId: String, approved: Boolean) {
        call("POST", "sessions/$sessionId/assessment", JSONObject().put("turn_id", turnId).put("approved", approved))
    }

    fun reviewBatch(reviews: List<String>, language: String) =
        p.batch(call("POST", "review-batches", JSONObject().put("reviews", JSONArray(reviews)).put("language", language)))
    fun reviewBatches() = callArray("GET", "review-batches").objects().map(p::batch)
    fun lessons() = callArray("GET", "lessons").objects().map(p::lesson)
    fun batchLesson(batchId: String, groupId: String) = p.lesson(call("POST", "batch-lessons",
        JSONObject().put("batch_id", batchId).put("group_id", groupId).put("approved", true)))

    fun draft(sessionId: String, language: String) =
        p.draft(call("POST", "practice-drafts", JSONObject().put("session_id", sessionId).put("language", language)))
    fun saveDraft(id: String, text: String) =
        p.draft(call("POST", "drafts/$id", JSONObject().put("text", text).put("approved", true)))

    fun remember(original: String, preferred: String, context: String) {
        call("POST", "memory", JSONObject().put("original", original.take(150)).put("preferred", preferred.take(150))
            .put("context", context).put("approved", true))
    }

    private fun call(method: String, path: String, body: JSONObject? = null) = JSONObject(raw(method, path, body).ifBlank { "{}" })
    private fun callArray(method: String, path: String) = JSONArray(raw(method, path, null).ifBlank { "[]" })

    private fun raw(method: String, path: String, body: JSONObject?): String {
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
            if (code >= 400) {
                val error = runCatching { JSONObject(text) }.getOrNull()
                throw CoachApiException(error?.optString("error")?.ifBlank { null } ?: "HTTP $code", error?.optBoolean("retryable") == true)
            }
            return text
        } finally {
            conn.disconnect()
        }
    }
}
