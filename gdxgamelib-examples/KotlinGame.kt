// ตัวอย่างนี้ไม่ได้ compile รวมเข้า .aar — ให้ก็อปไปวางเป็นไฟล์ .kt
// ใน Sketchware Pro (Java/Kotlin Manager -> Add -> Kotlin) เพื่อพิสูจน์ว่า
// เรียกคลาสจาก gdxgamelib.aar (ที่เป็น Java bytecode) ได้ตรงๆ แบบไม่ต้องแปลงอะไร
//
// ต้องใช้ Sketchware Pro รุ่น "-minApi26" เท่านั้น (Android 8+) ถึงจะมีตัวเลือก
// Kotlin ให้เพิ่มไฟล์ประเภทนี้

package com.sketchgdx.gamelib.example

import com.sketchgdx.gamelib.BaseGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

/**
 * สืบทอด BaseGame (Java class ใน .aar) จากฝั่ง Kotlin ตรงๆ
 * แสดงว่า Kotlin <-> Java class ใน local library ทำงานร่วมกันได้เต็มรูปแบบ
 */
class KotlinGame(imagePath: String = "badlogic.jpg") : BaseGame(imagePath) {

    override fun render() {
        // เรียก super ที่เป็นเมธอด Java ปกติ
        super.render()
        // ผสมโค้ด libGDX (Java library) กับ syntax Kotlin ได้ในไฟล์เดียว
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }
}
