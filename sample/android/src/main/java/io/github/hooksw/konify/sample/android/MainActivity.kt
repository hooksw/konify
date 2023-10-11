package io.github.hooksw.konify.sample.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import io.github.hooksw.konify.common.component.text.Text
import io.github.hooksw.konify.runtime.LaunchedEffect
import io.github.hooksw.konify.runtime.Switch
import io.github.hooksw.konify.runtime.platform.setContent
import io.github.hooksw.konify.runtime.state.mutableStateOf
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DiffUtil
        DiffResult
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
            Text("bottom")

            LaunchedEffect {
                while (true) {
                    delay(1000L)
                    booleanState.value = !booleanState.value
                }
            }
        }
    }
}
