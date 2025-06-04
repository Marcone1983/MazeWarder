package com.marcone1983.mazewarden3d

import android.graphics.Paint
import android.graphics.Color

object SkinSystem {

    enum class Skin(val color: Int, val name: String) {
        WARRIOR(Color.YELLOW, "Guerriero"),
        MAGE(Color.MAGENTA, "Maga"),
        ROBOT(Color.CYAN, "Robot")
    }

    fun getPaint(skin: Skin): Paint {
        return Paint().apply {
            this.color = skin.color
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }
    
    fun getSkinByCharacter(character: String): Skin {
        return when (character) {
            "Guerriero" -> Skin.WARRIOR
            "Maga" -> Skin.MAGE
            "Robot" -> Skin.ROBOT
            else -> Skin.WARRIOR
        }
    }
}