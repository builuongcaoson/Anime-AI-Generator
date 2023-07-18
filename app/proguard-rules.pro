# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#----- Retrofit -----
-dontnote okhttp3.**, okio.**, retrofit2.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
#---------------End: Retrofit  ----------

#----- Revenuecat -----
-keep class com.revenuecat.purchases.** { *; }
#---------------End: Revenuecat  ----------

#----- Firebase Realtime Database -----
# Add this global rule
-keepattributes Signature
# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models.
# Modify this rule to fit the structure of your app.
-keepclassmembers class com.sola.anime.ai.generator.domain.model.** {
   *;
}
#---------------End: Firebase Realtime Database  ----------

### Work around admob crash. Only reproduced on release APK on emulator or certain devices
-keep class com.google.android.gms.internal.ads.** { *; }
### End workaround