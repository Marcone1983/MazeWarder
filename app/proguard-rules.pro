# MazeWarden 3D ProGuard Rules

# Add project specific ProGuard rules here.
# Keep all classes in the main package
-keep class com.marcone1983.mazewarden3d.** { *; }

# Keep Hilt components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.AndroidEntryPoint
-keep @dagger.hilt.* class * { *; }

# Keep Filament classes
-keep class com.google.android.filament.** { *; }

# Keep ExoPlayer classes  
-keep class com.google.android.exoplayer2.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep data classes and enums
-keep class com.marcone1983.mazewarden3d.model.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize and obfuscate
-optimizationpasses 5
-dontskipnonpubliclibraryclasses
-repackageclasses 'mw'