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
-dontobfuscate
#-dontshrink
-keep class com.unnamed.b.atv.**{*;}
#-keep class sun1.security.**
-keep public class com.mrikso.apkrepacker.ui.projectview.FolderHolder
-keep public class com.mrikso.apkrepacker.ui.projectview.FolderHolder$TreeItem
-keepclassmembers class com.mrikso.apkrepacker.ui.projectview.FolderHolder{
public *;
private *;
}
-keep class sun1.security.x509.**{*;}
-keep class com.android.apksig.**{*;}
-keep class com.google.common.**{*;}
-keep class org.jf.dexlib2.**{*;}