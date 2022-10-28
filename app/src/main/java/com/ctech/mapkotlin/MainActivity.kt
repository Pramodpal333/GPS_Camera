package com.ctech.mapkotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(){
    var model: ArrayList<ImageModel> = ArrayList<ImageModel>()
    var arrayList = ArrayList<String>()
    var imageList = ArrayList<Int>()
    private lateinit var farmer_recyclerView: RecyclerView
    private lateinit var recyclerAdapter: RecyclerViewAdapter

    lateinit var linearList: LinearLayout
    var i: Int = 0
    var indx: Int = 0
    var rotate = 0
    var required: Boolean = false
    var mmmm: Boolean = false

    private lateinit var currentPhotoPath: String
    private lateinit var photoPath: File
    private lateinit var uri: Uri
    var imageFileName: String = ""
    private var image1: String = ""
    private lateinit var resizeBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        farmer_recyclerView = findViewById(R.id.farmer_report_recyclerView)
        linearList = findViewById(R.id.layout_list)

        recyclerAdapter = RecyclerViewAdapter(model)
        val layoutManager = LinearLayoutManager(this)
        farmer_recyclerView.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        farmer_recyclerView.adapter = recyclerAdapter

        resizeBitmap = ContextCompat.getDrawable(this@MainActivity, R.drawable.photo_camera)!!.toBitmap()
//        mmmm = addView(5)
        addView(5)

//        if (mmmm){
//            enable(0)
//        }

    }

    private fun addView(position: Int){
        if (position != 0) {
            val plotView = layoutInflater.inflate(R.layout.pipe_image_row, null, false)
            val camera = plotView.findViewById<View>(R.id.camera) as ImageView
            val item = plotView.findViewById<View>(R.id.count) as TextView
            val editDistance = plotView.findViewById<View>(R.id.distance) as TextView

            camera.isClickable = false

            i++
            Log.e("position", i.toString())
            imageList.add(i)
            item.text = i.toString()

            camera.setOnClickListener(View.OnClickListener {
//                Log.e("position", )
//                openCamera(item.text.toString())
                checkData(item.text.toString())
            })

            linearList.addView(plotView)
            addView(position - 1)
        }
    }


    private fun checkData(MINUSINDEX: String) {
        val view: View = linearList.getChildAt(MINUSINDEX.toInt() -1)
        val editCamera = view.findViewById<View>(R.id.camera) as ImageView
        val editCount = view.findViewById<View>(R.id.count) as TextView
        val editDistance = view.findViewById<View>(R.id.distance) as TextView

        if (model.size != 0) {
//            Log.e("model", model[MINUSINDEX.toInt() - 2].getIndex().toString())
            Log.e("MINUSINDEX", MINUSINDEX)
            Log.e("model.size", model.size.toString())
//            Log.e("editCount.text", MINUSINDEX)

            if (model.size + 1 <= MINUSINDEX.toInt() - 1){
                Toast.makeText(this@MainActivity, "Click first image first", Toast.LENGTH_SHORT).show()
            }
             else {
                openCamera(MINUSINDEX)
            }
        }
        else{
            if (MINUSINDEX.toInt() == 1) {
                openCamera(MINUSINDEX)
            }
            else{
                Toast.makeText(this@MainActivity, "Click first image first", Toast.LENGTH_SHORT).show()
            }
        }

        editDistance.text = MINUSINDEX
    }

    private fun openCamera(position: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            try {
                photoPath = createImageFile()
            } catch (ex: IOException) {}
// Continue only if the File was successfully created
            if (photoPath != null) {
                uri = FileProvider.getUriForFile(
                    this@MainActivity,
                    BuildConfig.APPLICATION_ID + ".provider", photoPath
                )
                indx = position.toInt() - 1
                Log.e("indx", indx.toString())
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                resultLauncher1.launch(intent)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                arrayList.add(currentPhotoPath)

                val resizeBitmap = resize(image, image.width / 2, image.height / 2)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)

                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }

                for (i in model.indices){
                    if (model[i].getIndex() == indx){
                        Log.e("indices", indx.toString())
                        model[i] = ImageModel(resizeBitmap, rotate, indx)
                        recyclerAdapter.notifyDataSetChanged()
//                        model.removeAt(i)
//                        recyclerAdapter.notifyItemRemoved(i)
//                        recyclerAdapter.notifyItemRangeChanged(i, model.size)

//                        model.add(ImageModel(resizeBitmap, rotate, indx))
//                        recyclerAdapter = RecyclerViewAdapter(model)
//                        recyclerAdapter.notifyItemChanged(indx)
                        required = true
                        Log.e("required", required.toString())
                    }
                }

                if(!required){
                    model.add(ImageModel(resizeBitmap, rotate, indx))
                    recyclerAdapter = RecyclerViewAdapter(model)
                    farmer_recyclerView.adapter = recyclerAdapter
                    recyclerAdapter.notifyDataSetChanged()

                    required = false
                    Log.e("required", required.toString())
                }

                required = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image = image
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            image
        } else {
            image
        }
    }


    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
// Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

// Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        Log.i("imagepath", currentPhotoPath)
        return image
    }

}