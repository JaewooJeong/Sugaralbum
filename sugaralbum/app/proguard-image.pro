# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontpreverify
-verbose

-keepattributes *Annotation*, Exceptions,InnerClasses,Signature

#-libraryjars ../libs/android-support-v13.jar
-libraryjars /Projects/Projects_SM/cozycamera/cozycamera/imageFrameworkLibrary/libs/jackson-core-asl-1.9.12.jar
-libraryjars /Projects/Projects_SM/cozycamera/cozycamera/imageFrameworkLibrary/libs/jackson-mapper-asl-1.9.12.jar
-libraryjars /Projects/Projects_SM/cozycamera/cozycamera/imageFrameworkLibrary/libs/httpmime-4.2.5.jar
-libraryjars /Projects/Projects_SM/cozycamera/cozycamera/imageFrameworkLibrary/libs/renderscript-v8.jar


-dontwarn com.kiwiple.imageframework.filter.IFilterService
-dontwarn com.kiwiple.imageframework.filter.IFilterServiceVcreation
-dontwarn com.kiwiple.imageframework.filter.IFilterServiceLgu
-dontwarn org.codehaus.jackson.map.ext.**
-dontwarn org.apache.http.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class lg.uplusbox.photo.** {*;}

-keepnames class org.codehaus.jackson.**
-dontwarn org.w3c.dom.**

-keepclassmembers class * {
    static final *;
}

# field
-keep public class * {
    public *;
}

# Parcelable
-keep class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
