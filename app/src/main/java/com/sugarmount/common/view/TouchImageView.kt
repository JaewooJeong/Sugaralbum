package com.sugarmount.common.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.sugarmount.common.utils.log
import com.sugarmount.sugaralbum.model.ImageResData
import kotlin.math.atan2

class TouchImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private var touchMode: TOUCH_MODE? = null
    private var image: ImageResData = ImageResData()

    init {
        image.matrix = Matrix()
        image.savedMatrix = Matrix()
        image.oldDistance = 0f
        image.oldDegree = 0.0
    }

    internal enum class TOUCH_MODE {
        NONE,
        SINGLE,
        MULTI
    }

    private fun downSingleEvent(event: MotionEvent) {
        image.matrix = this.imageMatrix
        image.savedMatrix!!.set(image.matrix)
        image.startPoint = PointF(event.x, event.y)
        log.e("downSingleEvent:%f, %f", image.startPoint!!.x, image.startPoint!!.y)
    }

    private fun downMultiEvent(event: MotionEvent) {
        image.oldDistance = getDistance(event)
        if (image.oldDistance > 5f) {
            image.savedMatrix!!.set(image.matrix)
            image.midPoint = getMidPoint(event)
            val radian =
                atan2((event.y - image.midPoint!!.y).toDouble(), (event.x - image.midPoint!!.x).toDouble())
            image.oldDegree = radian * 180 / Math.PI
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
        image.matrix!!.set(image.savedMatrix)
        image.matrix!!.postTranslate(event.x - image.startPoint!!.x, event.y - image.startPoint!!.y)
        this.imageMatrix = image.matrix

        log.e("moveSingleEvent#1:%f, %f", event.x, event.y)
        log.e("moveSingleEvent#2:%f, %f", event.x - image.startPoint!!.x, event.y - image.startPoint!!.y)
    }

    private fun moveMultiEvent(event: MotionEvent) {
        val newDistance = getDistance(event)
        if (newDistance > 5f) {
            image.matrix!!.set(image.savedMatrix)
            val scale = newDistance / image.oldDistance
            image.matrix!!.postScale(scale, scale, image.midPoint!!.x, image.midPoint!!.y)
            val nowRadian = atan2((event.y - image.midPoint!!.y).toDouble(), (event.x - image.midPoint!!.x).toDouble())
            val nDegress = nowRadian * 180 / Math.PI
            val degree = (nDegress - image.oldDegree).toFloat()
            image.matrix!!.postRotate(degree, image.midPoint!!.x, image.midPoint!!.y)
            this.imageMatrix = image.matrix

//            log.e("moveMultiEvent")
        }
    }

    fun restore() {
        image.matrix = Matrix()
        image.savedMatrix = Matrix()
        image.startPoint = PointF()
        image.midPoint = PointF()
        image.oldDistance = 0f
        image.oldDegree = 0.0
        this.scaleType = ScaleType.FIT_CENTER
        this.invalidate()
    }

    fun rotateRight() {
        this.scaleType = ScaleType.MATRIX
        this.invalidate()

        touchMode = TOUCH_MODE.MULTI
        image.matrix = this.imageMatrix
        image.savedMatrix!!.set(image.matrix)
        image.matrix!!.postRotate(90F)
        image.matrix!!.postTranslate(this.width * 1f, 0f)
        this.imageMatrix = image.matrix
    }

    fun rotateLeft() {
        this.scaleType = ScaleType.MATRIX
        this.invalidate()

        touchMode = TOUCH_MODE.MULTI
        image.matrix = this.imageMatrix
        image.savedMatrix!!.set(image.matrix)
        image.matrix!!.postRotate(-90F)
        image.matrix!!.postTranslate(0f, this.height * 1f)
        this.imageMatrix = image.matrix
    }

    private fun startRotate() {
        this.scaleType = ScaleType.MATRIX
        this.invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    val onTouch = OnTouchListener { v, event ->
        if (v == this && image.checked) {
            startRotate()
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

    fun setImagePosition(image: ImageResData) {
        this.image = image
        if(this.image.midPoint == null) {
            restore()
        }

    }


}