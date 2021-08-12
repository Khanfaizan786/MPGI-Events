package com.example.mpgievents

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {

    private lateinit var frame:FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        frame=findViewById(R.id.frame)

        supportFragmentManager.beginTransaction().replace(R.id.frame, HomeFragment()).commit()
    }
}
