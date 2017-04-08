# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android_SDK\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
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

#网易云信混淆配置 start
-dontwarn com.netease.**
-dontwarn io.netty.**
-keep class com.netease.** {*;}
#如果 netty 使用的官方版本，它中间用到了反射，因此需要 keep。如果使用的是我们提供的版本，则不需要 keep
#-keep class io.netty.** {*;}

#如果你使用全文检索插件，需要加入
#-dontwarn org.apache.lucene.**
#-keep class org.apache.lucene.** {*;}
#网易云信混淆配置 end
