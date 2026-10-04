package org.wbhack.coach

/** Picks English or Spanish copy for the AI-connected screens. */
class Tr(val es: Boolean) {
    operator fun invoke(en: String, es: String) = if (this.es) es else en
}

fun tr(language: String) = Tr(language == "es")

private val SKILLS = mapOf(
    "duration" to ("Make the visit work" to "Que la visita funcione"),
    "directions" to ("Help guests find you" to "Ayudar a que te encuentren"),
    "expectations" to ("Set clear expectations" to "Aclarar expectativas"),
)
private val CRITERIA = mapOf(
    "answers_request" to ("Answer the question" to "Responder a la pregunta"),
    "factual_accuracy" to ("Use confirmed facts" to "Usar datos confirmados"),
    "clarifies_unknowns" to ("Check what’s unknown" to "Aclarar lo desconocido"),
    "next_step" to ("Offer a next step" to "Proponer el siguiente paso"),
    "customer_tone" to ("Speak respectfully to the guest" to "Hablar con respeto"),
)
private val TOPICS = mapOf(
    "arrival" to ("Give clearer directions" to "Dar indicaciones más claras"),
    "timing" to ("Explain visit timing" to "Explicar la duración de la visita"),
    "inclusions" to ("Explain what is included" to "Explicar qué incluye la visita"),
    "booking" to ("Clarify a booking request" to "Aclarar una reserva"),
    "hospitality" to ("Respond to an unhappy guest" to "Responder a un visitante insatisfecho"),
)
private val FACTS = mapOf(
    "full_tour_minutes" to ("Full tour" to "Recorrido completo"),
    "standard_tour_minutes" to ("Full tour" to "Recorrido completo"),
    "tasting_minutes" to ("Coffee tasting" to "Degustación de café"),
    "short_tasting_minutes" to ("Coffee tasting" to "Degustación de café"),
    "includes" to ("Included activities" to "Actividades incluidas"),
    "lunch_included" to ("Lunch" to "Almuerzo"),
    "meeting_point" to ("Meeting point" to "Punto de encuentro"),
)

private fun Tr.pick(map: Map<String, Pair<String, String>>, key: String?) = map[key]?.let { this(it.first, it.second) }

fun Tr.skill(key: String) = pick(SKILLS, key) ?: key
fun Tr.criterion(key: String) = pick(CRITERIA, key) ?: key.replace('_', ' ')
fun Tr.topic(key: String?) = pick(TOPICS, key) ?: this("Practice from guest feedback", "Práctica a partir de comentarios")
fun Tr.level(difficulty: Int) = when (difficulty) {
    1 -> this("Start here", "Empieza aquí")
    2 -> this("Build confidence", "Gana confianza")
    else -> this("Stretch your skills", "Ponte a prueba")
}

/** One criterion result, matching the desktop UI wording. */
fun Tr.result(score: Number?) = when {
    score == null -> this("Not assessed", "Sin evaluar")
    score.toDouble() >= 2 -> this("Met", "Logrado")
    score.toDouble() >= 1 -> this("Partly met", "Parcial")
    else -> this("Needs work", "Por trabajar")
}

/** Overall label for one reply or a recap; a factual miss is never "almost there". */
fun Tr.performance(scores: Map<String, Number>): String {
    val values = scores.values.map { it.toDouble() }
    return when {
        values.isEmpty() -> this("Not assessed", "Sin evaluar")
        values.all { it >= 2 } -> this("Handled well", "Bien resuelto")
        values.any { it < 1 } || (scores["factual_accuracy"]?.toDouble() ?: 2.0) < 2 -> this("Needs practice", "Necesita práctica")
        else -> this("Almost there", "Casi conseguido")
    }
}

/** Explains an unscored reply as an AI assessment problem, not a learner failure. */
fun Tr.issue(key: String?) = when (key) {
    "checks_disagree" -> this(
        "The coach and its fact-check disagree about your reply. This is a grading problem, not a failing grade.",
        "El coach y su comprobación de datos discrepan sobre tu respuesta. Es un problema de evaluación, no una nota de suspenso.")
    "facts_unclear" -> this(
        "The coach could not verify the facts in your reply. No grade has been assigned.",
        "El coach no pudo comprobar los datos de tu respuesta. No se ha asignado una nota.")
    else -> this(
        "The coach could not interpret your reply clearly enough to score it. This is not a failing grade.",
        "El coach no pudo interpretar tu respuesta con suficiente claridad para puntuarla. No es una nota de suspenso.")
}

fun Tr.factLabel(key: String) = pick(FACTS, key) ?: key.replace('_', ' ').replaceFirstChar { it.uppercase() }
fun Tr.factValue(key: String, value: String) = when {
    value == "true" -> if (key.endsWith("_included")) this("Included", "Incluido") else this("Yes", "Sí")
    value == "false" -> if (key.endsWith("_included")) this("Not included", "No incluido") else "No"
    key.endsWith("_minutes") -> "$value ${this("minutes", "minutos")}"
    else -> value
}

fun Tr.planState(row: SkillPlan) = when {
    row.state == "retained_in_simulation" -> this("Revisited successfully after a delay", "Repasado con éxito tras un tiempo")
    row.acceptedSessions > 0 -> this("Building confidence", "Ganando confianza")
    else -> this("A fresh place to start", "Un buen punto de partida")
}

fun planFill(row: SkillPlan) = when {
    row.state == "retained_in_simulation" -> 1f
    row.acceptedSessions > 0 -> 0.45f
    else -> 0f
}
