# APEX Calculator - R8 rules
# Release builds keep minification disabled by default (see docs/RELEASE.md), these rules
# document the keeps that would be required when minification is enabled.

# Room generated implementations are referenced reflectively by the runtime.
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# Kotlin metadata used by reflection-free Compose runtime.
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations

# Keep application models that are serialized into the local database.
-keep class com.shohankhan.apexcalculator.core.data.** { *; }
