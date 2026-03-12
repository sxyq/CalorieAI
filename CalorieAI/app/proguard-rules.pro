# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room entities
-keep class com.calorieai.app.data.model.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.** { *; }
