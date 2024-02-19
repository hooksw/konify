import io.github.hooksw.konify.runtime.reactive.bind
import io.github.hooksw.konify.runtime.reactive.signalOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

expect fun setup()

class SignalTest {
    @BeforeTest
    fun before(){
        setup()
    }
    @Test
    fun test() {
        val a = signalOf(0)
        var b by a
        var c = 0
        bind {
            c++
        }
        repeat(10) {
            b++
        }
        assertTrue(b == 10)
    }

}