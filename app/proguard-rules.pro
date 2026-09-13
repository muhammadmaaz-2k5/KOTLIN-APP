# ProGuard & R8 Optimization Rules for ENGORA / Nazaarabox

# --- Android Core & App Entry Points ---
-keep public class com.job2day.nazaarabox.NazaaraboxApplication { *; }
-keep public class com.job2day.nazaarabox.MainActivity { *; }
-keep public class com.job2day.nazaarabox.services.** { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# --- AndroidX Startup & Initializers ---
-dontwarn androidx.startup.**
-keep class androidx.startup.** { *; }
-keep class * implements androidx.startup.Initializer {
    public <init>();
    <fields>;
    <methods>;
}

# --- AndroidX WorkManager & Room Database ---
-dontwarn androidx.work.**
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.InputMerger { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class * extends androidx.work.impl.WorkDatabase { *; }

-dontwarn androidx.room.**
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase {
    public static <fields>;
    <init>();
}
-keep class * extends androidx.room.RoomDatabase

-dontwarn androidx.sqlite.**
-keep class androidx.sqlite.** { *; }

# --- Preserve Data Models & JSON Serialization ---
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.job2day.nazaarabox.core.** { *; }
-keep class com.job2day.nazaarabox.model.** { *; }
-keep class com.job2day.nazaarabox.data.** { *; }
-keepclassmembers class com.job2day.nazaarabox.core.** { *; }
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# --- Retrofit 2 ---
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# --- Gson ---
-keepclassmembers enum * { *; }
-keepclassmembers class * implements java.io.Serializable { *; }

# --- Kotlinx Serialization ---
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    companion object;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Google Mobile Ads (AdMob) & UMP ---
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}
-keep class com.google.android.gms.ads.mediation.** { *; }
-keep class com.google.android.ump.** { *; }

# --- OneSignal Push Notifications ---
-dontwarn com.onesignal.**
-keep class com.onesignal.** { *; }

# --- Firebase Messaging & Analytics ---
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }

# --- Coil Image Loading ---
-dontwarn coil3.**
-keep class coil3.** { *; }

# --- Compose & Coroutines & Lifecycle ---
-dontwarn androidx.compose.**
-dontwarn kotlinx.coroutines.**
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
