package io.github.hooksw.konify.sample.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.hooksw.konify.common.component.text.Text
import io.github.hooksw.konify.runtime.LaunchedEffect
import io.github.hooksw.konify.runtime.Switch
import io.github.hooksw.konify.runtime.platform.setContent
import io.github.hooksw.konify.runtime.signal.signalOf

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val showA = signalOf(false)
            val showB = signalOf(false)
            LaunchedEffect {
                showA.value=true
                showB.value=true
            }
            Switch {
                If(showA) {
                    Text("Hello")
                }
                If(showB) {
                    Text("Hello")
                }
                Else {
                    Text("World")
                }
            }
            Text("bottom")

        }
    }
}
