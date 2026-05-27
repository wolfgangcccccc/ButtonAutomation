package com.buttonautomation.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroButtonDao {

    @Query("SELECT * FROM macro_buttons ORDER BY createdAt DESC")
    fun getAllButtons(): Flow<List<MacroButtonEntity>>

    @Query("SELECT * FROM macro_buttons WHERE id = :id")
    suspend fun getButtonById(id: String): MacroButtonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: MacroButtonEntity)

    @Update
    suspend fun updateButton(button: MacroButtonEntity)

    @Delete
    suspend fun deleteButton(button: MacroButtonEntity)

    @Query("DELETE FROM macro_buttons WHERE id = :id")
    suspend fun deleteButtonById(id: String)
}
