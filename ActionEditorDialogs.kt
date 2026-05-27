package com.buttonautomation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.buttonautomation.domain.model.*

// ─── Action Type Picker ───────────────────────────────────────────────────────

@Composable
fun ActionTypePicker(
    onSelect: (ActionType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Aktion hinzufügen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                ActionType.entries.forEach { type ->
                    ActionTypeRow(type = type, onClick = { onSelect(type); onDismiss() })
                }
            }
        }
    }
}

@Composable
private fun ActionTypeRow(type: ActionType, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(type.emoji, fontSize = 26.sp)
        Column {
            Text(type.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Generic Action Editor dispatcher ─────────────────────────────────────────

@Composable
fun ActionEditorDialog(
    action: Action,
    onSave: (Action) -> Unit,
    onDismiss: () -> Unit,
    installedApps: List<Pair<String, String>> = emptyList(),
    showAppPicker: Boolean = false,
    onRequestAppPicker: () -> Unit = {}
) {
    when (action) {
        is Action.OpenApp -> OpenAppEditor(action, onSave, onDismiss, installedApps, showAppPicker, onRequestAppPicker)
        is Action.OpenUrl -> OpenUrlEditor(action, onSave, onDismiss)
        is Action.WebSearch -> WebSearchEditor(action, onSave, onDismiss)
        is Action.OpenSettings -> OpenSettingsEditor(action, onSave, onDismiss)
        is Action.Delay -> DelayEditor(action, onSave, onDismiss)
        is Action.OpenFile -> OpenFileEditor(action, onSave, onDismiss)
        is Action.SendIntent -> SendIntentEditor(action, onSave, onDismiss)
        is Action.ShareText -> ShareTextEditor(action, onSave, onDismiss)
    }
}

// ─── Open App Editor ──────────────────────────────────────────────────────────

@Composable
private fun OpenAppEditor(
    action: Action.OpenApp,
    onSave: (Action) -> Unit,
    onDismiss: () -> Unit,
    installedApps: List<Pair<String, String>>,
    showAppPicker: Boolean,
    onRequestAppPicker: () -> Unit
) {
    var label by remember { mutableStateOf(action.label) }
    var packageName by remember { mutableStateOf(action.packageName) }
    var searchQuery by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(showAppPicker) }

    if (showPicker) {
        AppPickerDialog(
            apps = installedApps,
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onSelect = { name, pkg ->
                label = "App öffnen: $name"
                packageName = pkg
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    } else {
        EditorDialog(title = "📱 App öffnen", onSave = { onSave(action.copy(label = label, packageName = packageName)) }, onDismiss = onDismiss) {
            OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = packageName, onValueChange = { packageName = it },
                label = { Text("Package Name (z.B. com.spotify.music)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showPicker = true; onRequestAppPicker() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Installierte App wählen")
            }
        }
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<Pair<String, String>>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelect: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("App wählen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery, onValueChange = onSearchChange,
                    label = { Text("Suche...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                val filtered = apps.filter {
                    it.first.contains(searchQuery, ignoreCase = true) ||
                    it.second.contains(searchQuery, ignoreCase = true)
                }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered) { (name, pkg) ->
                        ListItem(
                            headlineContent = { Text(name, fontWeight = FontWeight.Medium) },
                            supportingContent = { Text(pkg, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.clickable { onSelect(name, pkg) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.3f))
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Abbrechen") }
            }
        }
    }
}

// ─── Open URL Editor ──────────────────────────────────────────────────────────

@Composable
private fun OpenUrlEditor(action: Action.OpenUrl, onSave: (Action) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(action.label) }
    var url by remember { mutableStateOf(action.url) }
    EditorDialog(title = "🌐 URL öffnen", onSave = { onSave(action.copy(label = label, url = url)) }, onDismiss = onDismiss) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = url, onValueChange = { url = it },
            label = { Text("URL (z.B. https://youtube.com)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        Spacer(Modifier.height(4.dp))
        Text("Tipp: Ohne https:// wird es automatisch ergänzt.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Web Search Editor ────────────────────────────────────────────────────────

@Composable
private fun WebSearchEditor(action: Action.WebSearch, onSave: (Action) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(action.label) }
    var query by remember { mutableStateOf(action.query) }
    EditorDialog(title = "🔍 Websuche", onSave = { onSave(action.copy(label = label, query = query)) }, onDismiss = onDismiss) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text("Suchbegriff") }, modifier = Modifier.fillMaxWidth(), singleLine = true
        )
    }
}

// ─── Open Settings Editor ─────────────────────────────────────────────────────

@Composable
private fun OpenSettingsEditor(action: Action.OpenSettings, onSave: (Action) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(action.label) }
    var selected by remember { mutableStateOf(action.settingsType) }
    var expanded by remember { mutableStateOf(false) }

    EditorDialog(title = "⚙️ Einstellungen öffnen", onSave = { onSave(action.copy(label = label, settingsType = selected)) }, onDismiss = onDismiss) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected.displayName, onValueChange = {},
                readOnly = true, label = { Text("Einstellungstyp") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SettingsType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = { selected = type; label = "Einstellungen: ${type.displayName}"; expanded = false }
                    )
                }
            }
        }
    }
}

// ─── Delay Editor ─────────────────────────────────────────────────────────────

@Composable
private fun DelayEditor(action: Action.Delay, onSave: (Action) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(action.label) }
    var seconds by remember { mutableStateOf((action.milliseconds / 1000.0).toString()) }
    EditorDialog(title = "⏱️ Pause / Delay", onSave = {
        val ms = ((seconds.toDoubleOrNull() ?: 1.0) * 1000).toLong().coerceAtLeast(100)
        onSave(action.copy(label = label, milliseconds = ms))
    }, onDismiss = onDismiss) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = seconds, onValueChange = { seconds = it },
            label = { Text("Sekunden") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("s") }
        )
        Spacer(Modifier.height(4.dp))
        Text("Mindest-Pause: 0.1 Sekunden", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Open File Editor ─────────────────────────────────────────────────────────

@Composable
private fun OpenFileEditor(action: Action.OpenFile, onSave: (Action) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(action.label) }
    var mimeType by remember { mutableStateOf(action.mimeType) }
    var expanded by remember { mutableStateOf(false) }

    val mimeOptions = listOf(
        "Bilder (Galerie)" to "image/*",
        "Videos" to "video/*",
        "Audio" to "audio/*",
        "PDF" to "application/pdf",
        "Alle Dateien" to "*/*"
    )

    EditorDialog(title = "📁 Datei / Galerie", onSave = { onSave(action.copy(label = label, mimeType = mimeType)) }, onDismiss = onDismiss) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = mimeOptions.find { it.second == mimeType }?.first ?: mimeType,
                onValueChange = {},
                readOnly = true, label = { Text("Dateityp") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                mimeOptions.forEach { (name, mime) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = { mimeType = mime; label = "Öffne: $name"; expanded = false }
                    )
                }
            }
        }
    }
}

// ─── Send Intent Editor ───────────────────────────────────────────────────────

@Composable
private fun SendIntentEditor(action: Action.SendIntent, onSave: (Action) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(action.label) }
    var intentAction by remember { mutableStateOf(action.action) }
    var data by remember { mutableStateOf(action.data) }

    EditorDialog(title = "📡 Intent senden", onSave = {
        onSave(action.copy(label = label, action = intentAction, data = data))
    }, onDismiss = onDismiss) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = intentAction, onValueChange = { intentAction = it },
            label = { Text("Intent Action (z.B. android.intent.action.CALL)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = data, onValueChange = { data = it },
            label = { Text("Data URI (optional, z.B. tel:+491234)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(4.dp))
        Text("Beispiel: android.intent.action.DIAL + tel:+491234567890", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Share Text Editor ────────────────────────────────────────────────────────

@Composable
private fun ShareTextEditor(action: Action.ShareText, onSave: (Action) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(action.label) }
    var text by remember { mutableStateOf(action.text) }
    EditorDialog(title = "📤 Text teilen", onSave = { onSave(action.copy(label = label, text = text)) }, onDismiss = onDismiss) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = text, onValueChange = { text = it },
            label = { Text("Zu teilender Text") }, modifier = Modifier.fillMaxWidth(),
            minLines = 3, maxLines = 6
        )
    }
}

// ─── Shared Dialog Shell ──────────────────────────────────────────────────────

@Composable
private fun EditorDialog(
    title: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                content()
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Abbrechen") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onSave, shape = RoundedCornerShape(12.dp)) {
                        Text("Speichern", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
