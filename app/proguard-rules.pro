# StreamHub TV - ProGuard rules

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.streamhub.tv.**$$serializer { *; }
-keepclassmembers class com.streamhub.tv.** { *** Companion; }
-keepclasseswithmembers class com.streamhub.tv.** { kotlinx.serialization.KSerializer serializer(...); }

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit
-keepattributes Signature, RuntimeVisibleAnnotations
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep data models (used for JSON parsing / Room entities)
-keep class com.streamhub.tv.data.model.** { *; }
-keep class com.streamhub.tv.data.local.** { *; }
