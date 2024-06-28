package com.example.newdesign

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.newdesign.databinding.ActivityNidverificationBinding
import java.io.File
import java.util.concurrent.Executors

class NIDVerification : AppCompatActivity(), BottomSheetDialogCameraGallery.OnInputListener {
    private lateinit var binding: ActivityNidverificationBinding
    private lateinit var customBottomSheetDialog: BottomSheetDialogCameraGallery
    private lateinit var currentUploadType: NidUpload
    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        storagePermission
    )
    private val getClickedImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 101) {
                Log.e("data from other activity", ": data = ${result.data?.extras.toString()}")
                val data = result.data
                val imageUri = data?.getStringExtra("imageUri")
                Log.e("TAG", ": data = $imageUri")
                if (imageUri != null) {
                    when (currentUploadType) {
                        NidUpload.FRONT_PART -> {
                            Glide.with(this).load(Uri.parse(imageUri))
                                .into(binding.imageViewFrontSideNid)
                            showViewsAfterImageSelection()
                        }

                        NidUpload.BACK_PART -> {
                            Glide.with(this).load(Uri.parse(imageUri))
                                .into(binding.imageViewBackSideNid)
                            showViewsAfterImageSelection()
                        }
                    }
                    binding.btnNext.isEnabled=true
                    binding.btnNext.alpha=1f
                }
            }
        }
    private var currentPermissionIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNidverificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.show()
        customBottomSheetDialog = BottomSheetDialogCameraGallery(this)
        customBottomSheetDialog.setOnInputListener(this)
        initListener()
    }

    private fun initListener() {
        binding.viewTakePhotoFrontSide.setOnClickListener {
            currentUploadType = NidUpload.FRONT_PART
            checkPermissionAndOpenGallery()
        }
        binding.viewTakePhotoBackSide.setOnClickListener {
            currentUploadType = NidUpload.BACK_PART
            checkPermissionAndOpenGallery()
        }
        binding.imageViewCancel.setOnClickListener {
            cancelImageSelection(NidUpload.FRONT_PART)
        }
        binding.imageViewCancelBackSide.setOnClickListener {
            cancelImageSelection(NidUpload.BACK_PART)
        }

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    override fun sendInput(imagePath: String) {
        when (currentUploadType) {
            NidUpload.FRONT_PART -> {
                //val cornerRadiusInPixels = resources.getDimensionPixelSize(R.dimen.corner_radius)
                Glide.with(this).load(File(imagePath)).into(binding.imageViewFrontSideNid)
                showViewsAfterImageSelection()
            }

            NidUpload.BACK_PART -> {
                Glide.with(this).load(File(imagePath)).into(binding.imageViewBackSideNid)
                showViewsAfterImageSelection()
            }
        }
        binding.btnNext.isEnabled=true
        binding.btnNext.alpha=1f
    }

    override fun onCameraClick() {
        val intent = Intent(this, CaptureActivity::class.java)
        getClickedImage.launch(intent)
    }

    private fun checkPermissionAndOpenGallery() {
        if (currentPermissionIndex < permissions.size) {
            val permission = permissions[currentPermissionIndex]
            requestPermissions(arrayOf(permission), currentPermissionIndex)
        } else {
            Log.d("permission index ", "open gallery")
            // currentUploadType?.let { customBottomSheetDialog.openCameraGalleryDialog(it) }
            customBottomSheetDialog.openCameraGalleryDialog(currentUploadType)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        customBottomSheetDialog.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == currentPermissionIndex) {
            currentPermissionIndex++
            checkPermissionAndOpenGallery()
        }
    }

    private fun showViewsAfterImageSelection() {
        when (currentUploadType) {
            NidUpload.FRONT_PART -> {
                binding.tvFrontSideNid.visibility = View.GONE
                binding.viewTakePhotoFrontSide.visibility = View.GONE
                binding.imageViewCaptureFrontSide.visibility = View.GONE
                binding.tvTakePhotoFrontSide.visibility = View.GONE
                //binding.viewNidCancelFrontSide.visibility = View.VISIBLE
                binding.imageViewCancel.visibility = View.VISIBLE
            }

            NidUpload.BACK_PART -> {
                binding.tvBackSideNid.visibility = View.GONE
                binding.viewTakePhotoBackSide.visibility = View.GONE
                binding.imageViewCaptureBackSide.visibility = View.GONE
                binding.tvTakePhotoBackSide.visibility = View.GONE
                //binding.viewNidCancelBackSide.visibility = View.VISIBLE
                binding.imageViewCancelBackSide.visibility = View.VISIBLE
            }
        }
    }

    private fun cancelImageSelection(nidUpload: NidUpload) {
        when (nidUpload) {
            NidUpload.FRONT_PART -> {
                binding.imageViewFrontSideNid.setImageDrawable(null)
                hideViewsAfterImageSelectionFrontSide()
                Log.d("Front Part", "hideViewsAfterImageSelection: Front Part Called")
            }

            NidUpload.BACK_PART -> {
                binding.imageViewBackSideNid.setImageDrawable(null)
                hideViewsAfterImageSelectionBackSide()
                Log.d("Front Part", "hideViewsAfterImageSelection: Front Part Called")
            }
        }
        binding.btnNext.isEnabled=false
        binding.btnNext.alpha=.5f
    }

    private fun hideViewsAfterImageSelectionBackSide() {
        binding.tvBackSideNid.visibility = View.VISIBLE
        binding.viewTakePhotoBackSide.visibility = View.VISIBLE
        binding.imageViewCaptureBackSide.visibility = View.VISIBLE
        binding.tvTakePhotoBackSide.visibility = View.VISIBLE
        //binding.viewNidCancelBackSide.visibility = View.GONE
        binding.imageViewCancelBackSide.visibility = View.GONE
        Log.d("Back Part", "hideViewsAfterImageSelection: Back Part")
    }

    private fun hideViewsAfterImageSelectionFrontSide() {
        binding.tvFrontSideNid.visibility = View.VISIBLE
        binding.viewTakePhotoFrontSide.visibility = View.VISIBLE
        binding.imageViewCaptureFrontSide.visibility = View.VISIBLE
        binding.tvTakePhotoFrontSide.visibility = View.VISIBLE
        //binding.viewNidCancelFrontSide.visibility = View.GONE
        binding.imageViewCancel.visibility = View.GONE
        Log.d("Front Part", "hideViewsAfterImageSelection: Front Part")
    }

}

//    private fun checkImagesAndEnableButton() {
//        val frontImageFilled = binding.imageViewFrontSideNid.drawable != null
//        val backImageFilled = binding.imageViewBackSideNid.drawable != null
//        if (frontImageFilled && backImageFilled) {
//            binding.btnNext.isEnabled = true
//            binding.btnNext.alpha = 1f
//        } else {
//            binding.btnNext.isEnabled = false
//            binding.btnNext.alpha = 0.5f
//        }
//    }
