package com.buttonautomation.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.buttonautomation.domain.model.Action
import com.buttonautomation.domain.model.MacroButton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class ActionResult(
    val action: Action,
    val success: Boolean,
    val errorMessage: String? = null
)

data class ExecutionReport(
    val buttonName: String,
    val results: List<ActionResult>,
    val totalActions: Int,
    val successCount: Int
) {
    val failCount get() = totalActions - successCount
    val allSucceeded get() = failCount == 0
}

@Singleton
class ActionEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun executeButton(button: MacroButton): ExecutionReport {
        val results = mutableListOf<ActionResult>()

        for (action in button.actions) {
            val result = executeAction(action)
            results.add(result)

            if (!result.success) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "⚠️ Fehler bei: ${action.label}\n${result.errorMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return ExecutionReport(
            buttonName = button.name,
            results = results,
            totalActions = button.actions.size,
            successCount = results.count { it.success }
        )
    }

    private suspend fun executeAction(action: Action): ActionResult {
        return try {
            when (action) {
                is Action.OpenApp -> executeOpenApp(action)
                is Action.OpenUrl -> executeOpenUrl(action)
                is Action.WebSearch -> executeWebSearch(action)
                is Action.OpenSettings -> executeOpenSettings(action)
                is Action.Delay -> executeDelay(action)
                is Action.OpenFile -> executeOpenFile(action)
                is Action.SendIntent -> executeSendIntent(action)
                is Action.ShareText -> executeShareText(action)
            }
        } catch (e: Exception) {
            ActionResult(action, false, e.message ?: "Unbekannter Fehler")
        }
    }

    // ── Open App ──────────────────────────────────────────────────────────────

    private fun executeOpenApp(action: Action.OpenApp): ActionResult {
        if (action.packageName.isBlank()) {
            return ActionResult(action, false, "Kein Package-Name angegeben")
        }
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(action.packageName)
            ?: return ActionResult(action, false, "App nicht gefunden: ${action.packageName}")

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return ActionResult(action, true)
    }

    // ── Open URL ─────────────────────────────────────────────────────────────

    private fun executeOpenUrl(action: Action.OpenUrl): ActionResult {
        if (action.url.isBlank()) {
            return ActionResult(action, false, "Keine URL angegeben")
        }
        val url = if (action.url.startsWith("http://") || action.url.startsWith("https://")) {
            action.url
        } else {
            "https://${action.url}"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(action, true)
    }

    // ── Web Search ────────────────────────────────────────────────────────────

    private fun executeWebSearch(action: Action.WebSearch): ActionResult {
        if (action.query.isBlank()) {
            return ActionResult(action, false, "Kein Suchbegriff angegeben")
        }
        val searchUri = Uri.parse("https://www.google.com/search?q=${Uri.encode(action.query)}")
        val intent = Intent(Intent.ACTION_VIEW, searchUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        // Try browser intent first
        try {
            val webIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", action.query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            context.startActivity(intent)
        }
        return ActionResult(action, true)
    }

    // ── Open Settings ─────────────────────────────────────────────────────────

    private fun executeOpenSettings(action: Action.OpenSettings): ActionResult {
        val intent = Intent(action.settingsType.androidAction).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(action, true)
    }

    // ── Delay ─────────────────────────────────────────────────────────────────

    private suspend fun executeDelay(action: Action.Delay): ActionResult {
        delay(action.milliseconds)
        return ActionResult(action, true)
    }

    // ── Open File / Gallery ───────────────────────────────────────────────────

    private fun executeOpenFile(action: Action.OpenFile): ActionResult {
        val intent = when (action.mimeType) {
            "image/*" -> Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            "video/*" -> Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
                type = "video/*"
            }
            else -> Intent(Intent.ACTION_GET_CONTENT).apply {
                type = action.mimeType
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val chooser = Intent.createChooser(intent, "Datei öffnen").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return ActionResult(action, true)
    }

    // ── Send Custom Intent ────────────────────────────────────────────────────

    private fun executeSendIntent(action: Action.SendIntent): ActionResult {
        if (action.action.isBlank()) {
            return ActionResult(action, false, "Keine Intent-Action angegeben")
        }
        val intent = Intent(action.action).apply {
            if (action.data.isNotBlank()) {
                data = Uri.parse(action.data)
            }
            for ((key, value) in action.extras) {
                putExtra(key, value)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(action, true)
    }

    // ── Share Text ────────────────────────────────────────────────────────────

    private fun executeShareText(action: Action.ShareText): ActionResult {
        if (action.text.isBlank()) {
            return ActionResult(action, false, "Kein Text zum Teilen angegeben")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, action.text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Teilen via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return ActionResult(action, true)
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    fun getInstalledApps(): List<Pair<String, String>> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            .map { info ->
                val appName = info.loadLabel(pm).toString()
                val packageName = info.activityInfo.packageName
                Pair(appName, packageName)
            }
            .sortedBy { it.first }
    }
}
