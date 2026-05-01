package com.nightcheck.di

import android.content.Context
import androidx.room.Room
import com.nightcheck.data.local.NightcheckDatabase
import com.nightcheck.data.local.dao.NoteDao
import com.nightcheck.data.local.dao.ReminderDao
import com.nightcheck.data.local.dao.TaskDao
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
    fun provideDatabase(@ApplicationContext context: Context): NightcheckDatabase =
        Room.databaseBuilder(
            context,
            NightcheckDatabase::class.java,
            NightcheckDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Replace with proper migrations in production
            .build()

    @Provides
    fun provideTaskDao(db: NightcheckDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideNoteDao(db: NightcheckDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideReminderDao(db: NightcheckDatabase): ReminderDao = db.reminderDao()
}
