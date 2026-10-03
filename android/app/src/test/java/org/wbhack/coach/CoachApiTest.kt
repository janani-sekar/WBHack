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
    private var profileLanguage = "es"

    private fun route(method: String, path: String): Pair<Int, String> = when (method to path) {
        "GET" to "/health" -> 200 to """{"ok":true,"model":"qwen3:1.7b","model_installed":true}"""
        "GET" to "/profile" -> 200 to """{"name":"Demo","language":"$profileLanguage","facts":{"tour":"60 minutes"}}"""
        "POST" to "/profile" -> 200 to """{"name":"Demo","language":"ta","facts":{}}"""
        "POST" to "/sessions" -> 200 to """{"id":"s1","skill":"duration","guest_message":"How long is the tour?","turns":[]}"""
        "POST" to "/sessions/s1/responses" -> 200 to """{"id":"t1","status":"pending","assessment":{"strength_local":"Respondiste con claridad.","improvement_local":"Ofrece confirmar la disponibilidad.","evidence_quote":"60 minutes","uncertain":false,"answers_request":2,"factual_accuracy":2,"clarifies_unknowns":1,"next_step":0}}"""
        "POST" to "/sessions/s1/next" -> 200 to """{"id":"s1","guest_message":"Can we come at 3?","turns":[{}]}"""
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

    @Test fun practiceFlowMapsScenarioAndSpanishFeedback() {
        val api = api()
        assertTrue(api.health().modelInstalled)
        val turn = api.start("time_request")
        assertEquals("s1", turn.sessionId)
        assertEquals("duration", requests.last().third!!.getString("skill"))
        val assessment = api.respond(turn.sessionId, "The tour takes 60 minutes.")
        assertEquals("t1", assessment.turnId)
        assertEquals("Ofrece confirmar la disponibilidad.", assessment.improvement)
        assertEquals(mapOf("answers_request" to 2, "factual_accuracy" to 2, "clarifies_unknowns" to 1, "next_step" to 0), assessment.scores)
        assertEquals("Can we come at 3?", api.next("s1").guestMessage)
        assertTrue(headers.none { "origin" in it })
        assertTrue(headers.filterIndexed { i, _ -> requests[i].third != null }
            .all { it["content-type"] == "application/json" })
    }

    @Test fun languageIsOnlyWrittenWhenItChanges() {
        api().ensureLanguage("es")
        assertTrue(requests.none { it.first == "POST" })
        api().ensureLanguage("ta")
        val post = requests.single { it.first == "POST" }.third!!
        assertEquals("ta", post.getString("language"))
        assertEquals("60 minutes", post.getJSONObject("facts").getString("tour"))
        assertTrue(post.getBoolean("approved"))
    }

    @Test fun backendErrorsSurfaceMessage() {
        val error = assertThrows(CoachApiException::class.java) { api().decide("missing", "t", true) }
        assertEquals("Local model unavailable", error.message)
        assertTrue(error.retryable)
    }

    @Test fun everyAndroidScenarioHasABackendSkill() {
        assertEquals(setOf("duration", "directions", "expectations"),
            listOf("time_request", "directions", "duration_availability").map { SKILL_FOR_SCENARIO.getValue(it) }.toSet())
    }
}
