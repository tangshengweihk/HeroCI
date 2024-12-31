package com.heroesports.heroci.di

import android.content.Context
import com.heroesports.heroci.data.AppDatabase
import com.heroesports.heroci.data.dao.CheckInDao
import com.heroesports.heroci.data.dao.MemberDao
import com.heroesports.heroci.data.dao.ProjectDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    fun provideMemberDao(database: AppDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    fun provideCheckInDao(database: AppDatabase): CheckInDao {
        return database.checkInDao()
    }
} 