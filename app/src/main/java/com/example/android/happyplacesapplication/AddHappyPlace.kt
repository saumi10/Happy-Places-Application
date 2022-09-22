package com.example.android.happyplacesapplication

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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.NonCancellable.cancel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlace : AppCompatActivity(), View.OnClickListener {

    private var cal=Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

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

        val et_date=findViewById<EditText>(R.id.date)
        et_date.setOnClickListener(this)

        val tv_add_image=findViewById<TextView>(R.id.tv_add_image)
        tv_add_image.setOnClickListener(this)
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
        }

    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==Activity.RESULT_OK){
            val iv_place_image=findViewById<ImageView>(R.id.iv_place_image)
            if(requestCode == GALLERY){
                if(data!=null){
                    val contentURI =data.data
                    try{
                        val selectedImageBitmap =MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)

                        val saveImageToInternalStorage=saveImageToInternalStorage((selectedImageBitmap))

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

                val saveImageToInternalStorage=saveImageToInternalStorage((thumbnail))

                Log.e("Saved Image","Path :: $saveImageToInternalStorage")

                iv_place_image.setImageBitmap(thumbnail)
                Toast.makeText(this@AddHappyPlace,"Image Added",Toast.LENGTH_SHORT).show()
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


    private fun updateDate(){
        val dateFormat="dd.MM.yyyy"
        val sdf=SimpleDateFormat(dateFormat,Locale.getDefault())
        val et_date=findViewById<EditText>(R.id.date)
        et_date.setText(sdf.format(cal.time).toString())
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
    }

}