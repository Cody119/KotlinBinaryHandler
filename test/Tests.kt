import com.kotlinBinaryHandler.Compiler
import com.kotlinBinaryHandler.Lex
import com.kotlinBinaryHandler.asString
import com.kotlinTest.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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


        val str = Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[3] = {1,2,3};}")[0]
        val str2 = Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[3] = {1,2,3};}")[0]
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

class CopyTest : TestCase() {
    override fun runTestCase() {
        val x = getBuff()
        val y = x.getInputStream()

        val z = Array<Any>(3, {it + 5})
        z[0] = 3
        z[2] = Array(3, {it + 1})


        val str = Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[3];}")[0]
        val str2 = str.copy(ArrayList(), HashMap())

        str.serialize(x, z)
        z[1] = 1000
        str2.serialize(x, z)

        str.deserialize(y)
        str2.deserialize(y)

        this.print("| ")
        for(it in str.array) {
            this.print(it.toString() + " | ")
            if (it is Array<*>) {
                for (value in it) {
                    this.print(value.toString() + " | ")
                }
            }
        }

        this.print("\n| ")
        for(it in str2.array) {
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

        val testStr = generateString()

        val s = Array(2, {
            if (it == 0) {
                testStr.length
            } else {
                testStr
            }
        })

        val str3 = Compiler(Lex()).compile("struct test {int len; char str[len];}")[0]
        val str4 = Compiler(Lex()).compile("struct test {int len; char str[len];}")[0]
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