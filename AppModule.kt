package com.buttonautomation.di

import android.content.Context
import androidx.room.Room
import com.buttonautomation.data.database.ButtonAutomationDatabase
import com.buttonautomation.data.database.MacroButtonDao
import com.buttonautomation.data.repository.MacroButtonRepository
import com.buttonautomation.data.repository.MacroButtonRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ButtonAutomationDatabase =
        Room.databaseBuilder(
            context,
            ButtonAutomationDatabase::class.java,
            "button_automation.db"
        ).build()

    @Provides
    fun provideDao(db: ButtonAutomationDatabase): MacroButtonDao = db.macroButtonDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepository(impl: MacroButtonRepositoryImpl): MacroButtonRepository
}
