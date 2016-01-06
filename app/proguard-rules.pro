-dontwarn **
-target 1.7
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-optimizations !code/simplification/arithmetic,!code/allocation/variable
-keep class **
-keepclassmembers class *{*;}
-keepattributes *

# ensure AndroidPlot classes are not obfuscated
# -keep class com.androidplot.** { *; }

# remove debug error logging messages
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
}
