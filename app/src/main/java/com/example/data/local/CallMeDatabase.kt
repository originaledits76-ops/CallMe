package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ReminderEntity
import com.example.data.model.ReminderHistoryEntity

@Database(
    entities = [ReminderEntity::class, ReminderHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CallMeDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CallMeDatabase? = null

        fun getInstance(context: Context): CallMeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallMeDatabase::class.java,
                    "callme_reminders.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
