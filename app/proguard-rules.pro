# ProGuard rules for MummyFlix

# Keep ExoPlayer classes
-keep class com.google.android.exoplayer2.** { *; }
-keep class com.google.android.exoplayer2.**$* { *; }
-dontwarn com.google.android.exoplayer2.**

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Keep Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Material3
-keep class androidx.material3.** { *; }
-dontwarn androidx.material3.**

# Keep TV Material
-keep class androidx.tv.** { *; }
-dontwarn androidx.tv.**

# Keep Navigation
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Keep application class
-keep class com.mummyflix.MummyFlixApplication { *; }

# Keep ViewModels
-keep class com.mummyflix.viewmodel.** { *; }

# Keep Models
-keep class com.mummyflix.model.** { *; }

# Keep Repository
-keep class com.mummyflix.repository.** { *; }

# Keep Preferences
-keep class com.mummyflix.preferences.** { *; }

# Keep UI components
-keep class com.mummyflix.ui.** { *; }

# Keep R class
-keep class com.mummyflix.R
-keep class com.mummyflix.R$* { *; }

# Keep BuildConfig
-keep class com.mummyflix.BuildConfig { *; }

# Optimization
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}