# gdx-sketchware — LibGDX Local Library สำหรับ Sketchware Pro

แพ็ก LibGDX (เวอร์ชัน 1.14.2 — เสถียรล่าสุด ณ พ.ค. 2026) ให้เป็นไฟล์ `.aar`
ก้อนเดียว ครบทุกอย่าง (คลาส libGDX + native .so ทุก ABI) เพื่อ import เป็น
**Local Library** ใน Sketchware Pro ได้ตรงๆ ไม่ต้องพึ่ง Maven ตอน build ในเครื่อง

## โครงสร้าง
```
gdx-sketchware/
├── gdxgamelib/                        <- โมดูลไลบรารีที่ build ออกมาเป็น .aar
│   ├── consumer-rules.pro             <- กัน proguard ตัดคลาส libGDX ทิ้ง
│   └── src/main/
│       ├── AndroidManifest.xml        <- คำแนะนำการเซ็ตอัปฝั่ง Sketchware Pro
│       ├── assets/badlogic.jpg        <- รูปตัวอย่าง (ฝังมากับ .aar อัตโนมัติ)
│       └── java/com/sketchgdx/gamelib/
│           ├── GdxGame.java           <- entry point (Game), setScreen(...)
│           ├── MenuScreen.java        <- ตัวอย่างเมนู
│           ├── PlayScreen2D.java      <- ตัวอย่าง gameplay 2D
│           ├── PlayScreen3D.java      <- ตัวอย่าง gameplay 3D
│           ├── BaseGame.java          <- เดโมสั้นๆ แบบคลาสเดียว (ทางเลือก)
│           └── GdxGameActivity.java   <- Activity ที่ Sketchware Pro เปิดผ่าน Intent
├── gdxgamelib-examples/KotlinGame.kt  <- ตัวอย่างเขียนเกมด้วย Kotlin
└── .github/workflows/build-aar.yml    <- GitHub Actions build ให้อัตโนมัติ
```

## ขั้นตอนที่ 1: ให้ GitHub Actions build ไฟล์ .aar ให้
1. สร้าง repo บน GitHub แล้ว push โฟลเดอร์นี้ทั้งหมดขึ้นไป
2. ไปที่แท็บ **Actions** ของ repo → workflow "Build gdxgamelib.aar" จะรันเอง
   (หรือกด "Run workflow" เพื่อรันมือ)
3. เมื่อรันเสร็จ (สีเขียว) เปิด run นั้น เลื่อนลงไปที่ **Artifacts**
   ดาวน์โหลด `gdxgamelib-aar` → แตกไฟล์ zip จะได้ `gdxgamelib-release.aar`

นี่คือไฟล์เดียวที่ต้องใช้ในขั้นตอนถัดไป

## ขั้นตอนที่ 2: Import เข้า Sketchware Pro
1. เปิดโปรเจกต์ใน Sketchware Pro → **Library Manager** (ไอคอนถัง/หนังสือใน
   หน้า Design หรือเมนู 3 จุด แล้วแต่เวอร์ชัน) → **Add Local Library**
2. เลือกไฟล์ `gdxgamelib-release.aar` ที่ดาวน์โหลดมา
3. เปิด **AndroidManifest.xml editor** ของโปรเจกต์ แล้วเพิ่ม activity นี้เข้าไป
   ในแท็ก `<application>` (Sketchware Pro **ไม่** merge manifest ของ local
   library ให้อัตโนมัติ ต้องเพิ่มเองครั้งเดียว):
   ```xml
   <activity
       android:name="com.sketchgdx.gamelib.GdxGameActivity"
       android:configChanges="orientation|keyboardHidden|screenSize"
       android:screenOrientation="landscape"
       android:exported="true" />
   ```
4. เอารูป/เสียง/ฟอนต์ของเกมไปวางที่ Assets ของโปรเจกต์ Sketchware Pro เอง
   (Project → Assets Manager) แล้วเรียกด้วย path ปกติผ่าน `AssetManager`
   ในโค้ด Java/Kotlin ของคุณ ไม่ต้องตั้งค่าอะไรเพิ่ม

## ขั้นตอนที่ 3: เปิดเกม (วิธีที่แนะนำ — เสถียรที่สุด)
เก็บ MainActivity ไว้เป็นหน้าแรกของแอปตามปกติ (อย่าไปแก้ manifest ส่วนนั้น
เพราะ IDE คุมเองอยู่แล้ว) แล้วใน **MainActivity → onCreate** ใส่ 2 บล็อก:
1. **Intent → Start Activity**, component: `com.sketchgdx.gamelib.GdxGameActivity`
2. **Activity → Finish Activity**

แค่นี้แอปจะบูตแล้วสลับเข้าเกมทันทีในเฟรมเดียว ไม่กระพริบ ไม่ต้องแตะ manifest
ส่วนที่ IDE ดูแลเอง — ทางเลือกอื่น (ตั้ง GdxGameActivity เป็น LAUNCHER ตรงๆ)
ก็ทำได้เหมือนกัน แต่เสี่ยงกว่าเพราะ Sketchware Pro บาง build อาจ regenerate
ส่วนนั้นทับ ดูรายละเอียดทั้งสองแบบได้ในคอมเมนต์ของ `AndroidManifest.xml`

ถ้าอยากรับผลลัพธ์กลับ (เช่นคะแนน) ใช้ **Start Activity for Result** แทนใน
ขั้นตอนที่ 1 แล้วรับค่าใน `onActivityResult` โดยอ่าน extra key `score` —
ฝั่งเกมเรียก `finishWithScore(int)` ใน `GdxGameActivity` เมื่อจบเกม

## ขยายเป็นเกมจริงของคุณ
วิธีหลัก (แนะนำ): เพิ่มคลาสใหม่ `implements Screen` วางไว้โฟลเดอร์เดียวกับ
`MenuScreen`/`PlayScreen2D` แล้วสลับไปด้วย `game.setScreen(new YourScreen(...))`
โหลด asset ผ่าน `AssetManager` ตามปกติของ libGDX — path อ้างอิงจากโฟลเดอร์
`assets/` ของแอป (โฟลเดอร์เดียวกับที่ Sketchware Pro มี Assets Manager ให้)
ไม่ต้องตั้งค่าอะไรเพิ่ม

วิธีเร็ว/ทดสอบสั้นๆ: แก้ `BaseGame.java` (extends `ApplicationAdapter` คลาส
เดียวจบ ไม่มีระบบสลับหน้าจอ) แล้วสลับ `GdxGameActivity` ให้ `initialize(new BaseGame(), config)`
แทน `new GdxGame()`

## ทำไมต้องมี fat-aar / copyAndroidNatives
Sketchware Pro import Local Library แบบ `.aar` ตรงๆ โดยไม่ไปไล่ resolve
dependency จาก Maven ให้ ดังนั้นคลาสของ libGDX core, libGDX-backend-android
และไฟล์ native `.so` (armeabi-v7a / arm64-v8a / x86 / x86_64) ต้องถูกอัดรวม
เข้าไปในไฟล์ `.aar` ก้อนเดียวตั้งแต่ตอน build เลย — นี่คือสิ่งที่
`gdxgamelib/build.gradle` ทำผ่านปลั๊กอิน `fat-aar` + task `copyAndroidNatives`

## รองรับ Kotlin ด้วยไหม
รองรับครับ ตัว `.aar` เป็น Java bytecode ธรรมดา ซึ่ง Kotlin เรียกใช้ได้ตรงๆ
โดยไม่ต้องแปลงอะไรเพิ่ม (Java <-> Kotlin interop มาตรฐาน) — แต่ Sketchware Pro
ต้องเป็นรุ่น **`-minApi26`** (รองรับ Android 8+) เท่านั้นถึงจะมีตัวเลือก
"เพิ่มไฟล์ Kotlin" ในหน้า Java/Kotlin Manager (รุ่น `-minApi21` ยังไม่มี Kotlin)
ดูตัวอย่างที่ `gdxgamelib-examples/KotlinGame.kt` — เป็นคลาส Kotlin ที่
extends `BaseGame` (Java) ตรงๆ

## รองรับ Android เวอร์ชันไหนบ้าง
`minSdk 24` (Android 7.0 ขึ้นไป) ตามที่ขอ — ใช้ได้ทั้งกับ Sketchware Pro
รุ่น `-minApi26` (มี Kotlin) และ `-minApi21` (Java อย่างเดียว) ได้ทั้งคู่
ขอแค่ตัวโปรเจกต์แอปของคุณใน Sketchware Pro ตั้ง minSdk ไว้ที่ 24 ขึ้นไปด้วย
(ตั้งได้ที่ Project > Advanced version control หรือ Manifest editor)

## แก้ปัญหาที่เจอจากรอบก่อน (fat-aar ใช้กับ AGP 8 ไม่ได้)
ปลั๊กอิน `fat-aar` ตัวต้นฉบับ (kezong 1.3.8) ใช้ API ที่ **ถูกถอดออกไปตั้งแต่
AGP 8.0** ทำให้ build fail แน่นอนถ้าใช้กับ Android Gradle Plugin 8.1.4 แบบที่
โปรเจกต์นี้ตั้งไว้ — เปลี่ยนไปใช้ฟอร์ก `com.github.aasitnikov:fat-aar-android:1.4.1`
แล้วแทน (รองรับ AGP 8.5 / Gradle 8.7 ที่ยืนยันจากผู้ดูแลฟอร์กเอง) ปัญหานี้แก้ให้
แล้วในไฟล์ `build.gradle` (root และ `gdxgamelib/build.gradle`)

นอกจากนี้ยังเพิ่ม `consumer-rules.pro` (keep rule กันคลาส libGDX โดน proguard/
minify ตัดทิ้งตอน export APK/AAB จริง) ไว้ในตัว AAR ให้อัตโนมัติด้วย

## เขียนเกมล้วนด้วยโค้ด (แทบไม่ใช้บล็อกเลย)
โครง `gdxgamelib` ตอนนี้ใช้แพทเทิร์นมาตรฐานของ libGDX คือ `Game` + `Screen`
แทนคลาสเดียวทำทุกอย่าง:
- `GdxGame.java` — entry point, สลับหน้าจอด้วย `setScreen(...)`
- `MenuScreen.java` — ตัวอย่างเมนู แตะจอเพื่อเริ่ม
- `PlayScreen2D.java` — ตัวอย่าง 2D ขยับสไปรท์ด้วยปุ่มลูกศร/แตะจอ
- `PlayScreen3D.java` — ตัวอย่าง 3D กล่องหมุน (ไม่ต้องมีไฟล์โมเดลเพิ่ม)

สร้างเกมจริงโดยเพิ่มคลาส `implements Screen` ของคุณเอง วางไว้โฟลเดอร์เดียวกัน
แล้ว `game.setScreen(new YourScreen(...))` จากหน้าจอไหนก็ได้ — ทั้งหมดนี้เป็น
Java class ล้วน ไม่เกี่ยวกับบล็อกของ Sketchware Pro เลย (จะเขียนเป็น Kotlin
แทนก็ได้ตามที่คุยไว้ก่อนหน้า — ดู `gdxgamelib-examples/KotlinGame.kt`)

## หมายเหตุ
- โปรเจกต์นี้ตั้งค่า `compileSdk 34`, `minSdk 24`, Android Gradle Plugin 8.1.4
  ถ้า Sketchware Pro ของคุณ build ด้วย AGP คนละเวอร์ชัน ปรับเลขใน `build.gradle`
  (root) ให้ตรงกันได้
- Workflow ตอนนี้มีขั้นตอน "Sanity-check AAR contents" เพิ่มมาแล้ว — มันจะแตก
  ไฟล์ .aar ที่ build ได้ออกมาดูเองใน log ของ Actions ว่ามี `jni/` ครบ 4 ABI
  และ `classes.jar` มีคลาส `com.badlogic.gdx.*` อยู่จริงไหม ถ้าขั้นตอนนี้ผ่าน
  แปลว่า AAR ที่ได้ครบสมบูรณ์แน่นอนก่อนที่คุณจะโหลดไปใช้
- ผมไม่มี Android SDK / Google Maven ในแซนด์บ็อกซ์ที่ใช้เขียนโค้ดนี้ให้ จึงยัง
  ไม่ได้รันคอมไพล์จริงด้วยตัวเอง — ให้ GitHub Actions (ซึ่งต่อเน็ตได้เต็มที่)
  เป็นตัวคอมไพล์และเช็คจริง ถ้ารอบแรก log ฟ้อง error ตรงไหน ส่ง log มาให้ดูได้
  เลย จะไล่แก้ให้ทันที
