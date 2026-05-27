package com.buttonautomation.domain.usecase

import com.buttonautomation.data.repository.MacroButtonRepository
import com.buttonautomation.domain.model.MacroButton
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllButtonsUseCase @Inject constructor(
    private val repository: MacroButtonRepository
) {
    operator fun invoke(): Flow<List<MacroButton>> = repository.getAllButtons()
}

class SaveButtonUseCase @Inject constructor(
    private val repository: MacroButtonRepository
) {
    suspend operator fun invoke(button: MacroButton) = repository.saveButton(button)
}

class DeleteButtonUseCase @Inject constructor(
    private val repository: MacroButtonRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteButton(id)
}

class GetButtonByIdUseCase @Inject constructor(
    private val repository: MacroButtonRepository
) {
    suspend operator fun invoke(id: String): MacroButton? = repository.getButtonById(id)
}

class ExecuteButtonUseCase @Inject constructor(
    private val actionEngine: ActionEngine
) {
    suspend operator fun invoke(button: MacroButton): ExecutionReport =
        actionEngine.executeButton(button)
}
