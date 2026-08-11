# Keep Android entry points and media callbacks used through reflection/framework APIs.
-keep class com.gsxgamer.c2004.** extends android.app.Service { *; }
-keep class android.support.v4.media.** { *; }
