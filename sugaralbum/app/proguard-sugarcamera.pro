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

-keepattributes *Annotation*, Exceptions,InnerClasses,Signature,SourceFile,LineNumberTable

-libraryjars /Users/jaewoo/cozycamera/cozycamera/cozycamera/libs/json-simple-1.1.1.jar
-libraryjars /Users/jaewoo/cozycamera/cozycamera/cozycamera/libs/sun.misc.BASE64Decoder.jar
-libraryjars /Users/jaewoo/cozycamera/cozycamera/cozycamera/libs/das-android-child-1.3.4.jar
-libraryjars /Users/jaewoo/cozycamera/cozycamera/ImageFrameworkLibrary/libs/sd-sdk-device-info.jar
-libraryjars /Users/jaewoo/cozycamera/cozycamera/ImageFrameworkLibrary/libs/sd-sdk-facial-processing.jar
#-libraryjars /PlusCameraMultimediaFramework/MultimediaFramework/ImageAnalysisEngine/ImageFrameworkLibrary/libs/android-support-v13.jar
-libraryjars /Users/jaewoo/cozycamera/cozycamera/ImageFrameworkLibrary/libs/jackson-core-asl-1.9.12.jar
-libraryjars /Users/jaewoo/cozycamera/cozycamera/ImageFrameworkLibrary/libs/jackson-mapper-asl-1.9.12.jar
-libraryjars /Users/jaewoo/cozycamera/cozycamera/ImageFrameworkLibrary/libs/httpmime-4.2.5.jar
-libraryjars /Users/jaewoo/cozycamera/cozycamera/VideoEngine/libs/kiwi-debug.jar
-libraryjars /Users/jaewoo/Library/Android/sdk/build-tools/33.0.0/renderscript/lib/renderscript-v8.jar

-dontwarn com.kiwiple.imageframework.filter.IFilterService
-dontwarn com.kiwiple.imageframework.filter.IFilterServiceVcreation
-dontwarn com.kiwiple.imageframework.filter.IFilterServiceLgu
-dontwarn com.kiwiple.mediaframework.ffmpeg.IFFmpegService
-dontwarn org.codehaus.jackson.map.ext.**
-dontwarn org.apache.http.**
-dontwarn org.w3c.dom.**
-dontwarn com.lge.concierge.sdk.**
-dontwarn com.google.android.gms.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.support.v8.renderscript.ScriptC

-keep class com.lg.das.** {*;}
-keep class android.** {*;}
-keep class lg.uplusbox.photo.** {*;}
-keep class com.sugarmount.sugarcamera.** {*;}
-keep class com.kiwiple.imageframework.filter.** {*;}
-keep class com.kiwiple.mediaframework.data.MediaFormatJNI {*;}
-keep class com.kiwiple.mediaframework.muxer.KwpMuxerJni {*;}
-keep class com.kiwiple.mediaframework.ffmpeg.** {*;}
-keep class com.kiwiple.imageframework.view.ScalableViewController {*;}

-keepnames class org.codehaus.jackson.**
-keepnames class com.kiwiple.multimedia.canvas.** extends com.kiwiple.multimedia.canvas.ICanvasUser

-keepclassmembers class * {
    public static final *;
    native <methods>;
}

-keepclassmembers class com.kiwiple.multimedia.canvas.** {
	<init>(***);
}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
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
