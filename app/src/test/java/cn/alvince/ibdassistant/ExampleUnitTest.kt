package cn.alvince.ibdassistant

import org.junit.Test

import org.junit.Assert.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testThread() {
        val signal = AtomicInteger(0)
        thread {
            while (true) {
                if (signal.get() % 3 == 0) {
                    println(1)
                    signal.addAndGet(1)
                }
            }
        }
        thread {
            while (true) {
                if (signal.get() % 3 == 1) {
                    println(2)
                    signal.addAndGet(1)
                }
            }
        }
        thread {
            while (true) {
                if (signal.get() % 3 == 2) {
                    println(3)
                    signal.addAndGet(1)
                }
            }
        }
    }
}
