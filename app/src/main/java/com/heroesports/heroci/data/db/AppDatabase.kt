package com.heroesports.heroci.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heroesports.heroci.data.dao.CheckInDao
import com.heroesports.heroci.data.dao.MemberDao
import com.heroesports.heroci.data.dao.ProjectDao
import com.heroesports.heroci.data.entity.CheckIn
import com.heroesports.heroci.data.entity.Member
import com.heroesports.heroci.data.entity.Project
import com.heroesports.heroci.data.converter.Converters
import java.io.File

@Database(
    entities = [Project::class, Member::class, CheckIn::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun memberDao(): MemberDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        private const val MAIN_DB_NAME = "main.db"

        @Volatile
        private var mainInstance: AppDatabase? = null

        private val projectDatabases = mutableMapOf<Long, AppDatabase>()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 如果需要，在这里添加迁移逻辑
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 如果需要，在这里添加迁移逻辑
            }
        }

        fun getMainDatabase(context: Context): AppDatabase {
            return mainInstance ?: synchronized(this) {
                mainInstance ?: buildDatabase(context, MAIN_DB_NAME).also { mainInstance = it }
            }
        }

        fun getProjectDatabase(context: Context, projectId: Long): AppDatabase {
            return projectDatabases[projectId] ?: synchronized(this) {
                projectDatabases[projectId] ?: buildDatabase(
                    context,
                    "project_${projectId}.db"
                ).also { projectDatabases[projectId] = it }
            }
        }

        fun closeProjectDatabase(projectId: Long) {
            synchronized(this) {
                projectDatabases[projectId]?.close()
                projectDatabases.remove(projectId)
            }
        }

        fun deleteProjectDatabase(context: Context, projectId: Long) {
            synchronized(this) {
                closeProjectDatabase(projectId)
                // 删除数据库文件
                val dbFile = context.getDatabasePath("project_${projectId}.db")
                if (dbFile.exists()) {
                    dbFile.delete()
                }
                // 删除数据库相关文件
                context.getDatabasePath("project_${projectId}.db-shm").delete()
                context.getDatabasePath("project_${projectId}.db-wal").delete()
                // 删除相关的照片文件
                val projectDir = File(context.filesDir, "project_$projectId")
                if (projectDir.exists()) {
                    projectDir.deleteRecursively()
                }
            }
        }

        private fun buildDatabase(context: Context, dbName: String): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, dbName)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }
    }
} 