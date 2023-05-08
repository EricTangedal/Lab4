package com.zybooks.lab4

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.zybooks.lab4.databinding.FragmentCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private var photoFile: File? = null
    private lateinit var photoImageView: ImageView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoImageView = view.findViewById(R.id.photo)

        binding.cameraButton.setOnClickListener {
            takePhotoClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createImageFile(): File {

        // Create a unique filename
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFilename = "photo_$timeStamp.jpg"

        // Create the file in the Pictures directory on external storage
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, imageFilename)
    }

    private fun takePhotoClick() {

        // Create the File for saving the photo
        val photoFile = createImageFile()

        // Create a content URI to grant camera app write permission to photoFile
        val photoUri: Uri = FileProvider.getUriForFile(requireContext(), "com.zybooks.lab4.fileprovider", photoFile)

        // Start camera app
        takePicture.launch(photoUri)
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            Toast.makeText(
                requireContext(), "Saved photo", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(), "Did not save photo", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun displayPhoto() {
        // Get ImageView dimensions
        val targetWidth = photoImageView.width
        val targetHeight = photoImageView.height

        // Get bitmap dimensions
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoFile!!.absolutePath, bmOptions)
        val photoWidth = bmOptions.outWidth
        val photoHeight = bmOptions.outHeight

        // Determine how much to scale down the image
        val scaleFactor = min(photoWidth / targetWidth, photoHeight / targetHeight)

        // Decode the image file into a smaller bitmap that fills the ImageView
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath, bmOptions)

        // Display smaller bitmap
        photoImageView.setImageBitmap(bitmap)
    }
}