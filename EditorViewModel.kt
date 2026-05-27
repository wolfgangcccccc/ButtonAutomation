package com.buttonautomation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonautomation.domain.model.Action
import com.buttonautomation.domain.model.MacroButton
import com.buttonautomation.domain.usecase.ActionEngine
import com.buttonautomation.domain.usecase.GetButtonByIdUseCase
import com.buttonautomation.domain.usecase.SaveButtonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditorUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val emoji: String = "⚡",
    val colorHex: String = "#6200EE",
    val actions: List<Action> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val editingActionId: String? = null,
    val installedApps: List<Pair<String, String>> = emptyList(),
    val showAppPicker: Boolean = false,
    val showColorPicker: Boolean = false,
    val showEmojiPicker: Boolean = false
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val getButtonById: GetButtonByIdUseCase,
    private val saveButton: SaveButtonUseCase,
    private val actionEngine: ActionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun loadButton(id: String?) {
        if (id == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val button = getButtonById(id)
            if (button != null) {
                _uiState.update {
                    it.copy(
                        id = button.id,
                        name = button.name,
                        emoji = button.emoji,
                        colorHex = button.colorHex,
                        actions = button.actions,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = actionEngine.getInstalledApps()
            _uiState.update { it.copy(installedApps = apps) }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onEmojiChange(emoji: String) = _uiState.update { it.copy(emoji = emoji) }
    fun onColorChange(hex: String) = _uiState.update { it.copy(colorHex = hex) }

    fun addAction(action: Action) {
        _uiState.update { it.copy(actions = it.actions + action) }
    }

    fun updateAction(updatedAction: Action) {
        _uiState.update { state ->
            state.copy(actions = state.actions.map {
                if (it.id == updatedAction.id) updatedAction else it
            })
        }
    }

    fun deleteAction(actionId: String) {
        _uiState.update { state ->
            state.copy(actions = state.actions.filter { it.id != actionId })
        }
    }

    fun moveActionUp(actionId: String) {
        _uiState.update { state ->
            val actions = state.actions.toMutableList()
            val idx = actions.indexOfFirst { it.id == actionId }
            if (idx > 0) {
                val tmp = actions[idx - 1]
                actions[idx - 1] = actions[idx]
                actions[idx] = tmp
            }
            state.copy(actions = actions)
        }
    }

    fun moveActionDown(actionId: String) {
        _uiState.update { state ->
            val actions = state.actions.toMutableList()
            val idx = actions.indexOfFirst { it.id == actionId }
            if (idx < actions.size - 1) {
                val tmp = actions[idx + 1]
                actions[idx + 1] = actions[idx]
                actions[idx] = tmp
            }
            state.copy(actions = actions)
        }
    }

    fun setEditingAction(id: String?) = _uiState.update { it.copy(editingActionId = id) }
    fun setShowAppPicker(show: Boolean) = _uiState.update { it.copy(showAppPicker = show) }
    fun setShowColorPicker(show: Boolean) = _uiState.update { it.copy(showColorPicker = show) }
    fun setShowEmojiPicker(show: Boolean) = _uiState.update { it.copy(showEmojiPicker = show) }

    fun saveButton() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Bitte gib dem Button einen Namen") }
            return
        }
        viewModelScope.launch {
            saveButton(
                MacroButton(
                    id = state.id,
                    name = state.name.trim(),
                    emoji = state.emoji,
                    colorHex = state.colorHex,
                    actions = state.actions
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
