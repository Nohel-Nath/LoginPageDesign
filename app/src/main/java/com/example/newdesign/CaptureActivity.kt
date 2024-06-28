package com.example.newdesign


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.newdesign.databinding.ActivityCaptureBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
@Suppress("DEPRECATION")
class CaptureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCaptureBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var outputDirectory: File
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPreview()
        initListener()
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun initPreview() {
        startCamera()
    }

    private fun initListener() {
        binding.btnProcess.setOnClickListener {
            takePhoto()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: photoFile.toUri()
                    binding.insidePreviewView.post {
                        binding.insideImageView.visibility = View.VISIBLE
                        binding.insidePreviewView.visibility = View.INVISIBLE
                        Glide.with(this@CaptureActivity)
                            .load(savedUri)
                            .into(binding.insideImageView)
                        binding.btnCross.visibility = View.VISIBLE
                        binding.btnClick.visibility = View.VISIBLE
                        binding.btnProcess.visibility = View.INVISIBLE
                        binding.view.visibility = View.VISIBLE
                    }
                    binding.btnClick.setOnClickListener {
                        Log.d(TAG, "Photo capture succeeded: $savedUri")
                        showToast("Photo saved successfully!")
                        val resultIntent = Intent()
                        resultIntent.putExtra("imageUri", savedUri.toString())
                        setResult(101, resultIntent)
                        finish()
                    }
                    binding.btnCross.setOnClickListener {
                        binding.insideImageView.setImageURI(null)
                        binding.insideImageView.visibility = View.INVISIBLE
                        binding.insidePreviewView.visibility = View.VISIBLE
                        binding.btnCross.visibility = View.GONE
                        binding.btnClick.visibility = View.GONE
                        binding.btnProcess.visibility = View.VISIBLE
                        binding.view.visibility = View.GONE
                    }
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.insidePreviewView.surfaceProvider)
                }
            //binding.previewView.setPreview(preview)
            imageCapture = ImageCapture.Builder()
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}


//    interface OnInputClickListener {
//        fun sendImage(imageUri: String)
//    }
//
//    private var mOnInputClickListener: OnInputClickListener? = null
//    fun setOnInputClickListener(listener: OnInputClickListener) {
//        mOnInputClickListener = listener
//    }
//
//    private fun sendSelectedImageUri(imageUri: String) {
//        mOnInputClickListener?.sendImage(imageUri)
//    }

//private fun captureFrame() {
//    val bitmap = binding.insidePreviewView.bitmap ?: return
//    val outputBitmap = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888) // Create a 12x12 bitmap
//    val canvas = Canvas(outputBitmap)
//    val srcRect = Rect(0, 0, 12, 12)
//    val destRect = Rect(0, 0, outputBitmap.width, outputBitmap.height)
//    canvas.drawBitmap(bitmap, srcRect, destRect, null)
//
//    val renderScript = RenderScript.create(this)
//    val blurInput = Allocation.createFromBitmap(renderScript, outputBitmap)
//    val blurOutput = Allocation.createTyped(renderScript, blurInput.type)
//    val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
//    blurScript.setRadius(25f) // Adjust the blur radius as needed
//    blurScript.setInput(blurInput)
//    blurScript.forEach(blurOutput)
//    blurOutput.copyTo(outputBitmap)
//    renderScript.destroy()
//
//    binding.previewView.setBlurBitmap(outputBitmap)
//}

//private fun captureAndProcessFrame() {
//    val bitmap = binding.insidePreviewView.bitmap ?: return
//    val blurredBitmap = blurBitmap(bitmap)
//    val canvas = Canvas(blurredBitmap)
//    val previewRect = Rect(
//        binding.previewView.left,
//        binding.previewView.top,
//        binding.previewView.right,
//        binding.previewView.bottom
//    )
//    canvas.drawBitmap(bitmap, previewRect, previewRect, null)
//    binding.previewView.setBlurBitmap(blurredBitmap)
//}
//
//private fun blurBitmap(bitmap: Bitmap): Bitmap {
//    val outputBitmap = Bitmap.createBitmap(bitmap)
//    val renderScript = RenderScript.create(this)
//    val blurInput = Allocation.createFromBitmap(renderScript, bitmap)
//    val blurOutput = Allocation.createTyped(renderScript, blurInput.type)
//    val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
//    blurScript.setRadius(25f) // Adjust the blur radius as needed
//    blurScript.setInput(blurInput)
//    blurScript.forEach(blurOutput)
//    blurOutput.copyTo(outputBitmap)
//    renderScript.destroy()
//    return outputBitmap
//}

// Periodically capture the frame for the blur effect
//handler.post(object : Runnable {
//    override fun run() {
//        captureAndProcessFrame()
//        handler.postDelayed(this, 100) // Adjust the interval as needed
//    }
//})

