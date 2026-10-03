package org.wbhack.coach

data class Scenario(val id: String, val guest: String, val rubric: List<String>, val unseenGuest: String) {
    val title: String get() = when (id) {
        "time_request" -> "A guest in a hurry"
        "directions" -> "A warm welcome"
        else -> "Planning a visit"
    }
}

data class Coaching(val suggestion: String, val example: String)

/** Integration seam for an on-device model. No generative model is bundled yet. */
interface PracticeEngine {
    fun coach(scenario: Scenario, response: String, language: String): Coaching
}

/** Fixed practice cards, deliberately not represented as AI assessment or translation. */
class ReferencePracticeEngine : PracticeEngine {
    override fun coach(scenario: Scenario, response: String, language: String): Coaching {
        require(response.isNotBlank()) { "Write a response first." }
        val english = when (scenario.id) {
            "time_request" -> "Compare your reply with the checklist. Mention the 20-minute tasting, then ask when they would like to start. Availability still needs confirmation."
            "directions" -> "Compare your reply with the checklist. Use the verified meeting point and ask whether they need more directions."
            else -> "Compare your reply with the checklist. Explain that the standard tour takes 60 minutes. Check availability before confirming a visit."
        }
        val es = when (scenario.id) {
            "time_request" -> "Compara tu respuesta con la lista. Menciona la degustación de café de 20 minutos y pregunta a qué hora les gustaría empezar. La disponibilidad aún debe confirmarse."
            "directions" -> "Compara tu respuesta con la lista. Indica el punto de encuentro verificado y pregunta si necesitan más indicaciones."
            else -> "Compara tu respuesta con la lista. Explica que el recorrido estándar dura 60 minutos. Verifica la disponibilidad antes de confirmar una visita."
        }
        val ta = when (scenario.id) {
            "time_request" -> "உங்கள் பதிலைப் பட்டியலுடன் ஒப்பிடுங்கள். காபி சுவைக்கும் நிகழ்ச்சி 20 நிமிடங்கள் என்று கூறுங்கள். அவர்கள் எப்போது தொடங்க விரும்புகிறார்கள் என்று கேளுங்கள். நேரம் கிடைக்குமா என்பதை உறுதி செய்ய வேண்டும்."
            "directions" -> "உங்கள் பதிலைப் பட்டியலுடன் ஒப்பிடுங்கள். நீல நிற வாயிலுக்கு அருகிலுள்ள பண்ணை நுழைவாயிலில் சந்திக்கலாம் என்று கூறுங்கள். மேலும் வழிகாட்டுதல் தேவையா என்று கேளுங்கள்."
            else -> "உங்கள் பதிலைப் பட்டியலுடன் ஒப்பிடுங்கள். பண்ணைச் சுற்றுலா 60 நிமிடங்கள் என்று கூறுங்கள். வருகையை உறுதி செய்வதற்கு முன் நேரம் கிடைக்குமா என்று சரிபார்க்க வேண்டும்."
        }
        val example = when (scenario.id) {
            "time_request" -> "Thank you for letting me know. Our short coffee tasting takes 20 minutes. What time would you like to start? I will need to confirm availability."
            "directions" -> "Please meet us at the farm entrance beside the blue gate. Would you like more directions?"
            else -> "Our standard farm tour takes 60 minutes. What time would you like to visit tomorrow? I will check availability before confirming."
        }
        val suggestion = when (language) {
            "es" -> es
            "ta" -> ta
            else -> english
        }
        return Coaching(suggestion, example)
    }
}
