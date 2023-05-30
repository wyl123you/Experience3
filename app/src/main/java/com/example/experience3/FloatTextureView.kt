package com.example.experience3

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.TextureView
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator

class FloatTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "FloatTextureView"
    }

    private var lastX = 0f
    private var lastY = 0f

    private var parentWidth = 0
    private var parentHeight = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val rawX = event?.rawX ?: 0f
        val rawY = event?.rawY ?: 0f

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                //Log.d(TAG, "按下")

                lastX = rawX
                lastY = rawY

                setParentSize()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                //Log.d(TAG, "拖动")
                //计算手指移动了多少
                val dx = rawX - lastX
                val dy = rawY - lastY
                move(dx, dy)
                lastX = rawX
                lastY = rawY
                return true
            }

            MotionEvent.ACTION_UP -> {
                //Log.d(TAG, "抬起")
                val gravity = calculateGravity()
                setGravity(gravity, true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun move(dx: Float, dy: Float) {
        var newX = x + dx
        var newY = y + dy

        if (newX < 0) newX = 0f
        if (newX > parentWidth - width) newX = (parentWidth - width).toFloat()

        if (newY < 0) newY = 0f
        if (newY > parentHeight - height) newY = (parentHeight - height).toFloat()

        x = newX
        y = newY
    }

    private fun calculateGravity(): Int {
        val offsetX = width / 2
        val offsetY = height / 2

        val centerX = x + offsetX
        val centerY = y + offsetY

        val parentCenterX = parentWidth / 2
        val parentCenterY = parentHeight / 2

        return if (centerX < parentCenterX) {
            Gravity.START
        } else {
            Gravity.END
        } or if (centerY < parentCenterY) {
            Gravity.TOP
        } else {
            Gravity.BOTTOM
        }
    }

    fun setGravity(gravity: Int, smooth: Boolean = false) {
        setParentSize()
        var endX = 0f
        var endY = 0f
        when (gravity) {
            Gravity.START or Gravity.TOP -> {
                endX = 0f
                endY = 0f
            }

            Gravity.START or Gravity.BOTTOM -> {
                endX = 0f
                endY = (parentHeight - height).toFloat()
            }

            Gravity.END or Gravity.TOP -> {
                endX = (parentWidth - width).toFloat()
                endY = 0f
            }

            Gravity.END or Gravity.BOTTOM -> {
                endX = (parentWidth - width).toFloat()
                endY = (parentHeight - height).toFloat()
            }
        }

        if (smooth) {
            slideToCorner(endX, endY)
        } else {
            x = endX
            y = endY
        }
    }

    private fun slideToCorner(endX: Float, endY: Float) {
        val start = PointF(x, y)
        val end = PointF(endX, endY)
        Log.d(TAG, "start: $start")
        Log.d(TAG, "end: $end")
        val evaluator = PositionEvaluator()
        val animation = ValueAnimator.ofObject(evaluator, start, end)
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.duration = 300
        animation.addUpdateListener {
            val point = it.animatedValue as PointF
            x = point.x
            y = point.y
        }
        animation.start()
    }

    private fun setParentSize() {
        val parent = parent as ViewGroup
        parentWidth = parent.width
        parentHeight = parent.height
    }

    inner class PositionEvaluator : TypeEvaluator<PointF> {
        override fun evaluate(fraction: Float, start: PointF, end: PointF): PointF {
            val dx = end.x - start.x
            val dy = end.y - start.y

            val nextX = start.x + dx * fraction
            val nextY = start.y + dy * fraction

            return PointF(nextX, nextY)
        }
    }
}