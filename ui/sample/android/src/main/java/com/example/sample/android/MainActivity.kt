package com.example.sample.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ui.runtime.Switch
import com.example.ui.common.component.text.Text
import com.example.ui.runtime.platform.host
import com.example.ui.runtime.state.mutableStateOf

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        host {
            val condition = mutableStateOf(true)

            Switch {
                If(condition) {
                    Text("Hello")
                }
                Else {
                    Text("World")
                }
            }
        }
    }
}
