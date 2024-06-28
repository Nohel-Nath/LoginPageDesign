package com.example.newdesign


import android.content.ContentValues
import android.content.Context
import android.graphics.Outline
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.newdesign.databinding.PhotoLayoutBinding
import java.io.File

class GalleryAdapter(
    private var isGalleryPGranted: Boolean,
    private var isCameraGranted: Boolean,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val VIEW_TYPE_CAMERA = 0
    private val VIEW_TYPE_GALLERY = 1
    private val VIEW_TYPE_IMAGE = 2
    private var isAlbumSelected: Boolean = false
    private var imageCapture: ImageCapture? = null
    private var imagesList = listOf<String>()

    fun updateAlbumSelection(selectedAlbum: String?) {
        isAlbumSelected = !selectedAlbum.isNullOrEmpty()
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateData(newImagesList: List<String>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = imagesList.size
            override fun getNewListSize(): Int = newImagesList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                imagesList[oldItemPosition] == newImagesList[newItemPosition]

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                imagesList[oldItemPosition] == newImagesList[newItemPosition]
        })

        imagesList = newImagesList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CAMERA -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_open_camera, parent, false)
                ViewHolder(view, viewType)
            }

            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_open_gallery, parent, false)
                ViewHolder(view, viewType)
            }

            else -> {
                val binding =
                    PhotoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding.root, viewType)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.viewType == VIEW_TYPE_CAMERA && !isAlbumSelected) {
            if (!isCameraGranted) {
                holder.itemView.findViewById<ConstraintLayout>(R.id.btnOpenCamera)
                    .setOnClickListener {
                        itemClickListener.onCameraPermissionDenied()
                    }
            } else {
                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(holder.itemView.context)
                holder.itemView.findViewById<ImageView>(R.id.imageView).visibility = View.INVISIBLE
                holder.itemView.findViewById<TextView>(R.id.galleryTextView1).visibility =
                    View.INVISIBLE
                holder.itemView.findViewById<TextView>(R.id.galleryTextView2).visibility =
                    View.INVISIBLE
                holder.itemView.findViewById<TextView>(R.id.galleryTextView3).visibility =
                    View.INVISIBLE
                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(holder.itemView.findViewById<PreviewView>(R.id.previewView).surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            holder.itemView.context as LifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (exc: Exception) {
                        Log.e(ContentValues.TAG, "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(holder.itemView.context))
                val previewView = holder.itemView.findViewById<PreviewView>(R.id.previewView)
                previewView.setOnClickListener {
                    itemClickListener.onCameraClick()
                }
                previewView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        val radius =
                            holder.itemView.context.resources.getDimension(R.dimen.outline_radius)
                        outline.setRoundRect(0, 0, view.width, view.height, radius)
                    }
                }
                previewView.clipToOutline = true
            }
        } else if (holder.viewType == VIEW_TYPE_GALLERY) {
            val index = if (isAlbumSelected) position else position - 1
            if (index in imagesList.indices) {
                val imageFile = File(imagesList[index])
                if (imageFile.exists()) {
                    holder.binding?.let {
                        Glide.with(holder.itemView.context)
                            .load(imageFile)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(it.galleryItem)
                    }
                    // Calculate the width dynamically based on screen width and margins
                    val displayMetrics = holder.itemView.context.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val screenHeight = displayMetrics.heightPixels
                    val imageWidth =
                        (screenWidth - dpToPx(
                            40,
                            holder.itemView.context
                        )) / 3 // Subtracting margins and dividing by 3 for 3 images in a row
                    val imageHeight = (screenHeight - dpToPx(40, holder.itemView.context)) / 5
                    holder.binding?.galleryItem?.layoutParams?.height = imageHeight
                    holder.binding?.galleryItem?.layoutParams?.width = imageWidth
                }
                holder.binding?.root?.setOnClickListener {
                    itemClickListener.onItemClick(imagesList[index])
                }
            }
        } else if (holder.viewType == VIEW_TYPE_IMAGE) {
            holder.itemView.findViewById<ConstraintLayout>(R.id.btnOpenGallery).setOnClickListener {
                itemClickListener.onGalleryPermissionDenied()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isGalleryPGranted) {
            if (isAlbumSelected) {
                imagesList.size
            } else {
                imagesList.size + 1
            }
        } else {
            2
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!isAlbumSelected && position == 0) {
            VIEW_TYPE_CAMERA
        } else {
            if (!isGalleryPGranted) VIEW_TYPE_IMAGE else VIEW_TYPE_GALLERY
        }
    }

    inner class ViewHolder(itemView: View, val viewType: Int) : RecyclerView.ViewHolder(itemView) {
        val binding: PhotoLayoutBinding? = if (viewType == VIEW_TYPE_GALLERY) {
            PhotoLayoutBinding.bind(itemView)
        } else {
            null
        }
    }

    fun prefetchImages(context: Context, startIndex: Int, count: Int) {
        val endIndex = startIndex + count
        for (i in startIndex until endIndex) {
            if (i < imagesList.size) {
                Glide.with(context)
                    .load(imagesList[i])
                    .preload()
            }
        }
    }


    fun upGalleryValue(value: Boolean) {
        isGalleryPGranted = value
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateCameraValue(value: Boolean) {
        isCameraGranted = value
        notifyItemRangeChanged(0, itemCount)
    }

    interface OnItemClickListener {
        fun onItemClick(imagePath: String)
        fun onCameraClick()
        fun onGalleryPermissionDenied()
        fun onCameraPermissionDenied()
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

//val request = ImageRequest.Builder(holder.itemView.context)
//    .data(imageFile)
//    .target(it.galleryItem)
//    .size(ViewSizeResolver(it.galleryItem))
//    .build()
//val imageLoader = Coil.imageLoader(holder.itemView.context)
//imageLoader.enqueue(request)
