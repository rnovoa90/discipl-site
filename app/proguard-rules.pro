# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.discipl.app.data.model.** { *; }
-keep class com.discipl.app.service.** { *; }

# PostHog
-keep class com.posthog.** { *; }

# RevenueCat
-keep class com.revenuecat.** { *; }

# Superwall
-keep class com.superwall.** { *; }
