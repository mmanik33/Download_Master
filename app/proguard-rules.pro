# ProGuard / R8 rules for Download Master

# Preserve line numbers and attributes
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Preserve all native methods and JNI bindings
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve youtubedl-android, FFmpeg, and aria2c libraries completely
-keep class com.yausername.** { *; }
-keepclassmembers class com.yausername.** { *; }
-dontwarn com.yausername.**

-keep class io.github.junkfood02.** { *; }
-keepclassmembers class io.github.junkfood02.** { *; }
-dontwarn io.github.junkfood02.**

# Preserve Jackson Databind and annotations used by youtubedl-android
-keep class com.fasterxml.jackson.** { *; }
-keepclassmembers class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* <fields>;
    @com.fasterxml.jackson.annotation.* <methods>;
}

# Preserve Apache Commons IO used by youtubedl-android ZipUtils
-keep class org.apache.commons.io.** { *; }
-keepclassmembers class org.apache.commons.io.** { *; }
-dontwarn org.apache.commons.io.**

# Preserve Application classes, Services, Models, and ViewModels
-keep class com.example.** { *; }
-keepclassmembers class com.example.** { *; }

-keep class com.example.service.DownloadForegroundService { *; }
-keep class com.example.DownloadMasterApp { *; }
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** { *; }

# AndroidX Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi & Retrofit
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-dontwarn com.squareup.moshi.**
-dontwarn retrofit2.**

# Jetpack Compose & Kotlin Coroutines
-keepclassmembers class androidx.compose.ui.** { *; }
-dontwarn kotlinx.coroutines.**
