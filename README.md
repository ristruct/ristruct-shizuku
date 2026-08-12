# RistructShizuku

Production-oriented Shizuku wrapper for Android and Sketchware-based projects.

## Build environment
- JDK 17
- Android SDK platform 36
- Build tools 35.0.0
- Gradle 8.11.1
- Android Gradle Plugin 8.10.1
- Shizuku API/provider 13.1.5

## Public Java API
```java
RistructShizuku.initialize(this, listener);
RistructShizuku.isAvailable();
RistructShizuku.hasPermission();
RistructShizuku.requestPermission();
RistructShizuku.getUid();
RistructShizuku.getBackend();
RistructShizuku.bindUserService(callback);
RistructShizuku.execute(new RistructCommand("id", 5000L), result -> { ... });
RistructShizuku.unbindUserService();
RistructShizuku.destroy();
```

The public API is Java, so Kotlin applications can call it directly without a Kotlin runtime dependency.

## Sketchware
The release workflow also creates:
```
RistructShizuku/
├── res/
├── AndroidManifest.xml
├── classes.dex
├── classes.jar
└── config
```

`config` contains `com.RistructShizuku` to match the Sketchware Master-style format supplied for this project.

## Security and scope
RistructShizuku does not grant root by itself. In ADB mode the effective UID is normally 2000; root mode is UID 0. Android storage and SELinux restrictions still apply.

The library does not implement Google Play Games login, token handling, server bypasses, anti-cheat bypasses, or cloud-save spoofing.


## Sketchware package format

The release workflow creates a Sketchware-style package containing exactly:

```text
RistructShizuku/
├── res/
├── AndroidManifest.xml
├── classes.dex
├── classes.jar
└── config
```

`config` contains `com.RistructShizuku`, matching the library format supplied as the reference. The package also bundles the required Shizuku API/provider/runtime classes so the consumer can call `RistructShizuku.*` from Sketchware Pro `Add source directly` without declaring a Maven dependency.

## Kotlin compatibility

The public API is written in Java with static methods and ordinary interfaces, so Kotlin consumers can call it directly. No Kotlin runtime is required by the library itself.

## Support policy

The project targets Android API 24-36. No Android library can honestly guarantee compatibility with every future Android release forever. Compatibility is tested per release and recorded in `compatibility.json`.
