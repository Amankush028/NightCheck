package com.nightcheck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nightcheck.data.local.dao.NoteDao
import com.nightcheck.data.local.dao.ReminderDao
import com.nightcheck.data.local.dao.TaskDao
import com.nightcheck.data.local.entity.NoteEntity
import com.nightcheck.data.local.entity.ReminderEntity
import com.nightcheck.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        NoteEntity::class,
        ReminderEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class NightcheckDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        const val DATABASE_NAME = "nightcheck.db"
    }
}