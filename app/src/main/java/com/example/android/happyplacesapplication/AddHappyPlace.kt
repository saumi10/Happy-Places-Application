package com.example.android.happyplacesapplication

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.NonCancellable.cancel
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlace : AppCompatActivity(), View.OnClickListener{

    private var cal=Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)
        val toolbar=findViewById<Toolbar>(R.id.toolbar_add_place)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{
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
                        1 -> featureSoon()
                    }
                }
                pictureDialog.show()

            }
        }

    }

    private fun featureSoon(){
        Toast.makeText(this,"FEATURE COMING SOON",Toast.LENGTH_SHORT).show()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object:MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport)
            {if (report.areAllPermissionsGranted()){

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
}