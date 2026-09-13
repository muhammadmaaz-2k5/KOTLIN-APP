# Proguard / R8 Optimization Rules for ENGORA (com.job2day)

# 1. General Attributes & Line Numbers for Crash Reporting
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 2. Kotlinx Serialization
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# 3. App Core Data Models & API Services
-keep class com.job2day.nazaarabox.core.models.** { *; }
-keep class com.job2day.nazaarabox.model.** { *; }
-keep class com.job2day.nazaarabox.data.** { *; }
-keep class com.job2day.nazaarabox.presentation.** { *; }
-keep class com.job2day.nazaarabox.utils.** { *; }

# 4. Retrofit & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# 5. Gson (if needed by converters)
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 6. Coil 3 Image Loader
-keep class io.coil.** { *; }
-keep class coil3.** { *; }
-dontwarn io.coil.**
-dontwarn coil3.**

# 7. Google Mobile Ads (AdMob) & UMP SDK
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-keep public class com.google.android.ump.** {
   public *;
}

# 8. Google Firebase (Analytics & Cloud Messaging)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# 9. OneSignal SDK
-dontwarn com.onesignal.**
-keep class com.onesignal.** { *; }

# 10. Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
