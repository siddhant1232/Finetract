package com.finetract.di

import android.content.Context
import androidx.room.Room
import com.finetract.data.local.AppDatabase
import com.finetract.data.local.dao.FinanceDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finetract_db"
        ).build()
    }

    @Provides
    fun provideFinanceDao(database: AppDatabase): FinanceDao {
        return database.financeDao()
    }
}
