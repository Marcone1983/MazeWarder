package com.marcone1983.mazewarden3d

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Simplified Application class for Hilt
 * No longer holds static context to prevent memory leaks
 */
@HiltAndroidApp
class MazeApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any global components here
        // Context injection will be handled by Hilt
    }
}