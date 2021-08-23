# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-injars bin\txzcomm.jar
-injars libs\protobuf-java-2.3.0-nano.jar
-injars libs\fastjson-1.1.34.android.jar
-injars libs\jcc-bate-0.7.3.jar
-injars libs\universal-image-loader-1.9.5.jar
-injars libs\zxing.jar
#-injars libs\

-outjars bin\TXZ_SDK.jar


#-libraryjars lib_3rd\android.jar
-libraryjars lib_3rd\

-assumenosideeffects class com.txznet.comm.ui.**{*;}
-dontwarn com.txznet.comm.ui.**
-dontwarn android.media.**

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-target 1.6

-obfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt


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

-libraryjars ./libs/android-support-v4.jar
-keep class * extends android.support.v4.app.FragmentActivity 
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-keep public class * extends com.google.protobuf.nano.MessageNano



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

-renamesourcefileattribute Proguard
-keepattributes SourceFile,LineNumberTable


