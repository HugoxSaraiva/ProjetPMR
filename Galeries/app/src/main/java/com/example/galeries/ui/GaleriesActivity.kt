package com.example.galeries.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.galeries.R
import kotlinx.android.synthetic.main.activity_galeries.*

class GaleriesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeries)

        setSupportActionBar(toolbar)
    }
}
