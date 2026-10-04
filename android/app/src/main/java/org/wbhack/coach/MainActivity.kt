package org.wbhack.coach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val Pine = Color(0xFF21594D)
private val Cream = Color(0xFFF8F6EF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = CoachStore(applicationContext)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Pine, background = Cream,
                surface = Cream, secondaryContainer = Color(0xFFE0EBDF))) {
                CoachApp(store)
            }
        }
    }
}

@Composable
private fun CoachApp(store: CoachStore) {
    val t = uiText(store.language)
    val tr = tr(store.language)
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var health by remember { mutableStateOf<Health?>(null) }
    var checking by remember { mutableStateOf(true) }
    val api = remember(store.baseUrl) { CoachApi(store.baseUrl) }
    val scope = rememberCoroutineScope()
    val live = remember(api) { LiveModel(api, scope) }
    LaunchedEffect(api, refresh) {
        checking = true
        health = runCatching { io { api.health() } }.getOrNull()
        if (health?.modelInstalled == true) runCatching { live.load() }.onFailure { live.error = it.message }
        checking = false
    }
    val connected = health?.modelInstalled == true && live.settings != null
    LaunchedEffect(connected, store.language) {
        val s = live.settings
        if (connected && s != null && store.language in setOf("es", "en") && s.coachLanguage != store.language)
            runCatching { live.settings = io { api.saveSettings(s.copy(coachLanguage = store.language)) } }
    }
    val scenario = store.scenarios.find { it.id == selected }
    val inPractice = connected && live.tab == 1 && live.practice != null
    BackHandler(enabled = selected != null || inPractice) { if (inPractice) live.practice = null else selected = null }
    val scroll = rememberScrollState()
    LaunchedEffect(live.tab, live.practice?.id, tab, selected) { scroll.scrollTo(0) }
    Scaffold(bottomBar = {
        Column {
            if (connected) LiveStatus(live, tr)
            if (store.welcomed && selected == null && !inPractice) NavigationBar {
                val tabs = if (connected) listOf(tr("Home", "Inicio") to Icons.Default.Home,
                    tr("Practice", "Practicar") to Icons.Default.PlayArrow, tr("Reviews", "Reseñas") to Icons.Default.Star,
                    tr("Progress", "Progreso") to Icons.Default.Person, t.tabs[2] to Icons.Default.Settings)
                else t.tabs.zip(listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Settings))
                val current = if (connected) live.tab else tab
                tabs.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(selected = current == index, onClick = { if (connected) live.tab = index else tab = index },
                        icon = { Icon(icon, contentDescription = null) }, label = { Text(label, maxLines = 1) })
                }
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()
            .verticalScroll(scroll).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("guest-i-mate", color = Pine, fontSize = 12.sp, letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold)
            val header: @Composable () -> Unit = {
                Text(if (store.language == "ta") "வணக்கம்!" else t.welcomeBack, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text(t.tagline, style = MaterialTheme.typography.headlineMedium)
                AiStatus(t, health, checking)
            }
            val phrases: @Composable () -> Unit = { Phrases(store, t) { id -> live.cases.find { it.id == id }?.title } }
            when {
                !store.welcomed -> Welcome(store, t)
                connected -> when (live.tab) {
                    1 -> live.practice?.let { key(it.id) { PracticeScreen(live, it, tr, store) } } ?: Catalog(live, tr)
                    2 -> ReviewsScreen(live, tr)
                    3 -> ProgressScreen(live, tr, phrases)
                    4 -> Settings(store, t, health, checking, { refresh++ }) { PracticeSettings(live, tr) }
                    else -> LiveHome(live, tr, header)
                }
                scenario != null -> key(scenario.id) { Session(store, scenario, t) { selected = null } }
                tab == 1 -> phrases()
                tab == 2 -> Settings(store, t, health, checking, { refresh++ }) {}
                else -> {
                    header()
                    Text(t.progress(store.practiceCount, store.completed.size), style = MaterialTheme.typography.bodyMedium)
                    Text(t.nextConversation, style = MaterialTheme.typography.titleLarge)
                    store.scenarios.sortedBy { it.id in store.completed }.forEachIndexed { index, item ->
                        Card(onClick = { selected = item.id }, modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("0${index + 1}   /   ${if (item.id in store.completed) t.practiceAgain else t.startHere}", color = Pine, fontSize = 12.sp)
                                Text(t.titles[item.id] ?: item.title, style = MaterialTheme.typography.titleLarge)
                                Text(item.guest)
                                Text(t.openPractice, color = Pine, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Text(t.footer, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun AiStatus(t: UiText, health: Health?, checking: Boolean) {
    when {
        checking -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp); Text(t.checking)
        }
        health?.modelInstalled == true -> Note(t.aiOnTitle, t.aiOnBody(health.model))
        else -> Note(t.aiOffTitle, t.aiOffBody)
    }
}

@Composable
private fun Welcome(store: CoachStore, t: UiText) {
    Spacer(Modifier.height(24.dp))
    Text(t.welcomeTitle, fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold)
    Text(t.welcomeBody, style = MaterialTheme.typography.titleMedium)
    LanguageChoice(store, t)
    Note(t.safeTitle, t.safeBody)
    Note(t.previewTitle, t.previewBody)
    Button(onClick = store::welcome, modifier = Modifier.fillMaxWidth()) { Text(t.start) }
}

/** Offline practice with the fixed reference guide, used when the local coach is unreachable. */
@Composable
private fun Session(store: CoachStore, scenario: Scenario, t: UiText, close: () -> Unit) {
    var response by rememberSaveable { mutableStateOf("") }
    var reviewed by rememberSaveable { mutableStateOf(false) }
    var novel by rememberSaveable { mutableStateOf(false) }
    var counted by rememberSaveable { mutableStateOf(false) }
    var saveDialog by rememberSaveable { mutableStateOf(false) }
    var phrase by rememberSaveable { mutableStateOf("") }
    val engine = remember { ReferencePracticeEngine() }

    TextButton(onClick = close) { Text(t.allScenarios) }
    Text(t.titles[scenario.id] ?: scenario.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Text(if (novel) t.freshSituation else t.guestAsks, color = Pine)
    Card(colors = CardDefaults.cardColors(containerColor = Pine)) {
        Text(if (novel) scenario.unseenGuest else scenario.guest, Modifier.padding(24.dp), color = Color.White,
            style = MaterialTheme.typography.titleLarge)
    }
    Text(t.facts, fontWeight = FontWeight.Bold)
    Text(t.factsList)
    Text(t.factsNote, style = MaterialTheme.typography.bodySmall)
    OutlinedTextField(value = response, onValueChange = { response = it.take(2000); reviewed = false },
        modifier = Modifier.fillMaxWidth(), minLines = 4, label = { Text(t.replyLabel) },
        supportingText = { Text(t.replyCounter(response.length)) })
    store.phrases.filter { it.context == scenario.id }.forEach { item ->
        TextButton(onClick = { response = item.text; reviewed = false }) { Text(t.useSaved(item.text)) }
    }
    Button(onClick = { reviewed = true }, enabled = response.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(t.compare) }
    if (reviewed) {
        val coaching = engine.coach(scenario, response, store.language)
        Note(t.oneThing, coaching.suggestion)
        Text(t.selfCheck, style = MaterialTheme.typography.titleMedium)
        scenario.rubric.forEach { Text("• $it") }
        Note(t.example, coaching.example)
        Text(t.fixedNote, style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { reviewed = false }, modifier = Modifier.fillMaxWidth()) { Text(t.improve) }
        OutlinedButton(onClick = { phrase = response; saveDialog = true }, modifier = Modifier.fillMaxWidth()) { Text(t.remember) }
        if (!novel) TextButton(onClick = { novel = true; response = ""; reviewed = false }) { Text(t.tryNew) }
        Button(onClick = {
            if (!counted) { store.complete(scenario.id); counted = true }
            close()
        }, modifier = Modifier.fillMaxWidth()) { Text(t.finish) }
    }
    if (saveDialog) AlertDialog(onDismissRequest = { saveDialog = false }, title = { Text(t.saveTitle) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(t.saveBody)
            OutlinedTextField(value = phrase, onValueChange = { phrase = it.take(150) }, label = { Text(t.approvedWording) })
        } }, confirmButton = { TextButton(enabled = phrase.isNotBlank(), onClick = {
            store.approve(phrase, scenario.id)
            saveDialog = false
        }) { Text(t.approveSave) } },
        dismissButton = { TextButton(onClick = { saveDialog = false }) { Text(t.cancel) } })
}

@Composable
private fun Phrases(store: CoachStore, t: UiText, titleFor: (String) -> String?) {
    Text(t.phrasesTitle, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Text(t.phrasesBody)
    if (store.phrases.isEmpty()) Note(t.emptyTitle, t.emptyBody)
    store.phrases.forEach { phrase ->
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(t.titles[phrase.context] ?: titleFor(phrase.context) ?: "Practice", color = Pine)
                Text(phrase.text)
                TextButton(onClick = { store.forget(phrase.id) }) { Text(t.forget) }
            }
        }
    }
}

@Composable
private fun Settings(store: CoachStore, t: UiText, health: Health?, checking: Boolean, recheck: () -> Unit,
                     practice: @Composable () -> Unit) {
    var confirmReset by remember { mutableStateOf(false) }
    var url by rememberSaveable { mutableStateOf(store.baseUrl) }
    Text(t.settingsTitle, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    LanguageChoice(store, t)
    Text(t.languageNote, style = MaterialTheme.typography.bodySmall)
    practice()
    Note(t.modelTitle, if (health?.modelInstalled == true) t.modelOn(health.model) else t.modelOff)
    OutlinedTextField(value = url, onValueChange = { url = it.take(200) }, singleLine = true,
        modifier = Modifier.fillMaxWidth(), label = { Text(t.backendLabel) })
    OutlinedButton(onClick = { store.backend(url); url = store.baseUrl; recheck() }, enabled = !checking) { Text(t.saveAndCheck) }
    Note(t.deviceTitle, t.deviceBody)
    OutlinedButton(onClick = { confirmReset = true }) { Text(t.clearData) }
    if (confirmReset) AlertDialog(onDismissRequest = { confirmReset = false }, title = { Text(t.resetTitle) },
        text = { Text(t.resetBody) },
        confirmButton = { TextButton(onClick = { store.reset(); confirmReset = false }) { Text(t.confirmClear) } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(t.keepData) } })
}

@Composable
private fun LanguageChoice(store: CoachStore, t: UiText) {
    Text(t.languageLabel, fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("es" to "Español", "ta" to "தமிழ்", "en" to "English").forEach { (code, label) ->
            FilterChip(selected = store.language == code, onClick = { store.language(code) }, label = { Text(label) })
        }
    }
}

@Composable
internal fun Note(title: String, text: String) {
    Column(Modifier.fillMaxWidth().background(Color(0xFFE8EDE3), RoundedCornerShape(18.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = Pine, fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
