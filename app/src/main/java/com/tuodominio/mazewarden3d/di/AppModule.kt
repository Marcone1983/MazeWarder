package com.tuodominio.mazewarden3d.di

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
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
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val player = ExoPlayer.Builder(context).build()
        val uri = android.net.Uri.parse("asset:///raw/background_music.mp3")
        player.setMediaItem(MediaItem.fromUri(uri))
        player.isLooping = true
        player.prepare()
        return player
    }

    // Puoi aggiungere altre dipendenze: Retrofit, Room, SharedPreferences ecc.
}