package com.kotlinTest

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Created by SuperRainbowNinja on 19/01/2018.
 */

class TestOutputStream(x : Int) : ByteArrayOutputStream(x) {
    constructor() : this(50)
    fun getInputStream() : ByteArrayInputStream = ByteArrayInputStream(buf)
}

abstract class TestCase {
    abstract fun runTestCase()

    fun getBuff() = TestOutputStream()
    fun getBuff(x : Int) = TestOutputStream(x)

    var result : Result = Success()
        private set
    var resString = ""
        private set

    fun removeMessages() {
        resString = ""
    }

    fun print(msg : String) {
        resString += msg
    }

    fun println(msg : String) {
        resString += msg + "\n"
    }

    fun fail() {
        result = Failure()
    }

    internal fun reset() {
        removeMessages()
        result = Success()
    }
}

class DoTest(val rep: Int, val case: TestCase) {
    constructor(case: TestCase) : this(1, case)
}

fun doTests(vararg tests: DoTest) {
    var failureCount = 0
    val total = tests.map { it.rep }.sum()
    for (test in tests) {
        for (XXX in Array(test.rep, {})) {
            try {
                test.case.runTestCase()
                when (test.case.result) {
                    is Success -> {
                        if (!test.case.resString.isEmpty()) {
                            println(test.case.resString)
                        }
                    }
                    is Failure -> {
                        failureCount++
                        if (!test.case.resString.isEmpty()) {
                            println(test.case::class.simpleName + " test failed with message:\n" + test.case.resString)
                        }
                    }
                }
            } catch (x : Exception) {
                failureCount++
                println(test.case::class.simpleName + " test failed with message:")
                println(x.localizedMessage)
            }
            test.case.reset()
        }
    }
    println(
            if (failureCount == 0) {
                println("All test cases run successfully")
            } else {
                println("$failureCount/$total cases failed")
            }
    )
}

fun charArray(vararg v : Char) : CharArray {
    return CharArray(v.size, v::get)
}

val characters = charArray('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')

fun generateString() : String {
    val tmp = Random()
    val cc = ArrayList<Char>()
    cc.addAll(characters.asList())
    cc.addAll(characters.map { it.toUpperCase() })

    return String(CharArray(tmp.nextInt(10), {cc[tmp.nextInt(cc.size)]}))
}

sealed class Result
class Success : Result()
//data class SuccessWithMessage(val msg: String) : Result()
class Failure : Result()