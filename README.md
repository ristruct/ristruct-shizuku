# Shizuku Library สำหรับ Sketchware Pro (ต่อยอดจากไฟล์ที่ส่งมา)

โปรเจกต์นี้สร้างต่อจาก `shizuku 13.6.2 library` ที่คุณอัปโหลดมา (component.json + blocks.json + classes.jar/dex เดิม)
โดยเพิ่ม **source code จริง** ของ `ShizukuRistruct.java` และ `ShizukuShell.java` ให้ตรงกับชื่อ method
ทุกตัวที่ blocks เดิมเรียกใช้ (เช่น `ensureShizukuReady()`, `execCommandBool()`, `readFile()` ฯลฯ)
พร้อม block ใหม่ 2 อัน (`installApk`, `uninstallPackage`) เป็นตัวอย่างการต่อยอด

## โครงสร้างโปรเจกต์

```
shizuku-lib-project/
├── src/com/SzkRistruct/
│   ├── ShizukuRistruct.java      ← คลาสหลัก ตรงกับ blocks ทั้งหมด
│   └── ShizukuShell.java       ← ตัวรัน shell command ผ่าน Shizuku
├── library-meta/
│   ├── ShizukuRistruct_13_6_2_component.json
│   └── shizuku_13_6_2_extended.json   ← blocks เดิม 43 + ใหม่ 2
└── .github/workflows/build.yml  ← คอมไพล์อัตโนมัติผ่าน GitHub Actions
```

## ขั้นตอนใช้งาน (MT Manager + GitHub)

### 1. สร้าง repo บน GitHub
- สร้าง repo ใหม่ (public หรือ private ก็ได้ ฟรีทั้งคู่ Actions ใช้ได้)
- อัปโหลดไฟล์ทั้งหมดในโฟลเดอร์นี้เข้า repo (ผ่านเว็บ GitHub หรือแอป เช่น "Working Copy"/"Termux+git" ก็ได้)

### 2. แก้โค้ดด้วย MT Manager
- ใช้แอป Git บนมือถือ (เช่น MGit, Termux + git) clone repo ลงเครื่อง
- เปิดไฟล์ `.java` ด้วย MT Manager text editor เพื่อแก้/เพิ่มฟีเจอร์
- แก้เสร็จ commit + push กลับขึ้น GitHub (ผ่านแอป git ที่ใช้ clone)
- ถ้าจะเพิ่ม block ใหม่ ต้องแก้ 2 จุดคู่กันเสมอ:
  1. เพิ่ม method ใน `ShizukuRistruct.java`
  2. เพิ่ม entry ใน `shizuku_13_6_2_extended.json` โดย `code` ต้องเรียก method ชื่อเดียวกัน เช่น
     `"code": "%1$s.methodName(%2$s)"`

### 3. ให้ GitHub Actions คอมไพล์ให้ฟรี
- ทุกครั้งที่ push เข้า branch `main` workflow จะรันอัตโนมัติ (ดูที่แท็บ **Actions** ใน repo)
- ขั้นตอนที่ workflow ทำ: โหลด Android SDK + Shizuku API/Provider jar จาก Maven → คอมไพล์ `.java` →
  รวมกับ dependency ทั้งหมดเป็น `classes.jar` → แปลงเป็น `classes.dex` ด้วย `d8` →
  แพ็กรวมกับไฟล์ json เป็น `ShizukuRistruct 13.6.2 library.zip`
- คอมไพล์เสร็จ ไปที่ Actions → เลือก run ล่าสุด → เลื่อนลงไปที่ **Artifacts** → โหลด
  `shizuku-sketchware-library` (เป็น zip ที่มี classes.jar/classes.dex/json ครบ)

### 4. นำเข้า Sketchware Pro
- แตกซิปที่โหลดมา จะได้โฟลเดอร์ `ShizukuRistruct 13.6.2 library`
- ใช้ MT Manager ย้ายโฟลเดอร์นี้ไปที่ path ของ local library ใน Sketchware Pro
  (ปกติอยู่ที่ `Sketchware/.local_library_manager/` หรือใช้เมนู "Import Library" ในแอป Sketchware Pro โดยตรง ถ้ามี)
- เปิด Sketchware Pro → เพิ่ม component ShizukuRistruct ในโปรเจกต์ตามปกติ

## ข้อควรระวัง / จุดที่ควรทดสอบ

- **ผมยังไม่ได้รันคอมไพล์จริงในเครื่อง** เพราะ sandbox นี้ไม่มีอินเทอร์เน็ตและไม่มี Android SDK ติดตั้งไว้
  โค้ดและ workflow เขียนตามหลักการที่ถูกต้อง (javac → d8) แต่ครั้งแรกที่รันบน GitHub Actions
  ควรเช็ก log ว่าทุก step ผ่าน ถ้า Maven URL หรือเวอร์ชัน build-tools เปลี่ยนไป อาจต้องแก้เลขเวอร์ชันใน
  `env:` ของ `build.yml`
- `SHIZUKU_VERSION: 13.1.5` เป็นเวอร์ชันไลบรารี Shizuku (คนละตัวกับเลข "13.6.2" ที่เป็นชื่อไฟล์เดิมของคุณ
  ซึ่งน่าจะเป็นเลขเวอร์ชันของ component/lib เอง) — ตรวจสอบเวอร์ชันล่าสุดได้ที่
  https://github.com/RikkaApps/Shizuku-API
- `writeFile()` ใช้ `printf` แทน `echo` เพื่อกันปัญหาอักขระพิเศษ แต่ input ที่มี quote (`'`) เยอะ ๆ
  ควรทดสอบก่อนใช้กับข้อมูลจริง
- ทุกคำสั่งที่รันผ่าน `ShizukuShell` ทำงานด้วยสิทธิ์ที่ Shizuku ได้รับ (root หรือ adb) —
  แอปที่ใช้ lib นี้ควรแจ้งผู้ใช้ให้ชัดเจนว่ามีการขอสิทธิ์ระดับสูง และควรตรวจสอบ input
  (เช่น path, package name) ก่อนส่งเข้า shell เพื่อกัน command injection ถ้ารับ input จากผู้ใช้ปลายทาง

## บล็อกใหม่ที่เพิ่มมา

| บล็อก | Method | คำอธิบาย |
|---|---|---|
| Install APK at path %s | `installApk(String apkPath)` | รัน `pm install -r` ผ่าน Shizuku |
| Uninstall package %s | `uninstallPackage(String packageName)` | รัน `pm uninstall` ผ่าน Shizuku |

อยากเพิ่มบล็อกอะไรต่อ บอกมาได้เลยครับ จะเขียน method คู่กับ block ให้ตามรูปแบบเดิม
