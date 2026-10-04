package org.wbhack.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

private val Mist = Color(0xFFE8EDE3)
private val LANGUAGES = listOf("en" to "English", "es" to "Español")

/** State for the screens backed by the local coach; mirrors the desktop web UI. */
class LiveModel(val api: CoachApi, private val scope: CoroutineScope) {
    var tab by mutableIntStateOf(0)
    var cases by mutableStateOf(emptyList<Case>())
    var plan by mutableStateOf<Plan?>(null)
    var memory by mutableStateOf<LearnerMemory?>(null)
    var lessons by mutableStateOf(emptyList<Lesson>())
    var settings by mutableStateOf<LearningSettings?>(null)
    var batch by mutableStateOf<ReviewBatch?>(null)
    var practice by mutableStateOf<LiveSession?>(null)
    var busy by mutableStateOf<String?>(null)
    var error by mutableStateOf<String?>(null)

    suspend fun load() {
        cases = io { api.curriculum() }
        batch = io { api.reviewBatches() }.maxByOrNull { it.createdAt }
        refresh()
        settings = io { api.settings() }
    }

    suspend fun refresh() {
        plan = io { api.plan() }
        lessons = io { api.lessons() }
        memory = io { api.memory() }
    }

    fun guestFor(case: Case) = settings?.let { case.guestFor(it.guestLanguage, it.style) } ?: case.guest

    fun run(message: String, block: suspend () -> Unit) {
        if (busy != null) return
        scope.launch {
            busy = message; error = null
            try { block() } catch (e: CancellationException) { throw e } catch (e: Exception) {
                error = e.message ?: e.javaClass.simpleName
            } finally { busy = null }
        }
    }

    fun start(scenarioId: String, tr: Tr, lessonId: String? = null, independent: Boolean = false) =
        run(tr("Your guest is arriving…", "Tu huésped está llegando…")) {
            practice = io { api.start(scenarioId, lessonId, independent) }
            tab = 1
        }

    fun startLesson(lesson: Lesson, tr: Tr) {
        val id = lesson.scenarioId ?: cases.firstOrNull { it.skill == lesson.skill && it.difficulty == 1 }?.id ?: return
        start(id, tr, lesson.id)
    }

    fun restart(independent: Boolean, tr: Tr) {
        val p = practice ?: return
        start(p.scenarioId ?: return, tr, p.lessonId, independent)
    }

    fun reply(text: String, tr: Tr, sent: () -> Unit) {
        val id = practice?.id ?: return
        run(tr("Reading your reply…", "Leyendo tu respuesta…")) {
            io { api.respond(id, text) }
            sent()
            practice = io { api.session(id) }
            refresh()
            if (practice!!.turns.size < 3) {
                busy = tr("Your guest is replying…", "Tu huésped está respondiendo…")
                practice = io { api.next(id) }
            } else practice = io { api.finish(id) }
        }
    }

    fun nextGuest(tr: Tr) = practice?.id?.let { id ->
        run(tr("Your guest is thinking of a follow-up…", "Tu huésped está pensando…")) { practice = io { api.next(id) } }
    }

    fun finish(tr: Tr) = practice?.id?.let { id ->
        run(tr("Preparing your recap…", "Preparando tu resumen…")) { practice = io { api.finish(id) } }
    }

    fun decide(sessionId: String, turnId: String, approved: Boolean, tr: Tr) =
        run(tr("Saving your choice…", "Guardando tu elección…")) {
            io { api.decide(sessionId, turnId, approved) }
            practice = practice?.let { io { api.session(it.id) } }
            refresh()
        }

    fun guestLanguage(language: String, tr: Tr) = practice?.id?.let { id ->
        run(tr("Changing guest language…", "Cambiando el idioma del huésped…")) {
            practice = io { api.guestLanguage(id, language) }
            settings = settings?.let { s -> io { api.saveSettings(s.copy(guestLanguage = language)) } }
        }
    }

    fun coachLanguage(language: String, tr: Tr) = practice?.id?.let { id ->
        run(tr("Translating feedback; scores stay the same…", "Traduciendo comentarios; las notas no cambian…")) {
            practice = io { api.coachLanguage(id, language) }
        }
    }

    fun readReviews(reviews: List<String>, language: String, tr: Tr) =
        run(tr("Reading your reviews locally…", "Leyendo tus reseñas en este equipo…")) {
            batch = io { api.reviewBatch(reviews, language) }
        }

    fun saveLesson(batchId: String, groupId: String, tr: Tr) =
        run(tr("Saving practice…", "Guardando práctica…")) {
            io { api.batchLesson(batchId, groupId) }
            lessons = io { api.lessons() }
        }

    fun saveSettings(next: LearningSettings, tr: Tr) =
        run(tr("Saving practice settings…", "Guardando ajustes…")) { settings = io { api.saveSettings(next) } }
}

@Composable
internal fun WhiteCard(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val colors = CardDefaults.cardColors(containerColor = Color.White)
    val inner: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
    if (onClick != null) Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = colors, content = inner)
    else Card(Modifier.fillMaxWidth(), colors = colors, content = inner)
}

@Composable
private fun Fold(title: String, initiallyOpen: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    var open by rememberSaveable(title) { mutableStateOf(initiallyOpen) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { open = !open }, contentPadding = PaddingValues(0.dp)) {
            Text((if (open) "▾  " else "▸  ") + title, color = Pine, fontWeight = FontWeight.SemiBold)
        }
        if (open) content()
    }
}

@Composable
private fun Choice(label: String, options: List<Pair<String, String>>, selected: String, enabled: Boolean = true,
                   onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { (code, name) ->
                FilterChip(selected = selected == code, enabled = enabled, onClick = { if (selected != code) onSelect(code) },
                    label = { Text(name) })
            }
        }
    }
}

@Composable
private fun Small(text: String) = Text(text, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5B635E))

@Composable
private fun Label(text: String) = Text(text, fontWeight = FontWeight.Bold)

/** Busy/error strip shown above the navigation bar so it stays visible while scrolling. */
@Composable
internal fun LiveStatus(model: LiveModel, tr: Tr) {
    model.busy?.let {
        Row(Modifier.fillMaxWidth().background(Mist).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Text("$it ${tr("The local model can take a minute.", "El modelo local puede tardar un minuto.")}",
                style = MaterialTheme.typography.bodySmall)
        }
    }
    model.error?.let {
        Row(Modifier.fillMaxWidth().background(Color(0xFFF6E3DC)).padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Couldn’t finish that step: ", "No se pudo completar: ") + it, Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { model.error = null }) { Text("OK") }
        }
    }
}

@Composable
private fun Recommended(model: LiveModel, tr: Tr) {
    val plan = model.plan ?: return
    val case = model.cases.find { it.id == plan.recommendedId } ?: return
    val row = plan.skills.find { it.skill == case.skill }
    Text(tr("Recommended next", "Te recomendamos"), style = MaterialTheme.typography.titleLarge)
    WhiteCard(onClick = { model.start(case.id, tr) }) {
        Text(tr.skill(case.skill).uppercase(), color = Pine, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(case.title, style = MaterialTheme.typography.titleLarge)
        Text("“${model.guestFor(case)}”")
        Small(if (row != null && row.acceptedSessions > 0)
            tr("Try a different situation. Keep working on: ", "Prueba otra situación. Sigue trabajando en: ") + tr.criterion(row.weakest) + "."
        else tr("Start with a short conversation, then use the feedback to find your next step.",
            "Empieza con una conversación corta y usa los comentarios para decidir tu siguiente paso."))
        Text(tr("Start practice →", "Empezar práctica →"), color = Pine, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun LiveHome(model: LiveModel, tr: Tr, header: @Composable () -> Unit) {
    header()
    Recommended(model, tr)
    listOf(
        Triple(1, tr("Practice", "Practicar"), tr("${model.cases.size} guest situations. Rehearse a conversation and get feedback on each reply.",
            "${model.cases.size} situaciones con huéspedes. Ensaya una conversación y recibe comentarios en cada respuesta.")),
        Triple(2, tr("Guest feedback", "Comentarios de huéspedes"), tr("Understand reviews in your language. Choose what to practice next.",
            "Entiende reseñas en tu idioma y elige qué practicar.")),
        Triple(3, tr("Progress", "Progreso"), tr("See your saved results and what the coach remembers about you.",
            "Mira tus resultados guardados y lo que el coach recuerda de ti.")),
    ).forEach { (tab, title, body) ->
        WhiteCard(onClick = { model.tab = tab }) {
            Text("$title  →", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body)
        }
    }
}

@Composable
private fun LessonCard(model: LiveModel, lesson: Lesson, tr: Tr) = WhiteCard {
    Text(tr("FROM YOUR REVIEWS", "DE TUS RESEÑAS"), color = Pine, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Text(tr.topic(lesson.topic), style = MaterialTheme.typography.titleMedium)
    Small(lesson.evidence?.let { "“$it”" } ?: lesson.reason)
    OutlinedButton(onClick = { model.startLesson(lesson, tr) }, enabled = model.busy == null) {
        Text(tr("Practice similar →", "Practicar algo similar →"))
    }
}

@Composable
internal fun Catalog(model: LiveModel, tr: Tr) {
    var filter by rememberSaveable { mutableStateOf("all") }
    Text(tr("Choose a situation", "Elige una situación"), fontSize = 30.sp, fontWeight = FontWeight.Bold)
    Small(tr("${model.cases.size} practice situations. Exercise facts are fictional examples.",
        "${model.cases.size} situaciones de práctica. Los datos de los ejercicios son ejemplos ficticios."))
    model.lessons.forEach { LessonCard(model, it, tr) }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (listOf("all") + model.cases.map { it.skill }.distinct()).forEach {
            FilterChip(selected = filter == it, onClick = { filter = it },
                label = { Text(if (it == "all") tr("All", "Todas") else tr.skill(it)) })
        }
    }
    model.cases.filter { filter == "all" || it.skill == filter }.forEach { case ->
        WhiteCard(onClick = { model.start(case.id, tr) }) {
            Text(tr.level(case.difficulty).uppercase(), color = Pine, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(case.title, style = MaterialTheme.typography.titleLarge)
            Text("“${model.guestFor(case)}”")
            Text(tr("Practise this moment →", "Practicar este momento →"), color = Pine, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Bubble(who: String, text: String, guest: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (guest) Arrangement.Start else Arrangement.End) {
        Column(Modifier.fillMaxWidth(0.88f).background(if (guest) Pine else Color.White, RoundedCornerShape(18.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(who, color = if (guest) Color(0xFFCFE3D9) else Pine, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text, color = if (guest) Color.White else Color.Unspecified,
                style = if (guest) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun Scorecard(scores: Map<String, Number>, tr: Tr) = Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    SCORE_KEYS.filter { it in scores }.forEach {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(tr.criterion(it), Modifier.weight(1f))
            Text(tr.result(scores[it]), color = Pine, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun PracticeScreen(model: LiveModel, p: LiveSession, tr: Tr, store: CoachStore) {
    val ftr = tr(p.coachLanguage)
    val busy = model.busy != null
    var reply by rememberSaveable(p.id) { mutableStateOf("") }
    TextButton(onClick = { model.practice = null }, contentPadding = PaddingValues(0.dp)) {
        Text(tr("← All situations", "← Todas las situaciones"))
    }
    Text(tr.skill(p.skill).uppercase(), color = Pine, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Text(p.case?.title.orEmpty(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
    if (p.reviewQuote != null) Note(tr("From your guest feedback", "De los comentarios de tus huéspedes"),
        "“${p.reviewQuote}”\n${p.reviewFocus.orEmpty()}\n" + tr(
            "Rehearse a similar concern with a simulated guest. The exercise facts are examples, not your business facts.",
            "Ensaya una situación parecida con un huésped simulado. Los datos son ejemplos, no los de tu negocio."))
    else p.observationFocus?.let { Note(tr("Why this practice", "Por qué esta práctica"),
        tr("Based on feedback you saved: ", "Según comentarios que guardaste: ") + tr.criterion(it)) }
    Small(when {
        p.completed -> tr("Practice finished · your recap is below.", "Práctica terminada · el resumen está abajo.")
        p.turns.size >= 3 -> tr("Three replies complete · open your recap.", "Tres respuestas completas · abre el resumen.")
        else -> tr("Reply ${p.turns.size + 1} of 3 · recap after reply 3.", "Respuesta ${p.turns.size + 1} de 3 · resumen después de la 3.")
    })
    Choice(tr("Guest speaks", "El huésped habla"), LANGUAGES, p.guestLanguage, !busy && !p.completed) { model.guestLanguage(it, tr) }
    Choice(tr("Coach feedback in", "Comentarios del coach en"), LANGUAGES, p.coachLanguage, !busy) { model.coachLanguage(it, tr) }
    if (p.turns.isEmpty()) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Switch(checked = p.independent, enabled = !busy, onCheckedChange = { model.restart(it, tr) })
        Text(tr("Practice without hints", "Practicar sin pistas"))
    }
    p.case?.let { case ->
        WhiteCard {
            Label(tr("Exercise facts", "Datos del ejercicio"))
            case.facts.forEach { Text("${tr.factLabel(it.key)}: ${tr.factValue(it.key, it.value)}") }
            Small(tr("Fictional example data for practice.", "Datos ficticios de ejemplo para practicar."))
        }
        if (!p.independent) Fold(tr("Hints: what a good reply covers", "Pistas: qué incluye una buena respuesta")) {
            case.goals.forEach { Text("• $it") }
        }
    }
    p.turns.forEachIndexed { index, turn ->
        Bubble(tr("YOUR GUEST", "TU HUÉSPED"), turn.guestMessage, guest = true)
        Bubble(tr("YOU", "TÚ"), turn.response, guest = false)
        FeedbackCard(model, p, turn, index, ftr, store)
    }
    if (p.awaitingReply) Bubble(tr("YOUR GUEST", "TU HUÉSPED"), p.guestMessage, guest = true)
    if (!p.completed && p.turns.size < 3) {
        if (p.awaitingReply) {
            OutlinedTextField(reply, { reply = it.take(2000) }, Modifier.fillMaxWidth(), minLines = 3,
                label = { Text(tr("Your reply to the guest", "Tu respuesta al huésped")) },
                supportingText = { Text("${reply.length}/2000") })
            Button(onClick = { model.reply(reply, tr) { reply = "" } }, enabled = reply.isNotBlank() && !busy,
                modifier = Modifier.fillMaxWidth()) { Text(tr("Send reply & get feedback", "Enviar y recibir comentarios")) }
        } else OutlinedButton(onClick = { model.nextGuest(tr) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(tr("Next guest message", "Siguiente mensaje del huésped"))
        }
    }
    if (!p.completed && p.turns.isNotEmpty()) OutlinedButton(onClick = { model.finish(tr) }, enabled = !busy,
        modifier = Modifier.fillMaxWidth()) {
        Text(if (p.turns.size >= 3) tr("See conversation recap", "Ver resumen") else tr("Finish & see recap", "Terminar y ver resumen"))
    }
    if (p.completed) Recap(model, p, ftr)
}

@Composable
private fun FeedbackCard(model: LiveModel, p: LiveSession, turn: Turn, index: Int, ftr: Tr, store: CoachStore) {
    val a = turn.assessment
    val (strength, improvement) = turn.feedback(p.coachLanguage)
    val busy = model.busy != null
    var saved by remember(turn.id) { mutableStateOf(false) }
    Fold(ftr("Feedback · reply ${index + 1}", "Resultado · respuesta ${index + 1}"),
        initiallyOpen = index == p.turns.lastIndex && !p.independent) {
        Column(Modifier.fillMaxWidth().background(Mist, RoundedCornerShape(18.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (a.uncertain) ftr("The coach couldn’t score this reply", "El coach no pudo puntuar esta respuesta")
                else ftr.performance(a.scores), color = Pine, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (a.uncertain) {
                Text(ftr.issue(turn.issue))
                Small(ftr("Your reply is kept. Your progress is unchanged.", "Tu respuesta se conserva. Tu progreso no cambia."))
                p.case?.goals?.let { goals ->
                    Label(ftr("Exercise goals", "Objetivos del ejercicio"))
                    goals.forEach { Text("• $it") }
                }
                Label(ftr("Unconfirmed coaching suggestion", "Sugerencia sin confirmar"))
                Text(improvement)
            } else {
                Scorecard(a.scores, ftr)
                Label(ftr("What worked", "Qué funcionó")); Text(strength)
                Label(ftr("For your next reply", "Para tu próxima respuesta")); Text(improvement)
            }
            if (a.evidence.isNotBlank()) Small(ftr("Your reply: ", "Tu respuesta: ") + "“${a.evidence}”")
            if (!a.uncertain) Text(when (turn.status) {
                "approved" -> ftr("Saved to your progress", "Guardado en tu progreso")
                "rejected" -> ftr("Assessment dismissed", "Evaluación descartada")
                else -> ftr("Does this feedback match your reply?", "¿Te parece correcta esta evaluación?")
            }, fontWeight = FontWeight.SemiBold)
            if (!a.uncertain) Button(onClick = { model.decide(p.id, turn.id, true, ftr) },
                enabled = turn.status != "approved" && !busy, modifier = Modifier.fillMaxWidth()) {
                Text(ftr("Save to progress", "Guardar en mi progreso"))
            }
            OutlinedButton(onClick = { model.decide(p.id, turn.id, false, ftr) }, enabled = turn.status != "rejected" && !busy,
                modifier = Modifier.fillMaxWidth()) { Text(ftr("Dismiss assessment", "Descartar evaluación")) }
            TextButton(onClick = {
                val context = p.scenarioId ?: p.skill
                store.approve(turn.response.take(150), context)
                model.run(ftr("Saving phrase…", "Guardando frase…")) { io { model.api.remember(turn.response, turn.response, context) } }
                saved = true
            }, enabled = !saved && !busy) {
                Text(if (saved) ftr("Saved to My phrases", "Guardada en Mis frases") else ftr("Save my wording to My phrases", "Guardar mi frase en Mis frases"))
            }
        }
    }
}

@Composable
private fun Recap(model: LiveModel, p: LiveSession, ftr: Tr) {
    val recap = p.recap ?: return
    val busy = model.busy != null
    WhiteCard {
        Text(ftr("Conversation recap", "Resumen de la conversación"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Small("${recap.replyCount}" + ftr(" replies reviewed", " respuestas revisadas") +
            if (recap.earlyFinish) ftr(" · finished early", " · terminaste antes") else "")
        val eligible = p.turns.filter { it.status != "rejected" && !it.assessment.uncertain }
        if (eligible.isEmpty()) {
            val unscored = p.turns.any { it.assessment.uncertain && it.status != "rejected" }
            Text(if (unscored) ftr("Practice completed · not scored", "Práctica completada · sin nota")
                else ftr("Assessments dismissed", "Evaluaciones descartadas"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(if (unscored) ftr("The coach could not assess this conversation consistently. This does not mean you performed poorly. Your replies are kept and your progress is unchanged.",
                "El coach no pudo evaluar esta conversación de forma consistente. Esto no significa que lo hayas hecho mal. Tus respuestas se conservan y tu progreso no cambia.")
                else ftr("You dismissed the assessments for this conversation. They will not be used to judge your performance.",
                "Descartaste las evaluaciones de esta conversación. No se usarán para valorar tu desempeño."))
            p.case?.goals?.forEach { Text("• $it") }
            Button(onClick = { p.scenarioId?.let { model.start(it, ftr, p.lessonId) } }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(ftr("Retry this situation", "Reintentar esta situación"))
            }
            return@WhiteCard
        }
        Text(ftr.performance(recap.means), color = Pine, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Small("${eligible.size}" + ftr(" of ${p.turns.size} replies included. Criteria summarize those replies.",
            " de ${p.turns.size} respuestas incluidas. Los criterios resumen esas respuestas."))
        Scorecard(recap.means, ftr)
        val best = eligible.maxBy { it.assessment.scores["answers_request"] ?: 0 }
        Label(ftr("Keep doing this", "Sigue haciendo esto")); Text(best.feedback(p.coachLanguage).first)
        Label(ftr("One focus for next time", "Un objetivo para practicar"))
        Text(recap.focus?.let { focus -> eligible.minBy { it.assessment.scores[focus] ?: 2 }.feedback(p.coachLanguage).second }
            ?: ftr("The assessments show no specific gap. Try a different situation to check transfer.",
                "Las evaluaciones no indican una dificultad concreta. Prueba otra situación para comprobarlo."))
        Small(ftr("This recap uses the reply assessments, not a new grade. It is practice feedback, not a certification.",
            "Este resumen usa las evaluaciones de cada respuesta, no es una nueva nota. Es práctica, no una certificación."))
        DraftTool(model, p, ftr)
        Button(onClick = { p.scenarioId?.let { model.start(it, ftr) } }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(ftr("Try this situation again", "Practicar de nuevo"))
        }
        OutlinedButton(onClick = { model.practice = null; model.tab = 3 }, modifier = Modifier.fillMaxWidth()) {
            Text(ftr("View progress & memory", "Ver progreso y memoria"))
        }
    }
}

@Composable
private fun DraftTool(model: LiveModel, p: LiveSession, ftr: Tr) {
    var draft by remember(p.id) { mutableStateOf<Draft?>(null) }
    var text by rememberSaveable(p.id) { mutableStateOf("") }
    var saved by remember(p.id) { mutableStateOf(false) }
    Fold(ftr("Prepare notes for my business", "Preparar notas para mi negocio")) {
        Small(ftr("The exercise facts are fictional. Replace them with your business details.",
            "Los datos de este ejercicio son ficticios. Sustitúyelos por los de tu negocio."))
        val current = draft
        if (current == null) OutlinedButton(onClick = {
            model.run(ftr("Writing a draft…", "Escribiendo un borrador…")) {
                val made = io { model.api.draft(p.id, p.coachLanguage) }
                draft = made; text = made.text
            }
        }, enabled = model.busy == null) { Text(ftr("Create draft", "Crear borrador")) }
        else {
            OutlinedTextField(text, { text = it.take(3000); saved = false }, Modifier.fillMaxWidth(), minLines = 4,
                label = { Text(ftr("Editable notes", "Notas editables")) })
            Small(ftr("Check every detail. Nothing is sent.", "Comprueba cada dato. No se envía nada."))
            OutlinedButton(onClick = {
                model.run(ftr("Saving notes…", "Guardando notas…")) { io { model.api.saveDraft(current.id, text) }; saved = true }
            }, enabled = text.isNotBlank() && model.busy == null) { Text(ftr("Save reviewed notes", "Guardar notas revisadas")) }
            if (saved) Text(ftr("Saved on your computer.", "Guardado en tu computadora."), color = Pine)
        }
    }
}

@Composable
internal fun ReviewsScreen(model: LiveModel, tr: Tr) {
    val reviews = remember { mutableStateListOf("") }
    var language by rememberSaveable { mutableStateOf(model.settings?.coachLanguage ?: if (tr.es) "es" else "en") }
    Text(tr("Guest feedback", "Comentarios de huéspedes"), fontSize = 30.sp, fontWeight = FontWeight.Bold)
    Text(tr("Paste up to five guest reviews. The local coach translates them and finds what to practice. Nothing is posted or sent.",
        "Pega hasta cinco reseñas. El coach local las traduce y encuentra qué practicar. No se publica ni se envía nada."))
    reviews.forEachIndexed { i, value ->
        OutlinedTextField(value, { reviews[i] = it.take(1800) }, Modifier.fillMaxWidth(), minLines = 3,
            label = { Text(tr("Review ${i + 1}", "Reseña ${i + 1}")) })
    }
    if (reviews.size < 5) TextButton(onClick = { reviews.add("") }) { Text(tr("+ Add another review", "+ Añadir otra reseña")) }
    Choice(tr("Explain in", "Explicar en"), LANGUAGES, language) { language = it }
    Button(onClick = { model.readReviews(reviews.map { it.trim() }.filter { it.isNotEmpty() }, language, tr) },
        enabled = reviews.any { it.isNotBlank() } && model.busy == null, modifier = Modifier.fillMaxWidth()) {
        Text(tr("Understand these reviews", "Entender estas reseñas"))
    }
    model.batch?.let { BatchResult(model, it) }
}

@Composable
private fun BatchResult(model: LiveModel, batch: ReviewBatch) {
    val btr = tr(batch.language)
    val saved = model.lessons.filter { it.batchId == batch.id }
    Text(btr("What the guest is saying", "Lo que dice el visitante"), style = MaterialTheme.typography.titleLarge)
    batch.findings.forEach { g ->
        WhiteCard {
            Text(btr.topic(g.topic).uppercase(), color = Pine, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(g.explanation)
            g.quotes.firstOrNull()?.let { Small("“$it”") }
            if (g.reviewCount > 1) Small(btr("Mentioned in ${g.reviewCount} reviews", "Mencionado en ${g.reviewCount} reseñas"))
            if (g.uncertain) Small(btr("Check what the guest meant.", "Confirma qué quiso decir el visitante."))
            if (g.action == "improve") Small(btr("Business improvement", "Mejora del negocio"))
            if (g.canPractice) {
                val lesson = saved.find { it.groupId == g.id }
                if (lesson == null) OutlinedButton(onClick = { model.saveLesson(batch.id, g.id, btr) }, enabled = model.busy == null) {
                    Text(btr("Practice similar →", "Practicar algo similar →"))
                } else Button(onClick = { model.startLesson(lesson, btr) }, enabled = model.busy == null) {
                    Text(btr("Saved · Start practice →", "Guardado · Empezar práctica →"))
                }
            }
        }
    }
    Note(btr("Practice", "Práctica"), when {
        saved.isNotEmpty() -> btr("Practice saved — also in the Practice tab. It uses example facts, not your real business facts.",
            "Ejercicio guardado; también está en Practicar. Usa datos de ejemplo, no los datos reales de tu negocio.")
        batch.findings.any { it.canPractice } -> btr("Choose “Practice similar” to save an exercise.", "Elige «Practicar algo similar» para guardar un ejercicio.")
        else -> btr("No practice exercise created from this feedback.", "No se creó un ejercicio a partir de esta reseña.")
    })
    Fold(btr("Reviews & translations", "Reseñas y traducciones")) {
        batch.reviews.forEach {
            Text("“${it.original}”", fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodySmall)
            Text(it.translation)
        }
    }
}

@Composable
internal fun ProgressScreen(model: LiveModel, tr: Tr, phrases: @Composable () -> Unit) {
    Text(tr("Your progress", "Tu progreso"), fontSize = 30.sp, fontWeight = FontWeight.Bold)
    Recommended(model, tr)
    model.plan?.skills?.forEach { row ->
        WhiteCard {
            Text(tr.skill(row.skill), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(tr.planState(row))
            LinearProgressIndicator(progress = { planFill(row) }, modifier = Modifier.fillMaxWidth())
            Small(tr("${row.acceptedSessions} accepted sessions · ${row.independentCases} situations without hints",
                "${row.acceptedSessions} prácticas aceptadas · ${row.independentCases} situaciones sin pistas"))
        }
    }
    Text(tr("What the coach remembers", "Lo que el coach recuerda"), style = MaterialTheme.typography.titleLarge)
    model.memory?.let { m ->
        Small(tr("Your choices: ", "Tus preferencias: ") + m.preferences.coachLanguage.uppercase() + tr(" coaching · ", " comentarios · ") +
            if (m.preferences.support == "example") tr("explanations with examples", "explicaciones con ejemplos") else tr("short next steps", "pasos breves"))
        if (m.observations.isEmpty()) Small(tr("No skill observations yet. Save feedback you agree with after practice.",
            "Aún no hay observaciones. Guarda los comentarios con los que estés de acuerdo después de practicar."))
        m.observations.forEach { o ->
            WhiteCard {
                Text(tr.skill(o.skill), fontWeight = FontWeight.SemiBold)
                Text((o.focus?.let { tr("Practice focus: ", "Enfoque de práctica: ") + tr.criterion(it) }
                    ?: tr("No specific gap in accepted feedback", "Sin una dificultad concreta en los comentarios aceptados")) +
                    tr(". Based on ${o.acceptedSessions} accepted sessions; tentative.", ". Basado en ${o.acceptedSessions} prácticas aceptadas; provisional."))
                o.sources.forEach { s ->
                    Small("“${s.quote}”")
                    TextButton(onClick = { model.decide(s.sessionId, s.turnId, false, tr) }, enabled = model.busy == null) {
                        Text(tr("Stop using this feedback", "Dejar de usar este comentario"))
                    }
                }
            }
        }
    }
    Small(tr("Preferences are your choices. Skill observations come from AI feedback you accepted and can be wrong. Not a professional certification.",
        "Las preferencias son tuyas. Las observaciones vienen de comentarios de IA que aceptaste y pueden equivocarse. No es una certificación profesional."))
    phrases()
}

@Composable
internal fun PracticeSettings(model: LiveModel, tr: Tr) {
    val s = model.settings ?: return
    val enabled = model.busy == null
    Text(tr("Practice settings", "Ajustes de práctica"), style = MaterialTheme.typography.titleLarge)
    Choice(tr("Guest language", "Idioma del huésped"), LANGUAGES, s.guestLanguage, enabled) {
        model.saveSettings(s.copy(guestLanguage = it), tr)
    }
    Choice(tr("Guest message style", "Estilo del huésped"),
        listOf("clear" to tr("Clear", "Claro"), "chat" to tr("Everyday chat", "Chat informal")), s.style, enabled) {
        model.saveSettings(s.copy(style = it), tr)
    }
    Choice(tr("Coach support", "Ayuda del coach"),
        listOf("brief" to tr("Short next step", "Paso breve"), "example" to tr("With an example", "Con un ejemplo")), s.support, enabled) {
        model.saveSettings(s.copy(support = it), tr)
    }
    Small(tr("Coach feedback follows the app language (English / Español). You can also switch it inside a practice.",
        "Los comentarios del coach siguen el idioma de la app (English / Español). También puedes cambiarlo dentro de una práctica."))
}
