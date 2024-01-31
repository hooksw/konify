
import io.github.hooksw.konify.ui.diff.DiffOperation
import io.github.hooksw.konify.ui.diff.DiffUtils
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime

class DiffutilsTest {
    private fun test(list1: List<Int>, list2: List<Int>) {
        val mutableList1 = list1.toMutableList()
        val diff = DiffUtils(object : DiffOperation {
            override fun insert(toBeInsertedIndex: Int, newListItemIndex: Int) {
                mutableList1.add(toBeInsertedIndex, list2[newListItemIndex])
                println("insert(index:$toBeInsertedIndex,item:${list2[newListItemIndex]})")
            }

            override fun remove(index: Int) {
                val item = mutableList1.removeAt(index)
                println("remove(index:$index,item:$item)")
            }

            override fun move(from: Int, to: Int) {
                val item = mutableList1.removeAt(from)
                mutableList1.add(to, item)
                println("move(from:$from,to:$to,item:$item)")
            }

        })
        measureTime {
            diff.perform(list1, list2)
        }.also(::println)
        println("\noriginal:$list1")
        assertEquals(list2, mutableList1)
    }

    @Test
    fun test1() {
        val list1 = listOf(1, 2, 3, 4)
        val list2 = listOf(1, 2, 5, 6, 3, 4)
        test(list1, list2)
    }

    @Test
    fun test2() {
        val list1 = listOf(1, 2, 3, 4, 5, 6)
        val list2 = listOf(1, 2, 5, 6)
        test(list1, list2)
    }

    @Test
    fun test3() {
        val list1 = listOf(1, 2, 3, 4)
        val list2 = listOf(1, 2, 3, 4, 5)
        test(list1, list2)
    }

    @Test
    fun test4() {
        val list1 = listOf(1, 2, 3, 4)
        val list2 = listOf(0, 5, 1, 2, 3, 4)
        test(list1, list2)
    }

    @Test
    fun test5() {
        val list1 = listOf(1, 2, 3, 4)
        val list2 = listOf(2, 3, 4)
        test(list1, list2)
    }

    @Test
    fun test6() {
        val list1 = listOf(1, 2, 3, 4)
        val list2 = listOf(1, 2, 3)
        test(list1, list2)
    }

    @Test
    fun test7() {
        val list1 = listOf(1, 2, 3, 4, 5, 6)
        val list2 = listOf(2, 5, 7, 3, 9, 0, 1, 6)
        test(list1, list2)
    }

    @Test
    fun test8() {
        val list1 = listOf(1, 2, 3, 4, 5, 6)
        val list2 = listOf(1, 2, 8, 3, 4, 7, 5, 6)
        test(list1, list2)
    }

    @Test
    fun test9() {
        val list1 = listOf(4, 0, 6, 9)
        val list2 = listOf(17, 14, 2, 13, 19, 9, 1, 4)
        test(list1, list2)
    }

    @Test
    fun test10() {
        val list1 = listOf(4, 1, 8, 11, 6, 17, 14, 0, 9, 7, 16, 10)
        val list2 = listOf(4, 10, 7, 6, 9, 5, 14, 18, 8, 15, 16)
        test(list1, list2)
    }

    @Test
    fun random() {
        repeat(1000) {
            val list1 = IntArray(16) { Random.nextInt(20) }.toSet().toList()
            val list2 = IntArray(18) { Random.nextInt(20) }.toSet().toList()
            test(list1, list2)
        }
    }

}