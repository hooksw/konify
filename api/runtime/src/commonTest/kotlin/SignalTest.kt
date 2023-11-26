import androidx.collection.MutableScatterMap
import io.github.hooksw.konify.runtime.signal.*
import io.github.hooksw.konify.runtime.utils.UnitCallBack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignalTest {
    private val fakeOwner= object : Owner {
        override val sources: MutableScatterMap<StateObserver, UnitCallBack>
            = MutableScatterMap()
    }
    @Test
    fun signals() {
        val a = signalOf(0)
        val b by a
        Owners.add(fakeOwner)
        signalConsume {
            assertTrue { CurrentListener?.isNotEmpty() }
            println(b)
            assertTrue { fakeOwner.sources.isNotEmpty() }
        }
        assertEquals((a as ObservedSignal).observers.size, 1)
        val o=Owners.removeLast()
        assertTrue { o==fakeOwner }
        o.disposeObservers()
        assertEquals(a.observers.size, 0)
    }
}