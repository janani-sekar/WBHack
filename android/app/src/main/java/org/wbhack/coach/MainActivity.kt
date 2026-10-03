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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Pine = Color(0xFF21594D)
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
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val scenario = store.scenarios.find { it.id == selected }
    BackHandler(enabled = selected != null) { selected = null }
    Scaffold(bottomBar = {
        if (store.welcomed && selected == null) NavigationBar {
            listOf("Practice", "My phrases", "Settings").forEachIndexed { index, label ->
                val icon = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Settings)[index]
                NavigationBarItem(selected = tab == index, onClick = { tab = index },
                    icon = { Icon(icon, contentDescription = null) }, label = { Text(label) })
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("HOSPITALITY COACH", color = Pine, fontSize = 12.sp, letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold)
            when {
                !store.welcomed -> Welcome(store)
                scenario != null -> key(scenario.id) { Session(store, scenario) { selected = null } }
                tab == 1 -> Phrases(store)
                tab == 2 -> Settings(store)
                else -> {
                    Text(if (store.tamil) "வணக்கம்!" else "Welcome back.", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text("A little practice.\nA more confident welcome.", style = MaterialTheme.typography.headlineMedium)
                    Note("Works without internet", "Practice cards and your saved phrases stay on this device. AI is not connected yet.")
                    Text("${store.practiceCount} practice sessions · ${store.completed.size} of 3 scenarios explored",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Your next conversation", style = MaterialTheme.typography.titleLarge)
                    store.scenarios.sortedBy { it.id in store.completed }.forEachIndexed { index, item ->
                        Card(onClick = { selected = item.id }, modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("0${index + 1}   /   ${if (item.id in store.completed) "PRACTICE AGAIN" else "START HERE"}", color = Pine, fontSize = 12.sp)
                                Text(item.title, style = MaterialTheme.typography.titleLarge)
                                Text(item.guest)
                                Text("Open practice →", color = Pine, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Text("Fictional farm scenarios · English guest dialogue · Tamil coaching drafts",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun Welcome(store: CoachStore) {
    Spacer(Modifier.height(24.dp))
    Text("Make every\nwelcome count.", fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold)
    Text("Try a guest conversation, compare your reply with a helpful example, then try again.",
        style = MaterialTheme.typography.titleMedium)
    LanguageChoice(store)
    Note("A space to practise", "Nothing is sent to a guest. Only phrases you choose to remember are saved. This is a shared-device prototype: anyone with access to this app can see saved phrases.")
    Note("Early preview", "This version uses fixed practice cards, not an AI model. Tamil text is a draft awaiting a local-language reviewer.")
    Button(onClick = store::welcome, modifier = Modifier.fillMaxWidth()) { Text("Start practising") }
}

@Composable
private fun Session(store: CoachStore, scenario: Scenario, close: () -> Unit) {
    var response by rememberSaveable { mutableStateOf("") }
    var reviewed by rememberSaveable { mutableStateOf(false) }
    var novel by rememberSaveable { mutableStateOf(false) }
    var counted by rememberSaveable { mutableStateOf(false) }
    var saveDialog by rememberSaveable { mutableStateOf(false) }
    var phrase by rememberSaveable { mutableStateOf("") }
    val engine = remember { ReferencePracticeEngine() }
    TextButton(onClick = close) { Text("← All scenarios") }
    Text(scenario.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Text(if (novel) "A fresh situation" else "Imagine your guest asks…", color = Pine)
    Card(colors = CardDefaults.cardColors(containerColor = Pine)) {
        Text(if (novel) scenario.unseenGuest else scenario.guest, Modifier.padding(24.dp),
            color = Color.White, style = MaterialTheme.typography.titleLarge)
    }
    Text("Facts you can rely on", fontWeight = FontWeight.Bold)
    Text("Standard tour: 60 minutes\nCoffee tasting: 20 minutes\nMeeting point: farm entrance beside the blue gate\nPrices and availability: not confirmed")
    Text("These are fictional demo facts.", style = MaterialTheme.typography.bodySmall)
    OutlinedTextField(value = response, onValueChange = { response = it.take(2000); reviewed = false },
        modifier = Modifier.fillMaxWidth(), minLines = 4, label = { Text("Your reply to the guest") },
        supportingText = { Text("${response.length}/2000 · This draft is not saved to disk") })
    val saved = store.phrases.filter { it.context == scenario.id }
    saved.forEach { item ->
        TextButton(onClick = { response = item.text; reviewed = false }) { Text("Use saved phrase: ${item.text}") }
    }
    Button(onClick = { reviewed = true }, enabled = response.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
        Text("Compare with practice guide")
    }
    if (reviewed) {
        val coaching = engine.coach(scenario, response, store.tamil)
        Note("One thing to practise", coaching.suggestion)
        Text("Self-check", style = MaterialTheme.typography.titleMedium)
        scenario.rubric.forEach { Text("• $it") }
        Note("An example reply", coaching.example)
        Text("Fixed reference guidance—not an assessment of your response.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { reviewed = false }, modifier = Modifier.fillMaxWidth()) { Text("Improve my reply") }
        OutlinedButton(onClick = { phrase = response; saveDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Remember my phrase…") }
        if (!novel) TextButton(onClick = { novel = true; response = ""; reviewed = false }) { Text("Try a new version of this situation") }
        Button(onClick = {
            if (!counted) { store.complete(scenario.id); counted = true }
            close()
        }, modifier = Modifier.fillMaxWidth()) { Text("Finish practice") }
    }
    if (saveDialog) AlertDialog(onDismissRequest = { saveDialog = false }, title = { Text("Remember this phrase?") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Edit before approving. It will stay on this device and appear in this scenario. Do not include private guest details.")
            OutlinedTextField(value = phrase, onValueChange = { phrase = it.take(2000) }, label = { Text("Approved wording") })
        } }, confirmButton = { TextButton(enabled = phrase.isNotBlank(), onClick = { store.approve(phrase, scenario.id); saveDialog = false }) { Text("Approve & save") } },
        dismissButton = { TextButton(onClick = { saveDialog = false }) { Text("Cancel") } })
}

@Composable
private fun Phrases(store: CoachStore) {
    Text("Words that work\nfor you.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Text("Only wording you approve appears here. Reuse it in practice, or remove it at any time.")
    if (store.phrases.isEmpty()) Note("Your phrasebook is ready", "After a practice round, choose ‘Remember my phrase’ to add your own wording.")
    store.phrases.forEach { phrase ->
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(store.scenarios.find { it.id == phrase.context }?.title ?: "Practice", color = Pine)
                Text(phrase.text)
                TextButton(onClick = { store.forget(phrase.id) }) { Text("Forget phrase") }
            }
        }
    }
}

@Composable
private fun Settings(store: CoachStore) {
    var confirmReset by remember { mutableStateOf(false) }
    Text("Make it yours.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    LanguageChoice(store)
    Note("On this phone", "Approved phrases, language preference and practice counts are stored locally. Replies are not saved to disk. Android backup is disabled. App locking and separate operator profiles are not implemented.")
    Note("Model status", "No model installed. The offline practice guide uses fixed examples. Generated coaching, free-form translation and review analysis still need an on-device model integration.")
    Text("Tamil coaching is draft content, not validated language support. Guest prompts and navigation are in English.")
    OutlinedButton(onClick = { confirmReset = true }) { Text("Clear phrases and progress…") }
    if (confirmReset) AlertDialog(onDismissRequest = { confirmReset = false }, title = { Text("Clear local learning data?") },
        text = { Text("This removes all approved phrases and practice counts on this phone. It cannot be undone.") },
        confirmButton = { TextButton(onClick = { store.reset(); confirmReset = false }) { Text("Clear data") } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Keep data") } })
}

@Composable
private fun LanguageChoice(store: CoachStore) {
    Text("Coaching language", fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterChip(selected = store.tamil, onClick = { store.language(true) }, label = { Text("தமிழ்") })
        FilterChip(selected = !store.tamil, onClick = { store.language(false) }, label = { Text("English") })
    }
}

@Composable
private fun Note(title: String, text: String) {
    Column(Modifier.fillMaxWidth().background(Color(0xFFE8EDE3), RoundedCornerShape(18.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = Pine, fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
