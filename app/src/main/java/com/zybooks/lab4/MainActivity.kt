package com.zybooks.lab4

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.zybooks.lab4.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleMap: GoogleMap
    private var client: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var zoomLevel = 15f
    private var mp: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        mp = MediaPlayer.create(this, R.raw.fart)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (hasLocationPermission()) {
            trackLocation()
        }

        binding.fab.setOnClickListener {
            takePhotoClick()
        }

        binding.fab2.setOnClickListener {
            mp!!.start()
        }
    }

//Map Functions
    private fun trackLocation() {

        // Create location request
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .build()

        // Create location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    updateMap(location)
                }
            }
        }

        client = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun updateMap(location: Location) {

        // Get current location
        val currentLatLng = LatLng(location.latitude,
            location.longitude)

        // Remove previous marker
        googleMap.clear()

        // Place a marker at the current location
        val markerOptions = MarkerOptions()
            .title("Here you are!")
            .position(currentLatLng)
        googleMap.addMarker(markerOptions)

        // Zoom to previously saved level
        val update: CameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel)

        googleMap.animateCamera(update)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Save zoom level
        googleMap.setOnCameraMoveListener {
            zoomLevel = googleMap.cameraPosition.zoom
        }

        // Handle marker click
        googleMap.setOnMarkerClickListener { marker: Marker ->
            val lineBreak = System.getProperty("line.separator")
            Toast.makeText(this,
                "Lat: ${marker.position.latitude} $lineBreak Long: ${marker.position.longitude}",
                Toast.LENGTH_LONG).show()

            return@setOnMarkerClickListener false
        }
    }

    override fun onPause() {
        super.onPause()
        client?.removeLocationUpdates(locationCallback!!)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            client?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, Looper.getMainLooper())
        }
    }

    private fun hasLocationPermission(): Boolean {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            trackLocation()
        }
    }

//Camera Functions
    private fun createImageFile(): File {

        // Create a unique filename
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFilename = "photo_$timeStamp.jpg"

        // Create the file in the Pictures directory on external storage
        val storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, imageFilename)
    }

    private fun takePhotoClick() {

        // Create the File for saving the photo
        val photoFile = createImageFile()

        // Create a content URI to grant camera app write permission to photoFile
        val photoUri: Uri = FileProvider.getUriForFile(this, "com.zybooks.lab4.fileprovider", photoFile)

        // Start camera app
        takePicture.launch(photoUri)
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            Toast.makeText(
                this, "Saved photo", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this, "Did not save photo", Toast.LENGTH_SHORT
            ).show()
        }
    }
}