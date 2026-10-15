# Room rules
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao

# Kotlin Serialization rules
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable ** write$Self(...);
}

# Ktor rules
-keep class io.ktor.** { *; }
-dontwarn java.lang.management.**

# Koin rules
-keep class org.koin.** { *; }

# EncryptedSharedPreferences / Tink rules
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.crypto.tink.**
