package test

import core.Log
import kotlin.test.Test

@Log
fun myFunction(value: Int): Int {
    return value + 1
}

class LogTest {
    @Test
    fun firstTest() {
        myFunction(1)
    }
}