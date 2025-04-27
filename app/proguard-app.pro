# 忽略警告
#-ignorewarning

# 混淆保护自己项目的部分代码以及引用的第三方jar包
#-libraryjars libs/xxxxxxxxx.jar

# 不混淆这个包下的类
-keep class com.hjq.demo.http.api.** {
    <fields>;
}
-keep class com.hjq.demo.http.response.** {
    <fields>;
}
-keep class com.hjq.demo.http.model.** {
    <fields>;
}

# 不混淆被 Log 注解的方法信息
-keepclassmembernames class ** {
    @com.hjq.demo.aop.Log <methods>;
}
# jpush----------------------------------------------------
-dontoptimize
-dontpreverify
-keepattributes  EnclosingMethod,Signature
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

-dontwarn cn.jmessage.**
-keep class cn.jmessage.**{ *; }

-keepclassmembers class ** {
    public void onEvent*(**);
}

# ========================gson================================
-dontwarn com.google.**
-keep class com.google.gson.** {*;}

# ========================protobuf================================
-keep class com.google.protobuf.** {*;}
# baidu map---------------------------------------------------------
-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-keep class com.baidu.vi.** {*;}
-dontwarn com.baidu.**
# ===========================smack======================
# Keep all classes and methods in Smack
-keep class org.jivesoftware.** { *; }
-keep class org.jxmpp.** { *; }
-keep class org.minidns.** { *; }

# Keep all public and protected methods in Smack
-keepclassmembers class org.jivesoftware.** {
    public protected *;
}
-keepclassmembers class org.jxmpp.** {
    public protected *;
}
-keepclassmembers class org.minidns.** {
    public protected *;
}
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
# Kotlin 协程
-keep class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keep class kotlinx.coroutines.Dispatchers { *; }
