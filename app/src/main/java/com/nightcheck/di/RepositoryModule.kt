package com.nightcheck.di

import com.nightcheck.data.repository.NoteRepositoryImpl
import com.nightcheck.data.repository.ReminderRepositoryImpl
import com.nightcheck.data.repository.TaskRepositoryImpl
import com.nightcheck.domain.repository.NoteRepository
import com.nightcheck.domain.repository.ReminderRepository
import com.nightcheck.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
