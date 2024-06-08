package com.example.ano.model

import android.view.GestureDetector
import android.view.MotionEvent

interface SwipeListener {
    fun onSwipeLeft()
    fun onSwipeRight()
}

class SwipeGestureListener(private val swipeListener: SwipeListener) : GestureDetector.SimpleOnGestureListener() {
    companion object {
        const val SWIPE_THRESHOLD = 100
        const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val diffX = e2?.x?.minus(e1?.x ?: 0f) ?: 0f
        val diffY = e2?.y?.minus(e1?.y ?: 0f) ?: 0f

        if (Math.abs(diffX) > Math.abs(diffY)) {
            // Swipe horizontal
            if (Math.abs(diffX) > SWIPE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (diffX > 0) {
                    swipeListener.onSwipeRight()
                } else {
                    swipeListener.onSwipeLeft()
                }
                return true
            }
        }
        return false
    }


}
