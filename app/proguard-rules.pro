-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Классы-мосты гейта (GateBridge/CloseBridge) и их члены сохраняем целиком.
-keep class iknow.yesino.app.presentation.gate.** { *; }

# WebView и его JS-интерфейс.
-keepclassmembers class android.webkit.WebView {
    public *;
}

-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }

# Зберігаємо класи Room, від яких залежить WorkManager
-keep class androidx.room.** { *; }

# Зберігаємо конструктори ваших Worker-ів, щоб Koin міг їх створювати
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Прибираємо зайві попередження в логах збірки
-dontwarn androidx.work.**
-dontwarn androidx.room.**

# Захищаємо всі класи для kotlinx.serialization (наприклад, роути Compose Navigation)
-keep @kotlinx.serialization.Serializable class * { *; }

# Захищаємо згенеровані серіалізатори
-keep class *$$serializer { *; }

# Зберігаємо атрибути для коректної роботи рефлексії
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod