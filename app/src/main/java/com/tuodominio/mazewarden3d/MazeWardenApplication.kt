package com.tuodominio.mazewarden3d

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * MazeWarden3D Application class with Hilt setup
 * 
 * This class initializes the entire dependency injection framework
 * and can be used for global app-level initialization like:
 * - Filament engine setup
 * - ExoPlayer configuration
 * - Analytics/Crash reporting
 * - Performance monitoring
 */
@HiltAndroidApp
class MazeWardenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize any global libraries here
        initializeFilament()
        setupCrashReporting()
        configurePerformanceMonitoring()
    }

    private fun initializeFilament() {
        // Filament engine initialization can be done here if needed
        // Usually it's better to initialize per-activity for memory management
    }

    private fun setupCrashReporting() {
        // Initialize crash reporting (Firebase Crashlytics, Bugsnag, etc.)
        // Example: FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    private fun configurePerformanceMonitoring() {
        // Setup performance monitoring for 3D rendering
        // Track FPS, memory usage, battery consumption
    }

    override fun onTerminate() {
        super.onTerminate()
        // Cleanup global resources if any
    }
}