package org.wbhack.coach

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Phrase(val id: String, val text: String, val context: String, val approvedAt: Long)

class CoachStore(context: Context) {
    private val prefs = context.getSharedPreferences("coach", Context.MODE_PRIVATE)
    var tamil by mutableStateOf(prefs.getBoolean("tamil", true)); private set
    var welcomed by mutableStateOf(prefs.getBoolean("welcomed", false)); private set
    var phrases by mutableStateOf(readPhrases()); private set
    var practiceCount by mutableStateOf(prefs.getInt("practiceCount", 0)); private set
    var completed by mutableStateOf(prefs.getStringSet("completed", emptySet())!!.toSet()); private set
    val scenarios: List<Scenario> = context.assets.open("scenarios.json").bufferedReader().use {
        val entries = JSONArray(it.readText())
        (0 until entries.length()).map { index ->
            val item = entries.getJSONObject(index)
            val rubric = item.getJSONArray("rubric")
            Scenario(item.getString("id"), item.getString("guest"),
                (0 until rubric.length()).map { n -> rubric.getString(n) }, item.getString("held_out_guest"))
        }
    }
    fun language(value: Boolean) { tamil = value; prefs.edit().putBoolean("tamil", value).apply() }
    fun welcome() { welcomed = true; prefs.edit().putBoolean("welcomed", true).apply() }
    fun complete(id: String) {
        practiceCount++
        completed = completed + id
        prefs.edit().putInt("practiceCount", practiceCount).putStringSet("completed", completed).apply()
    }
    fun approve(text: String, context: String) {
        if (text.isBlank() || phrases.any { it.text == text.trim() && it.context == context }) return
        phrases = phrases + Phrase(UUID.randomUUID().toString(), text.trim(), context, System.currentTimeMillis())
        persistPhrases()
    }
    fun forget(id: String) { phrases = phrases.filterNot { it.id == id }; persistPhrases() }
    fun reset() {
        phrases = emptyList(); completed = emptySet(); practiceCount = 0
        prefs.edit().remove("phrases").remove("completed").remove("practiceCount").apply()
    }
    private fun readPhrases(): List<Phrase> = runCatching {
        val rows = JSONArray(prefs.getString("phrases", "[]"))
        (0 until rows.length()).map { i -> rows.getJSONObject(i).let {
            Phrase(it.getString("id"), it.getString("text"), it.getString("context"), it.getLong("approvedAt"))
        } }
    }.getOrDefault(emptyList())
    private fun persistPhrases() {
        val rows = JSONArray()
        phrases.forEach { rows.put(JSONObject().put("id", it.id).put("text", it.text)
            .put("context", it.context).put("approvedAt", it.approvedAt).put("version", 1)) }
        prefs.edit().putString("phrases", rows.toString()).apply()
    }
}
