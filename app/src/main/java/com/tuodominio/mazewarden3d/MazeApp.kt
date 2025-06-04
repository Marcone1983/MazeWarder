package com.tuodominio.mazewarden3d

import android.app.Application
import android.content.Context

class MazeApp : Application() {
    
    companion object {
        lateinit var context: Context
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}