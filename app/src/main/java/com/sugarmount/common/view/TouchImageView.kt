package com.sugarmount.common.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.sugarmount.common.utils.log
import java.lang.Math.sqrt
import kotlin.math.atan2


class TouchImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private var touchMode: TOUCH_MODE? = null
    private var matrixx: Matrix? = null  //기존 매트릭스
    private var savedMatrix: Matrix? = null  //작업 후 이미지에 매핑할 매트릭스
    private var startPoint: PointF? = null  //한손가락 터치 이동 포인트
    private var midPoint: PointF? = null  //두손가락 터치 시 중신 포인트
    private var oldDistance= 0f  //터치 시 두손가락 사이의 거리
    private var oldDegree = 0.0 // 두손가락의 각도

    init {
        matrixx = Matrix()
        savedMatrix = Matrix()
    }

    internal enum class TOUCH_MODE {
        NONE,  // 터치 안했을 때
        SINGLE,  // 한손가락 터치
        MULTI //두손가락 터치
    }

    private fun downSingleEvent(event: MotionEvent) {
        savedMatrix!!.set(matrixx)
        startPoint = PointF(event.x, event.y)
    }

    private fun downMultiEvent(event: MotionEvent) {
        oldDistance = getDistance(event)
        if (oldDistance > 5f) {
            savedMatrix!!.set(matrixx)
            midPoint = getMidPoint(event)
            val radian =
                atan2((event.y - midPoint!!.y).toDouble(), (event.x - midPoint!!.x).toDouble())
            oldDegree = radian * 180 / Math.PI
        }
        log.e("downMultiEvent")
    }

    private fun getMidPoint(e: MotionEvent): PointF? {
        val x = (e.getX(0) + e.getX(1)) / 2
        val y = (e.getY(0) + e.getY(1)) / 2
        return PointF(x, y)
    }

    private fun getDistance(e: MotionEvent): Float {
        val x = e.getX(0) - e.getX(1)
        val y = e.getY(0) - e.getY(1)
        return kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun moveSingleEvent(event: MotionEvent) {
        matrixx!!.set(savedMatrix)
        matrixx!!.postTranslate(event.x - startPoint!!.x, event.y - startPoint!!.y)
        this.imageMatrix = matrixx
    }

    private fun moveMultiEvent(event: MotionEvent) {
        val newDistance = getDistance(event)
        if (newDistance > 5f) {
            matrixx!!.set(savedMatrix)
            val scale = newDistance / oldDistance
            matrixx!!.postScale(scale, scale, midPoint!!.x, midPoint!!.y)
            val nowRadian = atan2((event.y - midPoint!!.y).toDouble(), (event.x - midPoint!!.x).toDouble())
            val nDegress = nowRadian * 180 / Math.PI
            val degree = (nDegress - oldDegree).toFloat()
            matrixx!!.postRotate(degree, midPoint!!.x, midPoint!!.y)
            this.imageMatrix = matrixx

//            log.e("moveMultiEvent")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public val onTouch = OnTouchListener { v, event ->
        if (v == this) {
            val action = event.action
            when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    touchMode = TOUCH_MODE.SINGLE
                    downSingleEvent(event)
                }
                MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount == 2) { // 두손가락 터치를 했을 때
                    touchMode = TOUCH_MODE.MULTI
                    downMultiEvent(event)
                }
                MotionEvent.ACTION_MOVE -> if (touchMode === TOUCH_MODE.SINGLE) {
                    moveSingleEvent(event)
                } else if (touchMode === TOUCH_MODE.MULTI) {
                    moveMultiEvent(event)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> touchMode = TOUCH_MODE.NONE
            }
        }
        true
    }


}