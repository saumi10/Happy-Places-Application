package com.example.android.happyplacesapplication.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.android.happyplacesapplication.R
import com.example.android.happyplacesapplication.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_happy_place_detail.*

class HappyPlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)
        val toolbar=findViewById<Toolbar>(R.id.toolbar_happy_place_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        var happyPlaceDetailModel: HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            happyPlaceDetailModel =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }


        if (happyPlaceDetailModel != null) {
            iv_place_image.setImageURI(Uri.parse(happyPlaceDetailModel.image))
        }
        if (happyPlaceDetailModel != null) {
            tv_description.text = happyPlaceDetailModel.description
        }
        if (happyPlaceDetailModel != null) {
            tv_location.text = happyPlaceDetailModel.location
        }

    }
}