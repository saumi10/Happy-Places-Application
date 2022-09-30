package com.example.android.happyplacesapplication.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.android.happyplacesapplication.R
import com.example.android.happyplacesapplication.database.DatabaseHandler
import com.example.android.happyplacesapplication.models.HappyPlaceModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class AddHappyPlace : AppCompatActivity(), View.OnClickListener {

    private var cal=Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var et_date:EditText
    private lateinit var tv_add_image:TextView
    private lateinit var et_title:EditText
    private lateinit var et_description:EditText
    private lateinit var et_location:EditText
    private lateinit var btn_save:Button
    private lateinit var iv_place_image:ImageView
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude:Double=0.0
    private var mLongitude:Double=0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        val toolbar=findViewById<Toolbar>(R.id.toolbar_add_place)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDate()
        }
        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddHappyPlace,
                resources.getString(R.string.google_maps_api_key)
            )
        }


        et_date=findViewById(R.id.date)
        et_date.setOnClickListener(this)

        tv_add_image=findViewById(R.id.tv_add_image)
        tv_add_image.setOnClickListener(this)

        et_location=findViewById(R.id.location)
        et_location.setOnClickListener(this)

        et_title=findViewById(R.id.ed_title)
        et_description=findViewById(R.id.description)
        iv_place_image=findViewById(R.id.iv_place_image)

        btn_save=findViewById(R.id.btn_save)
        btn_save.setOnClickListener(this)

    }


    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.date -> {
                DatePickerDialog(
                    this@AddHappyPlace,
                    dateSetListener,
                    cal.get(Calendar.YEAR)
                    ,cal.get(Calendar.MONTH)
                    ,cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            R.id.tv_add_image -> {
                val pictureDialog=AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems= arrayOf("Upload photo from Gallery","Take photo")
                pictureDialog.setItems(pictureDialogItems){
                    dialog, which ->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()

            }

            R.id.location -> {
                try {

                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )

                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddHappyPlace)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            R.id.btn_save -> {
                when{
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please Enter Title" , Toast.LENGTH_SHORT).show()
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please Enter Description" , Toast.LENGTH_SHORT).show()
                    }
                    //et_location.text.isNullOrEmpty() -> {
                    //    Toast.makeText(this,"Please Enter Location" , Toast.LENGTH_SHORT).show()
                    //}
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this,"Please Add an Image" , Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            0,
                            et_title.text.toString(),
                            saveImageToInternalStorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                        if (addHappyPlace > 0){
                            setResult(Activity.RESULT_OK)
                            Toast.makeText(this,"New Happy Place Added",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }

    }

    private fun updateDate(){
        val dateFormat="dd.MM.yyyy"
        val sdf=SimpleDateFormat(dateFormat,Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data!=null){
                    val contentURI =data.data
                    try{
                        val selectedImageBitmap =MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)

                        saveImageToInternalStorage =saveImageToInternalStorage((selectedImageBitmap))

                        Log.e("Saved Image","Path :: $saveImageToInternalStorage")

                        iv_place_image.setImageBitmap(selectedImageBitmap)
                        Toast.makeText(this@AddHappyPlace,"Image Added",Toast.LENGTH_SHORT).show()

                    }
                    catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlace,"Failed to load Image!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if(requestCode== CAMERA){
                val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap

                saveImageToInternalStorage=saveImageToInternalStorage((thumbnail))

                Log.e("Saved Image","Path :: $saveImageToInternalStorage")

                iv_place_image.setImageBitmap(thumbnail)
                Toast.makeText(this@AddHappyPlace,"Image Added",Toast.LENGTH_SHORT).show()
            }
            else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

                val place: Place = Autocomplete.getPlaceFromIntent(data!!)

                et_location.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }

    }


    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object:MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport)
            {
                if (report.areAllPermissionsGranted()){
                    val galleryIntent =Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    this@AddHappyPlace.startActivityForResult(galleryIntent, CAMERA)
                }

            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }


    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object:MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport)
            {
                if (report.areAllPermissionsGranted()){
                    val galleryIntent =Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    this@AddHappyPlace.startActivityForResult(galleryIntent, GALLERY)
                }

            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()


    }


    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("It looks like permissions are turned off for this application. You can turn it on in the settings anytime.")
            .setPositiveButton("Go to Settings"){
                _, _ ->
                    try {
                        val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package",packageName,null)
                        intent.data= uri
                        startActivity(intent)
                    }
                    catch (e: ActivityNotFoundException){
                        e.printStackTrace()
                    }
            }.setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }.show()


    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream:OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object  {
        private const val GALLERY = 1
        private const val CAMERA =2
        private const val IMAGE_DIRECTORY="HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE=3
    }

}