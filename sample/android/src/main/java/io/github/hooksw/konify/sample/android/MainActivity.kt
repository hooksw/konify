package io.github.hooksw.konify.sample.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.hooksw.konify.ui.component.text.Text
import io.github.hooksw.konify.runtime.LaunchedEffect
import io.github.hooksw.konify.runtime.Switch
import io.github.hooksw.konify.ui.platform.setContent
import io.github.hooksw.konify.runtime.signal.signalOf

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showA by signalOf(false)
            var showB by signalOf(false)
            LaunchedEffect {
                showA=true
                showB=true
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