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

# Koin rules
-keep class org.koin.** { *; }
