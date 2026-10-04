package org.wbhack.coach

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackTest {
    private val en = tr("en")
    private val es = tr("es")

    @Test fun performanceMatchesDesktopRules() {
        assertEquals("Handled well", en.performance(SCORE_KEYS.associateWith { 2 }))
        assertEquals("Almost there", en.performance(mapOf("answers_request" to 2, "next_step" to 1)))
        assertEquals("Needs practice", en.performance(mapOf("answers_request" to 2, "factual_accuracy" to 1)))
        assertEquals("Necesita práctica", es.performance(mapOf("next_step" to 0.5)))
        assertEquals("Not assessed", en.performance(emptyMap()))
    }

    @Test fun unscoredRepliesAreNotFailingGrades() {
        listOf("checks_disagree", "facts_unclear", "interpretation_unclear", null).forEach {
            assertTrue(en.issue(it).contains("grade"))
        }
    }

    @Test fun factsAreReadable() {
        assertEquals("30 minutos", es.factValue("tasting_minutes", "30"))
        assertEquals("Not included", en.factValue("lunch_included", "false"))
        assertEquals("Meeting point", en.factLabel("meeting_point"))
        assertEquals("Parcial", es.result(1.5))
    }

    @Test
    fun modelDelaysAreRetryableNotFailures() {
        assertTrue(isModelDelay(java.net.SocketTimeoutException("timeout")))
        assertTrue(isModelDelay(CoachApiException("Local model unavailable", retryable = true)))
        assertFalse(isModelDelay(CoachApiException("response required")))
    }
}
