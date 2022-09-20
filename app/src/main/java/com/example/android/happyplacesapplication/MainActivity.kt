package com.example.android.happyplacesapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab=findViewById<FloatingActionButton>(R.id.fabAddHappyPlace)
        fab.setOnClickListener{
            val intent = Intent(this, AddHappyPlace::class.java)
            startActivity(intent)
        }
    }
}