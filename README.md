# gdx-sketchware — LibGDX Local Library สำหรับ Sketchware Pro

แพ็ก LibGDX (เวอร์ชัน 1.14.2 — เสถียรล่าสุด ณ พ.ค. 2026) ให้เป็นไฟล์ `.aar`
ก้อนเดียว ครบทุกอย่าง (คลาส libGDX + native .so ทุก ABI) เพื่อ import เป็น
**Local Library** ใน Sketchware Pro ได้ตรงๆ ไม่ต้องพึ่ง Maven ตอน build ในเครื่อง

## โครงสร้าง
```
gdx-sketchware/
├── gdxgamelib/                  <- โมดูลไลบรารีที่ build ออกมาเป็น .aar
│   └── src/main/java/com/sketchgdx/gamelib/
│       ├── BaseGame.java        <- ตัวอย่างเกม ใช้ AssetManager โหลดรูปจริง
│       └── GdxGameActivity.java <- Activity ที่ Sketchware Pro เปิดผ่าน Intent
└── .github/workflows/build-aar.yml   <- GitHub Actions build ให้อัตโนมัติ
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
   ในแท็ก `<application>`:
   ```xml
   <activity
       android:name="com.sketchgdx.gamelib.GdxGameActivity"
       android:configChanges="orientation|keyboardHidden|screenSize"
       android:screenOrientation="landscape"
       android:exported="false" />
   ```
4. (ถ้าอยากเปลี่ยนรูปที่โชว์) เอารูปของคุณไปวางที่ assets ของโปรเจกต์ Sketchware
   Pro เอง (Project → Assets Manager) เช่น `sprites/player.png`

## ขั้นตอนที่ 3: เปิดเกมจากบล็อก
บนปุ่มใน Sketchware Pro ใช้บล็อก **Intent → Start Activity**:
- Component: `com.sketchgdx.gamelib.GdxGameActivity`
- (ถ้าต้องการ) putExtra key `image_path` value `sprites/player.png`

ถ้าอยากรับผลลัพธ์กลับ (เช่นคะแนน) ใช้ **Start Activity for Result** แทน แล้วรับค่า
ใน `onActivityResult` โดยอ่าน extra key `score` — ฝั่งเกมเรียก
`finishWithScore(int)` ใน `GdxGameActivity` เมื่อจบเกม

## ขยายเป็นเกมจริงของคุณ
แก้ `BaseGame.java` (หรือสร้างคลาสใหม่ extends `ApplicationAdapter`) แล้วโหลด
asset ผ่าน `AssetManager` ตามปกติของ libGDX — path จะอ้างอิงจากโฟลเดอร์
`assets/` ของแอป (โฟลเดอร์เดียวกับที่ Sketchware Pro มี Assets Manager ให้)
ไม่ต้องตั้งค่าอะไรเพิ่ม

## ทำไมต้องมี fat-aar / copyAndroidNatives
Sketchware Pro import Local Library แบบ `.aar` ตรงๆ โดยไม่ไปไล่ resolve
dependency จาก Maven ให้ ดังนั้นคลาสของ libGDX core, libGDX-backend-android
และไฟล์ native `.so` (armeabi-v7a / arm64-v8a / x86 / x86_64) ต้องถูกอัดรวม
เข้าไปในไฟล์ `.aar` ก้อนเดียวตั้งแต่ตอน build เลย — นี่คือสิ่งที่
`gdxgamelib/build.gradle` ทำผ่านปลั๊กอิน `fat-aar` + task `copyAndroidNatives`

## หมายเหตุ
- โปรเจกต์นี้ตั้งค่า `compileSdk 34`, `minSdk 21`, Android Gradle Plugin 8.1.4
  ซึ่งตรงกับช่วงที่ Sketchware Pro เวอร์ชันปัจจุบันรองรับ ถ้า Sketchware Pro ของ
  คุณ build ด้วย AGP คนละเวอร์ชัน ปรับเลขใน `build.gradle` (root) ให้ตรงกันได้
- ผมไม่มี Android SDK / Google Maven ในแซนด์บ็อกซ์ที่ใช้เขียนโค้ดนี้ให้ จึงยัง
  ไม่ได้คอมไพล์ทดสอบจริง — ให้ GitHub Actions build รอบแรกเป็นตัวเช็ค ถ้า log
  ฟ้อง error ส่งมาให้ดูได้ จะช่วยไล่แก้ต่อ
