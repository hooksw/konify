package com.example.androidsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ui.Row
import com.example.ui.Text
import com.example.prop.host
import com.example.ui.configContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configContext(this)
        host {
            Row(it){
                Text(it,"111")
                Text(it,"111")
            }
        }
    }
}