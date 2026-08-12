# Public API
-keep public class com.ristruct.shizuku.** {
    public *;
}

# UserService is loaded by class name through Shizuku UserServiceArgs.
-keep class com.ristruct.shizuku.service.RistructUserService { *; }
-keep class com.ristruct.shizuku.service.IRistructUserService { *; }
-keep class com.ristruct.shizuku.service.IRistructUserService$Stub { *; }
-keep class com.ristruct.shizuku.service.IRistructUserService$Stub$Proxy { *; }

# Keep the Shizuku provider/runtime classes bundled by the Sketchware exporter.
-keep class rikka.shizuku.** { *; }
