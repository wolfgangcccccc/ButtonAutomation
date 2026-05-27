package com.buttonautomation.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buttonautomation.domain.model.*
import com.buttonautomation.presentation.components.*
import com.buttonautomation.presentation.viewmodel.EditorViewModel

val PRESET_COLORS = listOf(
    "#7C4DFF", "#6200EE", "#00BCD4", "#00E676",
    "#FF6D00", "#FF1744", "#F50057", "#FFEA00",
    "#40C4FF", "#69FF87", "#FF6E40", "#EA80FC"
)

val PRESET_EMOJIS = listOf(
    "⚡", "🚀", "🎵", "🌐", "📱", "🔍", "🎮", "📂",
    "⚙️", "🔔", "❤️", "⭐", "🔥", "💡", "🛡️", "🎯"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    buttonId: String?,
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load existing button
    LaunchedEffect(buttonId) {
        if (buttonId != null) viewModel.loadButton(buttonId)
        viewModel.loadInstalledApps()
    }

    // Navigate back after save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    var showActionTypePicker by remember { mutableStateOf(false) }
    var editingAction by remember { mutableStateOf<Action?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (buttonId == null) "Neuer Button" else "Button bearbeiten",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Zurück")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveButton() },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Speichern", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Preview Card ──────────────────────────────────────────────────
            item {
                ButtonPreviewCard(
                    name = uiState.name,
                    emoji = uiState.emoji,
                    colorHex = uiState.colorHex
                )
            }

            // ── Name Input ────────────────────────────────────────────────────
            item {
                SectionCard(title = "Button Name") {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Name des Buttons") },
                        placeholder = { Text("z.B. Music Mode, Browse Mode...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.error != null && uiState.name.isBlank()
                    )
                }
            }

            // ── Emoji Picker ──────────────────────────────────────────────────
            item {
                SectionCard(title = "Emoji") {
                    EmojiGrid(
                        selected = uiState.emoji,
                        onSelect = viewModel::onEmojiChange
                    )
                }
            }

            // ── Color Picker ──────────────────────────────────────────────────
            item {
                SectionCard(title = "Farbe") {
                    ColorGrid(
                        selected = uiState.colorHex,
                        onSelect = viewModel::onColorChange
                    )
                }
            }

            // ── Actions Section ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Aktionen (${uiState.actions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilledTonalButton(
                        onClick = { showActionTypePicker = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hinzufügen")
                    }
                }
            }

            if (uiState.actions.isEmpty()) {
                item {
                    EmptyActionsHint()
                }
            }

            // ── Action Items ──────────────────────────────────────────────────
            itemsIndexed(uiState.actions, key = { _, action -> action.id }) { index, action ->
                ActionListItem(
                    action = action,
                    index = index,
                    total = uiState.actions.size,
                    onEdit = { editingAction = action },
                    onDelete = { viewModel.deleteAction(action.id) },
                    onMoveUp = { viewModel.moveActionUp(action.id) },
                    onMoveDown = { viewModel.moveActionDown(action.id) }
                )
            }

            item { Spacer(Modifier.height(60.dp)) }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showActionTypePicker) {
        ActionTypePicker(
            onSelect = { type ->
                val newAction = createDefaultAction(type)
                editingAction = newAction
            },
            onDismiss = { showActionTypePicker = false }
        )
    }

    editingAction?.let { action ->
        ActionEditorDialog(
            action = action,
            installedApps = uiState.installedApps,
            onSave = { saved ->
                if (uiState.actions.any { it.id == saved.id }) {
                    viewModel.updateAction(saved)
                } else {
                    viewModel.addAction(saved)
                }
                editingAction = null
            },
            onDismiss = { editingAction = null }
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Fehler") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun createDefaultAction(type: ActionType): Action = when (type) {
    ActionType.OPEN_APP -> Action.OpenApp()
    ActionType.OPEN_URL -> Action.OpenUrl()
    ActionType.WEB_SEARCH -> Action.WebSearch()
    ActionType.OPEN_SETTINGS -> Action.OpenSettings()
    ActionType.DELAY -> Action.Delay()
    ActionType.OPEN_FILE -> Action.OpenFile()
    ActionType.SEND_INTENT -> Action.SendIntent()
    ActionType.SHARE_TEXT -> Action.ShareText()
}

// ─── Sub-Components ───────────────────────────────────────────────────────────

@Composable
private fun ButtonPreviewCard(name: String, emoji: String, colorHex: String) {
    val color = remember(colorHex) {
        try { Color(android.graphics.Color.parseColor(colorHex)) }
        catch (e: Exception) { Color(0xFF7C4DFF) }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = BorderStroke(1.5.dp, color.copy(0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(0.25f), CircleShape)
                    .border(2.dp, color.copy(0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji.ifBlank { "⚡" }, fontSize = 26.sp)
            }
            Text(
                text = name.ifBlank { "Button Name..." },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) { content() }
        }
    }
}

@Composable
private fun EmojiGrid(selected: String, onSelect: (String) -> Unit) {
    Column {
        PRESET_EMOJIS.chunked(8).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (emoji == selected) MaterialTheme.colorScheme.primary.copy(0.3f) else Color.Transparent)
                            .border(
                                if (emoji == selected) 2.dp else 0.dp,
                                if (emoji == selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ColorGrid(selected: String, onSelect: (String) -> Unit) {
    Column {
        PRESET_COLORS.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { hex ->
                    val color = remember(hex) {
                        try { Color(android.graphics.Color.parseColor(hex)) }
                        catch (e: Exception) { Color.Gray }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                if (hex == selected) 3.dp else 0.dp,
                                Color.White,
                                CircleShape
                            )
                            .clickable { onSelect(hex) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ActionListItem(
    action: Action,
    index: Int,
    total: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Step number
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Action emoji + label
            Text(actionEmoji(action), fontSize = 18.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    action.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    actionSubtitle(action),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reorder
            Column {
                IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, null, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onMoveDown, enabled = index < total - 1, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(18.dp))
                }
            }

            // Edit
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }

            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun actionEmoji(action: Action): String = when (action) {
    is Action.OpenApp -> "📱"
    is Action.OpenUrl -> "🌐"
    is Action.WebSearch -> "🔍"
    is Action.OpenSettings -> "⚙️"
    is Action.Delay -> "⏱️"
    is Action.OpenFile -> "📁"
    is Action.SendIntent -> "📡"
    is Action.ShareText -> "📤"
}

private fun actionSubtitle(action: Action): String = when (action) {
    is Action.OpenApp -> action.packageName.ifBlank { "Kein Package gewählt" }
    is Action.OpenUrl -> action.url.ifBlank { "Keine URL" }
    is Action.WebSearch -> action.query.ifBlank { "Kein Suchbegriff" }
    is Action.OpenSettings -> action.settingsType.displayName
    is Action.Delay -> "${action.milliseconds / 1000.0}s Pause"
    is Action.OpenFile -> action.mimeType
    is Action.SendIntent -> action.action.ifBlank { "Keine Action" }
    is Action.ShareText -> action.text.take(40).ifBlank { "Kein Text" }
}

@Composable
private fun EmptyActionsHint() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("➕", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Noch keine Aktionen",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Füge Aktionen hinzu, die beim Drücken des Buttons nacheinander ausgeführt werden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
