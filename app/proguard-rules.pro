-keep class top.yukonga.miuix.kmp.** { *; }
-dontwarn top.yukonga.miuix.kmp.**
-keep class me.freepps.tile.** { *; }
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
