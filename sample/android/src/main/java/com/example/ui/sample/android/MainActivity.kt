package com.example.ui.sample.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ui.common.component.text.Text
import com.example.ui.runtime.LaunchedEffect
import com.example.ui.runtime.Switch
import com.example.ui.runtime.platform.setContent
import com.example.ui.runtime.state.mutableStateOf
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val booleanState = mutableStateOf(false)

            Switch {
                If(booleanState) {
                    Text("Hello")
                }
                Else {
                    Text("World")
                }
            }

            LaunchedEffect {
                while (true) {
                    delay(1000L)
                    booleanState.value = !booleanState.value
                }
            }
        }
    }
}
