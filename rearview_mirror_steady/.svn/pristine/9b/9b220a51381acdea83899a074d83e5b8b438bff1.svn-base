# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-injars bin\txzcomm.jar
-injars libs\fastjson-1.1.34.android.jar
-injars libs\jcc-bate-0.7.3.jar
-libraryjars libs\universal-image-loader-1.9.5.jar
-libraryjars libs\protobuf-java-2.3.0-nano.jar
-libraryjars libs\zxing.jar
#-injars libs\
-outjars bin\TXZ_UI.jar

#-libraryjars lib_3rd\android.jar
-libraryjars lib_3rd\

-assumenosideeffects class com.txznet.comm.ui.**{*;}
-dontwarn com.txznet.comm.ui.**

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-target 1.6

-printusage bin\proguard_usage.txt
-printmapping bin\proguard_mapping.txt
-printseeds bin\proguard_seeds.txt

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,LocalVariable*Table,*Annotation*,Synthetic,EnclosingMethod
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

-keep public class com.txznet.txz.comm.R$* {*;}
-dontwarn com.txznet.txz.comm.R*

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**


# Custom
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

-keep public class com.txznet.txz.util.recordcenter.TXZAudioRecorder {
    public <methods>;
}


-keep class com.txznet.comm.ui.IKeepClass{
}
-keep class * implements com.txznet.comm.ui.IKeepClass {
	public <methods>;
}

-dontwarn com.txznet.comm.base.**
-dontwarn com.txznet.record.setting.**

-keep public class com.txznet.comm.ui.**{
	public <fields>;
    public <methods>;
}

-keep public class com.txznet.comm.util.**{
	public <fields>;
    public <methods>;
}

-keep public class com.txznet.comm.remote.**{
	public <fields>;
    public <methods>;
}

-keep public class com.txznet.comm.notification.**{
	public <fields>;
    public <methods>;
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

-keep public class com.txz.ui.voice.**{
	public <fields>;
	public <methods>;
}

-keep public class com.txznet.record.setting.**{
	public <fields>;
	public <methods>;
}

-renamesourcefileattribute Proguard
-keepattributes SourceFile,LineNumberTable


