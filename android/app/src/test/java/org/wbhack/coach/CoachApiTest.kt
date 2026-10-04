package org.wbhack.coach

import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

class CoachApiTest {
    private lateinit var server: ServerSocket
    private val requests = mutableListOf<Triple<String, String, JSONObject?>>()
    private val headers = mutableListOf<Map<String, String>>()

    private val case = """{"id":"short_visit","skill":"duration","difficulty":1,"title":"Short visit","guest":"We only have 45 minutes.",
        "guest_variants":{"es":{"clear":"Solo tenemos 45 minutos.","chat":"solo 45 min"}},
        "facts":{"tasting_minutes":30,"includes":["farm walk","tasting"],"lunch_included":false},
        "goal":"Say what fits; offer the tasting"}"""
    private val turn = """{"id":"t1","response":"The tasting takes 30 minutes.","guest_message":"We only have 45 minutes.",
        "status":"pending","assessment_issue":"checks_disagree","question_index":0,"coach_language":"en",
        "assessment":{"strength_local":"Clear answer.","improvement_local":"Offer a next step.","evidence_quote":"30 minutes",
        "uncertain":true,"answers_request":2,"factual_accuracy":2,"clarifies_unknowns":1,"next_step":0,"customer_tone":2},
        "feedback_variants":{"en":{"strength_local":"Clear answer.","improvement_local":"Offer a next step."},
        "es":{"strength_local":"Respuesta clara.","improvement_local":"Ofrece un siguiente paso."}}}"""
    private fun session(completed: Boolean, turns: String) = """{"id":"s1","skill":"duration","scenario_id":"short_visit",
        "lesson_id":"l1","scenario":$case,"guest_message":"We only have 45 minutes.","guest_language":"en","coach_language":"es",
        "independent":false,"question_index":0,"completed":$completed,"turns":[$turns],
        "personalization":{"review_focus":"Explain timing","review_sources":[{"quote":"Too long for us"}],"observation":null}
        ${if (completed) ""","summary":{"reply_count":1,"early_finish":true,"criterion_means":{"answers_request":2.0,"next_step":0.5},"focus":"next_step"}""" else ""}}"""
    private val batch = """{"id":"b1","language":"es","created_at":"2026-10-03T00:00:00",
        "reviews":[{"original":"Too long","analysis":{"translation_local":"Demasiado largo"}}],
        "groups":[{"id":"g1","topic":"timing","action":"practice","review_count":2,"can_practice":true,"uncertain":false,
        "sources":[{"quote":"Too long","explanation_local":"La visita fue larga."}]}]}"""
    private val lesson = """{"id":"l1","batch_id":"b1","group_id":"g1","topic":"timing","skill":"duration",
        "scenario_id":"short_visit","evidence_quote":"Too long","reason_local":"Practica la duración"}"""

    private fun route(method: String, path: String): Pair<Int, String> = when (method to path) {
        "GET" to "/health" -> 200 to """{"ok":true,"model":"qwen3:1.7b","model_installed":true}"""
        "GET" to "/curriculum" -> 200 to "[$case]"
        "POST" to "/sessions" -> 200 to session(false, "")
        "POST" to "/sessions/s1/responses" -> 200 to turn
        "GET" to "/sessions/s1", "POST" to "/sessions/s1/finish" -> 200 to session(true, turn)
        "GET" to "/learning-plan" -> 200 to """{"skills":[{"skill":"duration","accepted_sessions":1,"independent_cases":0,
            "state":"practicing","weakest_dimension":"next_step","criterion_means":{"next_step":0.5}}],
            "recommended_scenario_id":"late_arrival","recommended_title":"Late arrival"}"""
        "GET" to "/learner-memory" -> 200 to """{"preferences":{"guest_language":"es","style":"clear","coach_language":"es","support":"example"},
            "skill_observations":[{"skill":"duration","practice_focus":"next_step","accepted_sessions":1,
            "sources":[{"session_id":"s1","turn_id":"t1","quote":"30 minutes"}]}]}"""
        "POST" to "/learning-settings" -> 200 to """{"guest_language":"es","style":"clear","coach_language":"es","support":"example"}"""
        "POST" to "/review-batches" -> 200 to batch
        "POST" to "/batch-lessons" -> 200 to lesson
        "GET" to "/lessons" -> 200 to "[$lesson]"
        else -> 503 to """{"error":"Local model unavailable","retryable":true}"""
    }

    @Before fun start() {
        server = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
        thread(isDaemon = true) {
            while (!server.isClosed) {
                val socket = runCatching { server.accept() }.getOrNull() ?: break
                socket.use {
                    val input = it.getInputStream().bufferedReader(Charsets.UTF_8)
                    val (method, path) = input.readLine().split(" ")
                    val head = generateSequence { input.readLine()?.takeIf(String::isNotEmpty) }
                        .associate { line -> line.substringBefore(":").lowercase() to line.substringAfter(":").trim() }
                    val length = head["content-length"]?.toInt() ?: 0
                    val body = if (length > 0) CharArray(length).also { buf -> var n = 0; while (n < length) n += input.read(buf, n, length - n) }
                        .let { buf -> JSONObject(String(buf)) } else null
                    synchronized(requests) { requests += Triple(method, path, body); headers += head }
                    val (code, reply) = route(method, path)
                    val bytes = reply.toByteArray()
                    it.getOutputStream().apply {
                        write("HTTP/1.1 $code X\r\nContent-Type: application/json\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray())
                        write(bytes); flush()
                    }
                }
            }
        }
    }

    @After fun stop() = server.close()

    private fun api() = CoachApi("http://127.0.0.1:${server.localPort}/")
    private fun lastBody() = requests.last().third!!

    @Test fun curriculumSessionFlowParsesCurrentBackend() {
        val api = api()
        assertTrue(api.health().modelInstalled)
        val case = api.curriculum().single()
        assertEquals("Solo tenemos 45 minutos.", case.guestFor("es", "clear"))
        assertEquals("We only have 45 minutes.", case.guestFor("en", "chat"))
        assertEquals(listOf("Say what fits", "offer the tasting"), case.goals)
        assertEquals("farm walk, tasting", case.facts.single { it.key == "includes" }.value)

        val started = api.start("short_visit", lessonId = "l1")
        assertEquals("short_visit", lastBody().getString("scenario_id"))
        assertEquals("l1", lastBody().getString("lesson_id"))
        assertFalse(lastBody().getBoolean("independent"))
        assertTrue(started.awaitingReply)
        assertEquals("Too long for us", started.reviewQuote)
        assertNull(started.observationFocus)

        val turn = api.respond("s1", "The tasting takes 30 minutes.")
        assertEquals("checks_disagree", turn.issue)
        assertTrue(turn.assessment.uncertain)
        assertEquals(SCORE_KEYS.toSet(), turn.assessment.scores.keys)
        assertEquals("Ofrece un siguiente paso.", turn.feedback("es").second)
        assertEquals("Offer a next step.", turn.feedback("ta").second)

        val done = api.finish("s1")
        assertTrue(done.completed)
        assertFalse(done.awaitingReply)
        assertEquals(Recap(1, true, mapOf("answers_request" to 2.0, "next_step" to 0.5), "next_step"), done.recap)
        assertTrue(headers.none { "origin" in it })
        assertTrue(headers.filterIndexed { i, _ -> requests[i].third != null }.all { it["content-type"] == "application/json" })
    }

    @Test fun reviewsBecomeApprovedLessons() {
        val api = api()
        val result = api.reviewBatch(listOf("Too long", "Slow"), "es")
        assertEquals(2, lastBody().getJSONArray("reviews").length())
        assertEquals("es", lastBody().getString("language"))
        val finding = result.findings.single()
        assertTrue(finding.canPractice)
        assertEquals("La visita fue larga.", finding.explanation)
        assertEquals("Demasiado largo", result.reviews.single().translation)
        api.batchLesson("b1", "g1")
        assertTrue(lastBody().getBoolean("approved"))
        assertEquals("short_visit", api.lessons().single().scenarioId)
    }

    @Test fun planMemoryAndSettings() {
        val api = api()
        val plan = api.plan()
        assertEquals("late_arrival", plan.recommendedId)
        assertEquals("next_step", plan.skills.single().weakest)
        val memory = api.memory()
        assertEquals("example", memory.preferences.support)
        assertEquals(MemorySource("s1", "t1", "30 minutes"), memory.observations.single().sources.single())
        api.saveSettings(LearningSettings("es", "clear", "es", "example"))
        with(lastBody()) {
            assertTrue(getBoolean("approved"))
            assertEquals("clear", getString("style"))
            assertEquals("es", getString("coach_language"))
            assertEquals("example", getString("support"))
        }
    }

    @Test fun backendErrorsSurfaceMessage() {
        val error = assertThrows(CoachApiException::class.java) { api().decide("missing", "t", true) }
        assertEquals("Local model unavailable", error.message)
        assertTrue(error.retryable)
    }
}
