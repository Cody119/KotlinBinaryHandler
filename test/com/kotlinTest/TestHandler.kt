package com.kotlinTest

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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

    var result = true
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
        result = false
    }

    internal fun reset() {
        removeMessages()
        result = true
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
                if (test.case.result) {
                    if (!test.case.resString.isEmpty()) {
                        println(test.case.resString)
                    }
                } else {
                    failureCount++
                    if (!test.case.resString.isEmpty()) {
                        println(test.case::class.simpleName + " test failed with message:\n" + test.case.resString)
                    }
                }
            } catch (x : Exception) {
                failureCount++
                println(test.case::class.simpleName + " test failed with Exception:")
                println(x.message)
                x.stackTrace.forEach {
                    println(it.toString())
                }
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