# kotlinx.serialization keeps generated serializers reachable
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
# Dados persistidos (data/) e payload do link (share/).
-keepclassmembers class com.whiteelephant.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.whiteelephant.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# O SDK do AdMob arrasta o WorkManager, que roda sobre Room. O Room carrega a
# implementação gerada do banco via Class.forName("<Database>_Impl"), uma
# referência que o R8 não enxerga — sem estas regras o app morre no start com
# "Failed to create an instance of androidx.work.impl.WorkDatabase".
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <fields>;
}
