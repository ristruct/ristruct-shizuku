# สร้าง Local Library (LibGDX) สำหรับ Sketchware Pro ด้วย GitHub Actions

ระบบนี้จะ build LibGDX **1.14.2 (เวอร์ชันเสถียรล่าสุด ณ ตอนนี้)** ให้ออกมาเป็น
`classes.jar` + `classes.dex` พร้อมวางในโฟลเดอร์ `local_libs/GdxLib` ของ
Sketchware Pro ได้ทันที โดยรันอัตโนมัติผ่าน GitHub Actions (ไม่ต้องมี Android
Studio ในเครื่อง)

## โครงสร้างโปรเจกต์
```
.
├── build.gradle / settings.gradle          # root gradle
├── libgdx-module/                          # โมดูลที่ห่อ LibGDX ไว้
│   ├── build.gradle
│   └── src/main/java/com/ristruct/gdxlib/
│       ├── GdxGameActivity.java            # Activity สำหรับเรียกเกม
│       └── DefaultGdxListener.java         # หน้าจอทดสอบเปล่า ๆ
└── .github/workflows/build-locallib.yml    # workflow หลัก
```

## วิธีใช้
1. Push โค้ดชุดนี้ขึ้น GitHub repo ของคุณ
2. เปิดแท็บ **Actions** แล้วรัน workflow "Build LibGDX Local Library
   (Sketchware Pro)" (หรือรอให้รันเองตอน push เข้า `main`)
3. เมื่อ build เสร็จ จะได้ไฟล์แนบ (Artifacts) 2 ตัว:
   - `GdxLib-local_libs.zip` → แตกไฟล์แล้วจะได้โฟลเดอร์
     `local_libs/GdxLib/` ที่มี `classes.jar` + `classes.dex`
   - `GdxLib-native_libs.zip` → ไฟล์ `.so` ของแต่ละสถาปัตยกรรม
     (armeabi-v7a, arm64-v8a, x86, x86_64)

## นำไปใช้ใน Sketchware Pro
1. คัดลอกโฟลเดอร์ `GdxLib` ทั้งโฟลเดอร์ไปวางในโฟลเดอร์
   `local_libs/` ของโปรเจกต์ Sketchware Pro บนมือถือ (path ปกติคือ
   `/storage/emulated/0/.sketchware/data/<project_id>/local_libs/`)
2. ในแอป Sketchware Pro: Project → Library Manager → Local Library →
   เลือก `GdxLib` แล้วเปิดใช้งาน (compile-time reference)
3. คัดลอกไฟล์ `.so` จาก `native_libs_for_jniLibs/<abi>/` ไปวางใน
   `app/src/main/jniLibs/<abi>/` ของโปรเจกต์ (จำเป็น เพราะไฟล์ `.so`
   ใส่ใน local_libs ไม่ได้ ต้องวางแยกแบบนี้เสมอ)
4. `GdxGameActivity` ไม่สามารถทำให้เป็น `MainActivity` หลักของ
   Sketchware Pro ได้ตรง ๆ (MainActivity ของ Sketchware Pro ต้อง extends
   AppCompatActivity) — ให้เพิ่มเป็น **Extra Activity** แทน แล้วเรียกจาก
   MainActivity ด้วยบล็อก Intent เช่น:
   ```java
   Intent i = new Intent(MainActivity.this, GdxGameActivity.class);
   i.putExtra("gdxListenerClass", "com.yourpackage.YourGameListener");
   startActivity(i);
   ```
5. `YourGameListener` คือคลาสเกมจริงของคุณ (implements
   `com.badlogic.gdx.ApplicationListener`) ที่คุณเขียนเองในโปรเจกต์
   Sketchware Pro (Extra .java source) — แยกจาก local_libs เพื่อให้
   `GdxLib` ตัวนี้ใช้ซ้ำได้กับทุกเกมที่จะทำต่อไป ไม่ต้อง build ใหม่ทุกครั้ง

## หมายเหตุสำคัญ (อ่านก่อนใช้จริง)
- ผมรัน/ทดสอบ workflow นี้ไม่ได้ในระบบตอนนี้ เพราะ sandbox ที่ผมใช้เขียนไฟล์
  ให้ไม่มีสิทธิ์ต่ออินเทอร์เน็ตไปยัง Maven/Android SDK — แต่ GitHub Actions
  runner จริงมีอินเทอร์เน็ตเต็มรูปแบบ จึง build ได้ตามปกติ สิ่งที่ควรทำคือรัน
  ครั้งแรกแล้วดู log ถ้ามี error (เช่นเวอร์ชัน Android Gradle Plugin/Gradle
  ไม่ตรงกับ runner) ส่ง log กลับมาให้ผมแก้ให้ต่อได้ทันที
- "ไม่มีข้อผิดพลาด 100%" ไม่มีใครยืนยันแทนเครื่องจริงของคุณได้ล่วงหน้า
  (ขึ้นกับเวอร์ชัน Sketchware Pro ที่ติดตั้ง, minSdk ของโปรเจกต์คุณ ฯลฯ)
  แต่โครงสร้างและขั้นตอนที่ให้มานี้เป็น pattern มาตรฐานที่ใช้ได้จริงกับ
  Sketchware Pro + LibGDX
- ต้องการอัปเดตเป็นเวอร์ชัน LibGDX ใหม่กว่านี้ในอนาคต แค่แก้ตัวแปร
  `gdxVersion` ใน `libgdx-module/build.gradle` แล้ว push ใหม่ — Actions
  จะ build เวอร์ชันใหม่ให้อัตโนมัติ
