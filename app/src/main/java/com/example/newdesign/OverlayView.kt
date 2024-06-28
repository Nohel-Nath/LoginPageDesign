@file:Suppress("DEPRECATION")

package com.example.newdesign

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.camera.core.Preview

@Suppress("DEPRECATION")
class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var bitmap: Bitmap? = null

    init {
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.nidimage)
        if (bitmap == null) {
            Log.e("OverlayView", "Bitmap could not be loaded!")
        } else {
            Log.d("OverlayView", "Bitmap loaded successfully.")
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = 0f
        val top = 0f
        val right = viewWidth.toFloat()
        val bottom = viewHeight.toFloat()
        canvas.drawRect(left, top, right, bottom, paint)
        bitmap?.let {
            val scaledBitmap = Bitmap.createScaledBitmap(it, viewWidth, viewHeight, false)
            val blurredBitmap = blurBitmap(context, scaledBitmap, 20f)
            canvas.drawBitmap(blurredBitmap, 0f, 0f, null)
        }
    }

    private fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        val outputBitmap = Bitmap.createBitmap(bitmap)
        val renderScript = RenderScript.create(context)
        val input = Allocation.createFromBitmap(renderScript, bitmap)
        val output = Allocation.createFromBitmap(renderScript, outputBitmap)
        val scriptIntrinsicBlur =
            ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptIntrinsicBlur.setRadius(radius)
        scriptIntrinsicBlur.setInput(input)
        scriptIntrinsicBlur.forEach(output)
        output.copyTo(outputBitmap)
        renderScript.destroy()
        return outputBitmap
    }
}


/*
class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.BLACK
        alpha = 150 // Semi-transparent black
    }

    private val rect = RectF()
    private var blurBitmap: Bitmap? = null
    private var blurredBitmap: Bitmap? = null
    private var renderScript: RenderScript = RenderScript.create(context)
    private var blurInput: Allocation? = null
    private var blurOutput: Allocation? = null
    private var blurScript: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the blurred overlay
        blurBitmap?.let {
            blurredBitmap = it.copy(it.config, true)
            blurInput = Allocation.createFromBitmap(renderScript, blurredBitmap)
            blurOutput = Allocation.createTyped(renderScript, blurInput!!.type)
            blurScript.setRadius(25f) // Adjust the blur radius as needed
            blurScript.setInput(blurInput)
            blurScript.forEach(blurOutput)
            blurOutput?.copyTo(blurredBitmap)
            canvas.drawBitmap(blurredBitmap!!, 0f, 0f, null)
        }
        canvas.drawRect(rect, paint)
    }

    fun setPreviewArea(left: Float, top: Float, right: Float, bottom: Float) {
        rect.set(left, top, right, bottom)
        invalidate()
    }

    fun setBlurBitmap(bitmap: Bitmap) {
        blurBitmap = bitmap
        invalidate()
    }
}*/
