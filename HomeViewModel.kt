package com.buttonautomation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonautomation.domain.model.MacroButton
import com.buttonautomation.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val buttons: List<MacroButton> = emptyList(),
    val isLoading: Boolean = false,
    val executingButtonId: String? = null,
    val lastReport: ExecutionReport? = null,
    val showReport: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllButtons: GetAllButtonsUseCase,
    private val deleteButton: DeleteButtonUseCase,
    private val executeButton: ExecuteButtonUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllButtons().collect { buttons ->
                _uiState.update { it.copy(buttons = buttons, isLoading = false) }
            }
        }
    }

    fun onExecuteButton(button: MacroButton) {
        viewModelScope.launch {
            _uiState.update { it.copy(executingButtonId = button.id) }
            val report = executeButton(button)
            _uiState.update {
                it.copy(
                    executingButtonId = null,
                    lastReport = report,
                    showReport = true
                )
            }
        }
    }

    fun onDeleteButton(id: String) {
        viewModelScope.launch {
            deleteButton(id)
        }
    }

    fun dismissReport() {
        _uiState.update { it.copy(showReport = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
