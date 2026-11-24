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

# TopOn (AnyThink / Secmtp) Core & Mediation
-keep class com.secmtp.sdk.** { *; }
-dontwarn com.secmtp.sdk.**
-keep class com.anythink.** { *; }
-dontwarn com.anythink.**
-keepattributes EnclosingMethod, InnerClasses, Signature, *Annotation*

# Meta Audience Network
-keep class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**

# Liftoff Monetize (Vungle)
-keep class com.vungle.ads.** { *; }
-dontwarn com.vungle.ads.**
-keep class com.iab.omid.library.vungle.** { *; }
-dontwarn com.iab.omid.library.vungle.**


