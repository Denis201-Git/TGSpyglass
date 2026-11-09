# Keep rules for TGParser
-keep class com.example.tgparser.model.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# kotlinx.serialization
-keep class kotlinx.serialization.** { *; }

# Go (gomobile)
-keep class go.** { *; }
-keep class mobile.** { *; }

# Strip Log.d in release (PII)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(java.lang.String, java.lang.String);
    public static java.lang.String getStackTraceString(java.lang.Throwable);
}
