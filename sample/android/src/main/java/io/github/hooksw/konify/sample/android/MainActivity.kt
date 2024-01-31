package io.github.hooksw.konify.sample.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.hooksw.konify.ui.component.text.Text
import io.github.hooksw.konify.foundation.LaunchedEffect
import io.github.hooksw.konify.ui.platform.setContent
import io.github.hooksw.konify.foundation.signal.signalOf
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var str by signalOf("")
//            var showA by signalOf(false)
//            var showB by signalOf(false)
            LaunchedEffect {
                while (true){
                    delay(200)
                    str=Math.random().toString()
                }
            }
//            Switch {
//                If(showA) {
//                    Text("Hello")
//                }
//                If(showB) {
//                    Text("Hello")
//                }
//                Else {
//                    Text("World")
//                }
//            }
            Text({ str })

        }
    }
}