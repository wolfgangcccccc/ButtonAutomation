package com.buttonautomation.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MacroButtonEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ButtonAutomationDatabase : RoomDatabase() {
    abstract fun macroButtonDao(): MacroButtonDao
}
