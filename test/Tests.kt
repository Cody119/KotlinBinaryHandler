import com.kotlinBinaryHandler.Compiler
import com.kotlinBinaryHandler.Lex
import com.kotlinBinaryHandler.asString
import com.kotlinTest.*

/**
 * Created by SuperRainbowNinja on 19/01/2018.
 */


class SimpleTest : TestCase() {
    override fun runTestCase() {
        val x = getBuff()
        val y = x.getInputStream()

        //x.write(ByteArray(20, { intArrayOf(3,0,0,0,7,0,0,0,100,0,0,0,44,1,0,0,144,1,0,0).get(it).toByte() }))

        val z = Array<Any>(3, {it + 5})
        z[0] = 3
        z[2] = Array(3, {it + 1})


        val str = Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[3] = {1,2,3};}")
        val str2 = Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[3] = {1,2,3};}")
        str2.serialize(x, z)

        str.deserialize(y)
        this.print("| ")
        for(it in str.array) {
            this.print(it.toString() + " | ")
            if (it is Array<*>) {
                for (value in it) {
                    this.print(value.toString() + " | ")
                }
            }
        }
    }
}

class StringTest : TestCase() {
    override fun runTestCase() {
        val x = getBuff()
        val y = x.getInputStream()

        val testStr = com.kotlinTest.generateString()

        val s = Array(2, {
            if (it == 0) {
                testStr.length
            } else {
                testStr
            }
        })

        val str3 = Compiler(Lex()).compile("struct test {int len; char str[len];}")
        val str4 = Compiler(Lex()).compile("struct test {int len; char str[len];}")
        str4.serialize(x, s)

        str3.deserialize(y)
        val tmp = (str3.array[1] as Array<Any>).asString()
        if (tmp != testStr) {
            fail()
            removeMessages()
            this.println("case '$testStr' was found as '$tmp'")
        }
    }

}