package test

import core.ConstExp
import kotlin.test.Test

@ConstExp
fun myFunction(value: Int): Int = value + 1

class ConstExpTest {
    @Test
    fun firstTest() {
        println(myFunction(1))
    }
}