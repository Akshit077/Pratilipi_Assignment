package com.example.pratilipiassignment.di

import android.content.Context
import androidx.room.Room
import com.example.pratilipiassignment.data.local.AppDatabase
import com.example.pratilipiassignment.data.local.DocumentDao
import com.example.pratilipiassignment.data.repository.DocumentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "pratilipi.db").build()

    @Provides
    @Singleton
    fun provideDocumentDao(database: AppDatabase): DocumentDao = database.documentDao()

    @Provides
    @Singleton
    fun provideDocumentRepository(dao: DocumentDao): DocumentRepository = DocumentRepository(dao)
}
