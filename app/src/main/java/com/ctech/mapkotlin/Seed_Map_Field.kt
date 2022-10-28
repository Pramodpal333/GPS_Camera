package com.ctech.mapkotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build.ID
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Seed_Map_Field : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mLastLocation: Location

    private lateinit  var viewFinder : PreviewView

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var tvAddressTitle: TextView
    private lateinit var tvFullAddress: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvLong: TextView
    private lateinit var tvDate: TextView
    private lateinit var llDisplayAddress: LinearLayout
    private  var locationBitmap : Bitmap? = null
    private  var new : Bitmap? = null
    private  var neewLocation : Bitmap? = null
    private lateinit var googleMapFragment : Fragment

    var addresses: List<Address>? = null
    private lateinit var  geocoder: Geocoder

    private var village: String = ""
    private var state: String = ""
    private var district: String = ""
    private var country: String = ""
    private var address: String = ""
    private var lati: Double = 0.0
    private var longi: Double = 0.0
    private var utils : Utils = Utils()

//  map save
    private var mapfileName : String = " "
    private  var mapImage : Bitmap? = null
    private  var getMapImage : Bitmap? = null
    private  var dataImage : Bitmap? = null
    private  var dataHeight : Int? = null
    private  var mapHeight : Int = 0
    private  var mapWidth : Int = 0
    private  var dataWidth : Int? = null


//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seed_map_field)

        tvAddressTitle = findViewById(R.id.tvAddressTitle)
        tvFullAddress = findViewById(R.id.tvFullAddress)
        tvLat = findViewById(R.id.tvLat)
        tvLong = findViewById(R.id.tvLong)
        tvDate = findViewById(R.id.tvDate)
        geocoder = Geocoder(this@Seed_Map_Field)
        viewFinder = findViewById(R.id.viewFinder)
        llDisplayAddress = findViewById(R.id.llDisplayAddress)

        if (allPermissionsGranted()) {
            startCamera()
            checkOrAskLocationPermission(){
              Toast.makeText(this,"Location Access Success",Toast.LENGTH_LONG).show()
//                getCurrentLocation()
            }

        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }


//Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        llDisplayAddress.setOnClickListener{

            Log.i("LATI","Clicked")
        }

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@Seed_Map_Field)
        getCurrentLocation()


        // set on click listener for the button of capture photo
        // it calls a method which is implemented below

        findViewById<Button>(R.id.camera_capture_button).setOnClickListener {

            takePhoto()
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    // creates a folder inside internal storage
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhoto() {

        mapImage(locationBitmap!!)
        // Get a stable reference of the
        // modifiable image capture use case
        val imageCapture = imageCapture ?: return


        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener,
        // which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)


                        val image: Bitmap = BitmapFactory.decodeFile(photoFile.path)


                        var rotate =0
                        val exif: ExifInterface? = savedUri.getPath()?.let { ExifInterface(it) }
                        val rotation = exif!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)

                        rotate = when (rotation) {
                            ExifInterface.ORIENTATION_ROTATE_180 -> 180
                            6 -> 90
                            8 -> -90
                            else -> 0
                        }

                        val rotatedImage  = image.rotatee(rotate.toFloat())

                        val height = rotatedImage.height
                        Log.i("HEIGHT","$height")






//                       new  =  utils.overlay(yo,locationBitmap!!)
//                       new  =  utils.bitmapOverlayToCenter(yo,locationBitmap!!)


                        val   newLocation = utils.getResizedBitmap(locationBitmap!!, locationBitmap!!.width*2.0,locationBitmap!!.height*2.0)
//                        val resizedLocation = utils.getResizedBitmap(newLocation!!,100,50)!!
                        new = newLocation?.let { utils.mark(rotatedImage, it,height.toFloat()) }
                        Log.i("NOOOW","yo i s$image")
                        Log.i("NOOOW","height is ${newLocation?.height}")
                        Log.i("NOOOW","loco i s$locationBitmap")
                        Log.i("NOOOW","new i s$new")
                        Log.i("ADDRESS","after captured in getLoc fun i s${tvFullAddress.text}")

                        utils.saveToGallery(applicationContext,new!!,"DCIM")
                        Toast.makeText(this@Seed_Map_Field, "Image saved!", Toast.LENGTH_LONG).show()



                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)
                }
            })
    }


    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {

            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED

    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync(OnMapReadyCallback {
                    var latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

                    Log.e("getCurrentLocation", latLng.latitude.toString() + "-" + latLng.longitude)
                })
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        requestLocationPermission()
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true


//        mMap.setOnMapClickListener { latLng ->
//            if (editable) {
//                mCurrLocationMarker =
//                    mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(latLng))
//                adjustPolygonWithRespectTo(latLng)
//
//                currentLat = latLng.latitude
//                currentLng = latLng.longitude
//
//                for (i in latLngArrayListPolygon.indices) if (i == 0) {
//                    mCurrLocationMarker =
//                        mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(latLng))
//                    polygonOptions =
//                        PolygonOptions().add(
//                            latLngArrayListPolygon[0]
//                        )
//                } else {
//                    mMap.clear()
//                    polygonOptions!!.add(latLngArrayListPolygon[i])
//                    polygonOptions!!.strokeColor(Color.BLACK)
//                    polygonOptions!!.strokeWidth(5f)
//                    polygonOptions!!.fillColor(0x33FF0000)
//                    polygon = mMap.addPolygon(polygonOptions!!)
//
//                    mCurrLocationMarker?.let { markerList.add(it) }
//                }
//
//                for (i in latLngArrayListPolygon.indices) {
//                    mCurrLocationMarker = mMap.addMarker(
//                        MarkerOptions().anchor(0.5f, 0.5f).position(latLngArrayListPolygon[i])
//                    )
//                }
//
//// Getting the marker Lat & Lng and storing it in variable. It is accessible from "latLng".
//                KO = latLng.toString()
//
//// Replacing or trimming all the text that are unnecessary and only keeping the , . & numbers.
//                KO = KO.replace("[^0-9,.]".toRegex(), "").trim { it <= ' ' }
//                Polygon_lat_lng.add(KO)
//                Log.e("Polygon", Polygon_lat_lng.toString())
//            }
//        }


        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location->

            if (location != null){
                mLastLocation = location
                val currentLatLng = LatLng(location.latitude,location.longitude)
                Log.i("LATI", " ready is ${currentLatLng.latitude} & Longi is ${currentLatLng.longitude}")

                lati = currentLatLng.latitude
                longi = currentLatLng.longitude

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

                addresses = geocoder.getFromLocation(lati, longi, 1)
                state = addresses!![0].adminArea
                district = addresses!![0].locality
                country = addresses!![0].countryName
                address =  addresses!![0].getAddressLine(0)

                val sdf = SimpleDateFormat("dd/M/yyyy")
                val currentDate = sdf.format(Date())


                tvDate.text = currentDate
                tvFullAddress.text = address
                tvAddressTitle.text = "$district, $state, $country"
                tvLat.text = "Lat ${lati}"
                tvLong.text = "Lat ${longi}"

              locationBitmap =  utils.createBitmapFromLayout(llDisplayAddress)


            }
        }


        mMap.setMinZoomPreference(15f)
        mMap.uiSettings.isScrollGesturesEnabled = false
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
//Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)
        Log.e("onLocationChanged", latLng.latitude.toString() + "-" + latLng.longitude)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location->

            if (location != null){
                mLastLocation = location
                val currentLatLng = LatLng(location.latitude,location.longitude)
                Log.i("LATI", " On Location Change is ${currentLatLng.latitude} & Longi is ${currentLatLng.longitude}")

                Toast.makeText(this,"On Location Change is ${currentLatLng.latitude} & Longi is ${currentLatLng.longitude}",Toast.LENGTH_LONG).show()

                lati = currentLatLng.latitude
                longi = currentLatLng.longitude

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

                addresses = geocoder.getFromLocation(lati, longi, 1)
                state = addresses!![0].adminArea
                district = addresses!![0].locality
                country = addresses!![0].countryName
                address =  addresses!![0].getAddressLine(0)

                val sdf = SimpleDateFormat("dd/M/yyyy")
                val currentDate = sdf.format(Date())


                tvDate.text = currentDate
                tvFullAddress.text = address
                tvAddressTitle.text = "$district, $state, $country"
                tvLat.text = "Lat ${lati}"
                tvLong.text = "Lat ${longi}"

                locationBitmap =  utils.createBitmapFromLayout(llDisplayAddress)


            }
        }
//move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.moveCamera(CameraUpdateFactory.zoomIn())
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
    }

    companion object {
        private const val TAG = "CameraXGFG"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    fun Bitmap.rotatee(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }


    private fun getScreenShotFromView(v: View): Bitmap? {
        // create a bitmap object
        var screenshot: Bitmap? = null
        try {
            // inflate screenshot object
            // with Bitmap.createBitmap it
            // requires three parameters
            // width and height of the view and
            // the background color
            screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            // Now draw this bitmap on a canvas
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("GFG", "Failed to capture screenshot because:" + e.message)
        }
        // return the bitmap
        return screenshot
    }


    // When converting the layout and if google maps in present. You will need to take to convert the part with map separately, as for some
    // reason the image/pdf will not have the map in it. The map portion will be blacked-out/ blank.
    // Below method to get only the google map from the layout.
    private fun mapImage(bitmap: Bitmap) {
        val callback =
            SnapshotReadyCallback { snapshot ->
                mapImage = snapshot
                val mapDirectory =
                    Environment.getExternalStorageDirectory().toString() + "/Download/"
                mapfileName = "AgriInsuranceMap$ID.jpg"
                val mapImagePath = File(mapDirectory, mapfileName)
                try {
                    val fileOutputStream = FileOutputStream(mapImagePath)
                    mapImage?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    getMapImage(bitmap)
                    Log.e("MAP", "MAPImage")
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        mMap.snapshot(callback)
    }

//     Getting the just saved google map image from the device.
//     Because android is not able to get the Height and the width of the image from above.
    private fun getMapImage(bitmap: Bitmap) {
        val uri = Environment.getExternalStorageDirectory().toString() + "/Download/"
        val name = "AgriInsuranceMap$ID.jpg"
        val file = File(uri, name)
    Log.e("MAP", "getMAPImage")
        getMapImage = BitmapFactory.decodeFile(file.absolutePath)
        mapHeight = getMapImage!!.getHeight()
        mapWidth = getMapImage!!.getWidth()
    Log.i("LAATII", "$mapWidth  $mapHeight")
    val reSizedMap = utils.getResizedBitmap(getMapImage!!, 280.0, 280.0)
    Log.e("LATI", "map image $getMapImage")
    val neeewLocation2 = utils.getResizedBitmap(reSizedMap!!,412.0,412.0)
    neewLocation = neeewLocation2?.let { utils.overlay(bitmap, it) }
    }

    // Combining the two images together to make one image.
//    private fun combineImage(bitmap: Bitmap) {
//        var CombinedImage: Bitmap? = null
//        var width = 0
//        var height = 0
//        dataHeight = locationBitmap!!.height
//        dataWidth = locationBitmap!!.width
//
//        Log.i("LAATII", "$dataWidth  $dataHeight")
//        if (mapWidth!! > dataWidth!!) {
//            width = mapWidth!! + dataWidth!!
//            height = mapHeight!!
//        } else {
//            width = mapWidth as Int
//            height = dataHeight!!
//        }
//        CombinedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//
//        val canvas = Canvas(CombinedImage)
//        getMapImage?.let { canvas.drawBitmap(it, 0f, 0f, null) }
//        dataImage?.let { canvas.drawBitmap(it, 0f, 0f, null) }
//        val extr = Environment.getExternalStorageDirectory().toString() + "/Download/"
//        val fileName = "AgriInsuranceDownload$ID.jpg"
//        val myPath = File(extr, fileName)
//        try {
//            val fileOutputStream = FileOutputStream(myPath)
//            CombinedImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
//        } catch (e: IOException) {
//            Log.e("combineImages", "problem combining images", e)
//        }
//    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            REQUEST_CODE_PERMISSIONS
        )
    }

    // Check location permission is granted - if it is, start
// the service, otherwise request the permission
    fun checkOrAskLocationPermission(callback: () -> Unit) {
        // Check GPS is enabled
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
            buildAlertMessageNoGps(this)
            return
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            callback.invoke()
        } else {
            // callback will be inside the activity's onRequestPermissionsResult(
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
               startCamera()
            }
        }
    }

    fun buildAlertMessageNoGps(context: Context) {
        val builder = AlertDialog.Builder(context);
        builder.setMessage("Your GPS is disabled. Do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel(); }
        val alert = builder.create();
        alert.show();
    }

}