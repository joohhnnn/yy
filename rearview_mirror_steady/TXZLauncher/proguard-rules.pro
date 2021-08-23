# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}


-keepattributes *Annotation*
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-keep class android.support.**{ *; }
-dontwarn android.support.**

-keep class android.app.**{ *; }
-dontwarn android.app.**

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

-keep class com.bumptech.** { *; }
-dontwarn com.bumptech.**

-keep class com.android.support.** { *; }
-dontwarn com.android.support.**

-keep class jp.wasabeef.** { *; }
-dontwarn jp.wasabeef.**


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.**{ *; }

# Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { *; }

##---------------End: proguard configuration for Gson  ----------

#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}


-keep class com.amap.** {*;}
-dontwarn com.amap.**
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}
-keep class com.autonavi.** {*;}
-dontwarn com.autonavi.**

-keep class com.txznet.comm.remote.GlobalContext {*;}


-keep class com.txznet.comm.ui.IKeepClass{
}
-keep class * implements com.txznet.comm.ui.IKeepClass {
	public <methods>;
}

-keep public class com.txznet.comm.**{
	public <fields>;
    public <methods>;
	protected <methods>;
}

-keep public class com.txznet.loader.**{
	public <fields>;
    public <methods>;
}

-keep public class com.txznet.txz.util.**{
	public <fields>;
    public <methods>;
}

-keep public class  com.txznet.txz.util.runnables.**{
	public <fields>;
    public <methods>;
	protected <fields>;
}


-keep public class com.txz.**{*;}

-keep public class com.txz.ui.voice.**{
	public <fields>;
	public <methods>;
}

-keep class com.txznet.loader.** { *; }
-dontwarn com.txznet.comm.remote.util.**
-keepclassmembers class com.txznet.comm.base.BaseApplication {
	*;
}

-keep class taobe.tec.jcc.** {*; }

-keep class com.alibaba.fastjson.** {*; }

-keep class com.nostra13.universalimageloader.** { *; }

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
    public <fields>;
}

-keep public class com.txznet.sdk.bean.** {
    void set*(***);
    void set*(int,***);
    boolean is*();
    boolean is*(int);
    *** get*();
    *** get*(int);
}

-keep public class com.txznet.sdk.** {
    public <fields>;
    public <methods>;
}

-keep class retrofit2.** {*;}
-dontwarn retrofit2.**
-keep class org.reactivestreams.** {*;}
-dontwarn org.reactivestreams.**
-keep class io.reactivex.** {*;}
-dontwarn io.reactivex.**

# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain service method parameters.
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-keep class okio.** {*;}
-keep class okhttp3.** {*;}
-keep class javax.annotation.** {*;}
-keep class org.conscrypt.** {*;}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keep class org.apache.** {*;}
-dontwarn  org.apache.**

-keep public class com.txznet.launcher.domain.music.bean.** { *;}
-keep class com.txznet.launcher.data.entity.** {*;}

# 系统ota升级用到了gson去解释bean
-keep class com.txznet.launcher.domain.upgrade.bean.** { *;}