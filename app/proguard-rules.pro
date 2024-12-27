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

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE


-dontwarn com.squareup.okhttp.**

# restrict obfuscation of data members which has parcelable implemention
-keepclassmembers class * implements android.os.Parcelable {
 static ** CREATOR;
 }

####### RETROFIT ########
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

####### GSON ########
-keepattributes EnclosingMethod
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Preserve generic type information used by Gson
-keepattributes Signature
-keepattributes *Annotation*

# Keep TypeToken information
-keepclassmembers class * {
    *** EMPTY_ARRAY;
}

# Keep generic types in TypeToken or similar usage
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    <fields>;
}

# Retain Gson classes and their members
-keep class com.google.gson.** { *; }

# Keep serialized class members
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class com.iffelse.iastro.** { *; }


-keep class com.iffelse.iastro.model.** {*;}

# Firebase rules

# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# Keep FirebaseMessaging
-keep class com.google.firebase.messaging.** { *; }

# Keep Firebase Auth classes
-keep class com.google.firebase.auth.** { *; }

# Keep Firestore classes
-keep class com.google.firebase.firestore.** { *; }

# Keep Firebase Realtime Database classes
-keep class com.google.firebase.database.** { *; }

# Keep Firebase Storage classes
-keep class com.google.firebase.storage.** { *; }

# Keep Analytics classes
-keep class com.google.firebase.analytics.** { *; }

# Keep Crashlytics classes
-keep class com.google.firebase.crashlytics.** { *; }

# Keep Cloud Functions classes
-keep class com.google.firebase.functions.** { *; }

# Keep Firebase Dynamic Links classes
-keep class com.google.firebase.dynamiclinks.** { *; }

# Keep Firebase In-App Messaging classes
-keep class com.google.firebase.inappmessaging.** { *; }

# Keep Firebase ML Kit classes
-keep class com.google.firebase.ml.** { *; }

# If you are using Google Play Services, add the following rules
-keep class com.google.android.gms.** { *; }

# Keep all Parcelable classes for Firebase
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep class members with the annotation @Keep
-keep @com.google.gson.annotations.SerializedName class * { *; }

-keep class com.razorpay.** {*;}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface
-keepattributes *Annotation*

-dontwarn com.razorpay.**
-keep class com.razorpay.** {*;}

-optimizations !method/inlining/*

-keepclasseswithmembers class * {
  public void onPayment*(...);
}

-keep class com.sceyt.chatuikit.** { *; }
-keep class com.masoudss.** { *; }
-dontwarn com.masoudss.**
