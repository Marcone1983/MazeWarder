package com.marcone1983.mazewarden3d.di

import android.content.Context
import com.marcone1983.mazewarden3d.SaveSystem
import com.marcone1983.mazewarden3d.repository.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideGameRepository(
        @ApplicationContext context: Context,
        saveSystem: SaveSystem
    ): GameRepository {
        return GameRepository(context, saveSystem)
    }
}