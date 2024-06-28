package com.example.newdesign

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newdesign.databinding.BottomlayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File

class BottomSheetDialogCameraGallery(private val context: Context) {
    private lateinit var binding: BottomlayoutBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var images: ArrayList<String>
    private lateinit var adapter: GalleryAdapter
    private lateinit var manager: GridLayoutManager

    companion object {
        private const val REQUEST_GALLERY_PERMISSION = 100
        private const val REQUEST_CAMERA_PERMISSION = 101
    }

    interface OnInputListener {
        fun sendInput(imagePath: String)
        fun onCameraClick()
    }

    private var mOnInputListener: OnInputListener? = null
    fun setOnInputListener(listener: OnInputListener) {
        mOnInputListener = listener
    }

    private fun sendSelectedImagePath(imagePath: String) {
        mOnInputListener?.sendInput(imagePath)
    }

    private var selectedAlbum: String? = null
    private var albumsDialog: BottomSheetDialog? = null
    private var dialog: BottomSheetDialog? = null
    private lateinit var albumsList: List<Album>
    val paddingInDp = 5

    fun openCameraGalleryDialog(nidUpload: NidUpload) {
        Log.d("permission index ", "inside Bottom sheet")
        dialog = BottomSheetDialog(context)
        binding = BottomlayoutBinding.inflate(LayoutInflater.from(context))
        dialog?.setContentView(binding.root)
        dialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog?.behavior?.isDraggable = false
        binding.toolbar.title = "All Photos"
        binding.iconImageView.visibility = View.INVISIBLE
        recyclerView = binding.galleryRecycler
        images = ArrayList()

        val isHaveCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val storagePermissionForCameraGallery =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        val isHaveGalleryPermission = ContextCompat.checkSelfPermission(
            context,
            storagePermissionForCameraGallery
        ) == PackageManager.PERMISSION_GRANTED

        adapter = GalleryAdapter(
            isHaveGalleryPermission,
            isHaveCameraPermission,
            object : GalleryAdapter.OnItemClickListener {
                override fun onItemClick(imagePath: String) {
                    val imageName = File(imagePath).name
                    //Toast.makeText(context, "Selected Image: $imageName", Toast.LENGTH_LONG).show()
                    sendSelectedImagePath(imagePath)
                    dialog?.dismiss()
                }

                override fun onCameraClick() {
                    dialog?.dismiss()
                    mOnInputListener?.onCameraClick()
                }

                override fun onGalleryPermissionDenied() {
                    requestGalleryPermission()
                }

                override fun onCameraPermissionDenied() {
                    requestCameraPermission()
                }
            })
        recyclerView.adapter = adapter
        adapter.updateData(images)
        galleryAdapterSet()

        selectedAlbum = null
        albumsList = emptyList()
        if (isHaveGalleryPermission) {
            loadImages()
            albumsList = fetchAlbums()
            binding.iconImageView.visibility = View.VISIBLE
            binding.iconImageView.setOnClickListener {
                showAlbumsDialog()
            }
        }
        dialog?.setCancelable(true)
        dialog?.show()
    }

    private fun galleryAdapterSet() {
        manager = GridLayoutManager(context, 3)
        val itemDecoration = GridSpacingItemDecoration(dpToPx(paddingInDp))
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.layoutManager = manager
        val startIndex = 0 // Starting index of items to prefetch
        val count = 20 // Number of items to prefetch
        adapter.prefetchImages(context, startIndex, count)
    }

    @SuppressLint("InflateParams")
    private fun showAlbumsDialog() {
        val windowHeight = context.resources.displayMetrics.heightPixels
        albumsDialog = BottomSheetDialog(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogalbums, null)
        albumsDialog?.setContentView(dialogView)
        // Set the dialog's height
        val layoutParams = dialogView.layoutParams
        layoutParams.height = windowHeight
        dialogView.layoutParams = layoutParams
        val recyclerView = albumsDialog?.findViewById<RecyclerView>(R.id.recyclerViewForAlbum)
        val layoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = layoutManager
        val adapterAlbum = AlbumAdapter(object : AlbumAdapter.OnAlbumClickListener {
            override fun onAlbumClick(album: Album) {
                albumsDialog?.dismiss()
                selectedAlbum = album.albumName
                loadImages()
                binding.toolbar.title = selectedAlbum
                adapter.updateAlbumSelection(selectedAlbum)
                binding.galleryRecycler.scrollToPosition(0)
            }
        })
        recyclerView?.adapter = adapterAlbum
        // Assuming albumsList is your new list of albums
        adapterAlbum.submitList(albumsList)
        albumsDialog?.behavior?.state=BottomSheetBehavior.STATE_EXPANDED
        albumsDialog?.behavior?.isDraggable = false
        albumsDialog?.show()
        albumsDialog?.setCancelable(true)
        //binding.galleryRecycler.scrollToPosition(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadImages() {
        val selection =
            if (selectedAlbum.isNullOrEmpty()) null else "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = if (selectedAlbum.isNullOrEmpty()) null else arrayOf(selectedAlbum)
        val order = "${MediaStore.Images.Media.DATE_TAKEN} ASC"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATA),
            selection,
            selectionArgs,
            order
        )
        cursor?.use {
            images.clear()
            while (it.moveToNext()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                images.add(it.getString(columnIndex))
            }
            images.reverse()
            adapter.notifyDataSetChanged()
        }
    }

    private fun fetchAlbums(): List<Album> {
        val contentResolver = context.contentResolver
        val albums = mutableListOf<Album>()
        val encounteredAlbums = mutableSetOf<String>() // Set to keep track of encountered albums
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val bucketNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val imagePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val albumName = cursor.getString(bucketNameColumn)
                if (encounteredAlbums.contains(albumName)) {
                    continue
                }
                val imagePath = cursor.getString(imagePathColumn)
                val albumImageCount = cursor.countImagesInAlbum(albumName, contentResolver)
                encounteredAlbums.add(albumName)
                albums.add(Album(listOf(imagePath), albumName, albumImageCount))
            }
        }

        return albums
    }

    private fun Cursor.countImagesInAlbum(
        albumName: String,
        contentResolver: ContentResolver
    ): Int {
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(albumName)
        return contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            cursor.count
        } ?: 0
    }

    private fun requestGalleryPermission() {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                context,
                storagePermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            adapter.upGalleryValue(true)
            binding.iconImageView.visibility = View.VISIBLE
            albumsList = fetchAlbums()
            binding.iconImageView.setOnClickListener {
                showAlbumsDialog()
            }
            loadImages()
        } else {
            when {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    storagePermission
                ) -> {
                    showPermissionRationaleDialogForGallery()
                }

                else -> {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(storagePermission),
                        REQUEST_GALLERY_PERMISSION
                    )

                }
            }
        }
    }

    private fun requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                adapter.updateCameraValue(true)
            } else {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.CAMERA
                    ) -> {
                        showPermissionRationaleDialogForCamera()
                    }

                    else -> {
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION
                        )
                    }
                }
            }
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_GALLERY_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    adapter.upGalleryValue(true)
                    binding.iconImageView.visibility = View.VISIBLE
                    albumsList = fetchAlbums()
                    binding.iconImageView.setOnClickListener {
                        showAlbumsDialog()
                    }
                    loadImages()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity,
                            permissions[0]
                        )
                    ) {
                        explainFeatureUnavailableForGallery()
                    } else {
                        showPermissionRationaleDialogForGallery()
                    }
                }
            }

            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    adapter.updateCameraValue(true)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity,
                            Manifest.permission.CAMERA
                        )
                    ) {
                        explainFeatureUnavailableForCamera()
                    } else {
                        showPermissionRationaleDialogForCamera()
                    }
                }
            }
        }
    }

    private fun explainFeatureUnavailableForCamera() {
        AlertDialog.Builder(context)
            .setTitle("Feature Unavailable")
            .setMessage("The Image clicking feature is currently unavailable because the storage permission has been denied.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.setNegativeButton("Open Settings") { _, _ ->
                openAppSettings()
            }.show()
    }

    private fun showPermissionRationaleDialogForCamera() {
        AlertDialog.Builder(context)
            .setTitle("Storage Permission")
            .setMessage("Storage permission is needed in order to click photo")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            }
            .show()
    }

    private fun explainFeatureUnavailableForGallery() {
        AlertDialog.Builder(context)
            .setTitle("Feature Unavailable")
            .setMessage("The Image selection feature is currently unavailable because the storage permission has been denied.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.setNegativeButton("Open Settings") { _, _ ->
                openAppSettings()
            }.show()
    }

    private fun showPermissionRationaleDialogForGallery() {
        AlertDialog.Builder(context)
            .setTitle("Storage Permission")
            .setMessage("Storage permission is needed in order to select photo")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    ),
                    REQUEST_GALLERY_PERMISSION
                )
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
        dialog?.dismiss()
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
