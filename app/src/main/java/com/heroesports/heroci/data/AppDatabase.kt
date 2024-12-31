package com.heroesports.heroci.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heroesports.heroci.data.dao.CheckInDao
import com.heroesports.heroci.data.dao.MemberDao
import com.heroesports.heroci.data.dao.ProjectDao
import com.heroesports.heroci.data.entity.CheckIn
import com.heroesports.heroci.data.entity.Member
import com.heroesports.heroci.data.entity.Project
import com.heroesports.heroci.data.util.Converters

@Database(
    entities = [Project::class, Member::class, CheckIn::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun memberDao(): MemberDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 