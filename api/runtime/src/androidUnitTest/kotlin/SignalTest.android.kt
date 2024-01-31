import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

actual fun setup() {
// Mocking Looper.getMainLooper() using MockK
    mockkStatic(Looper::class)

// Provide a mock Looper instance for Looper.getMainLooper()
//    val mockMainLooper = mockk<Looper>()
    every { Looper.getMainLooper().isCurrentThread } returns true
}