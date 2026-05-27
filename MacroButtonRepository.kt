package com.buttonautomation.data.repository

import com.buttonautomation.data.database.MacroButtonDao
import com.buttonautomation.data.database.toDomain
import com.buttonautomation.data.database.toEntity
import com.buttonautomation.domain.model.MacroButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface MacroButtonRepository {
    fun getAllButtons(): Flow<List<MacroButton>>
    suspend fun getButtonById(id: String): MacroButton?
    suspend fun saveButton(button: MacroButton)
    suspend fun deleteButton(id: String)
}

@Singleton
class MacroButtonRepositoryImpl @Inject constructor(
    private val dao: MacroButtonDao
) : MacroButtonRepository {

    override fun getAllButtons(): Flow<List<MacroButton>> =
        dao.getAllButtons().map { list -> list.map { it.toDomain() } }

    override suspend fun getButtonById(id: String): MacroButton? =
        dao.getButtonById(id)?.toDomain()

    override suspend fun saveButton(button: MacroButton) =
        dao.insertButton(button.toEntity())

    override suspend fun deleteButton(id: String) =
        dao.deleteButtonById(id)
}
