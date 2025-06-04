package com.marcone1983.mazewarden3d

import android.view.View
import android.view.animation.TranslateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet

object PlayerAnimator {

    fun animateStep(view: View, fromX: Float, toX: Float, fromY: Float, toY: Float) {
        val anim = TranslateAnimation(
            fromX, toX,
            fromY, toY
        ).apply {
            duration = 150
            fillAfter = true
        }
        view.startAnimation(anim)
    }
    
    fun animateSkillUse(view: View) {
        val scaleAnim = ScaleAnimation(
            1.0f, 1.3f, 1.0f, 1.3f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300
            repeatCount = 1
            repeatMode = android.view.animation.Animation.REVERSE
        }
        
        val alphaAnim = AlphaAnimation(1.0f, 0.5f).apply {
            duration = 300
            repeatCount = 1
            repeatMode = android.view.animation.Animation.REVERSE
        }
        
        val animSet = AnimationSet(true).apply {
            addAnimation(scaleAnim)
            addAnimation(alphaAnim)
        }
        
        view.startAnimation(animSet)
    }
    
    fun animateWallHit(view: View) {
        val shakeAnim = TranslateAnimation(
            0f, 10f, 0f, 0f
        ).apply {
            duration = 50
            repeatCount = 3
            repeatMode = android.view.animation.Animation.REVERSE
        }
        view.startAnimation(shakeAnim)
    }
}