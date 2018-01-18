import com.kotlinBinaryHandler.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Created by SuperRainbowNinja on 8/01/2018.
 */

class Tmp : ByteArrayOutputStream(50) {
    fun tmp() : ByteArray = buf
}

fun main(args: Array<String>) {
    val x = Tmp()
    //x.write(ByteArray(20, { intArrayOf(3,0,0,0,7,0,0,0,100,0,0,0,44,1,0,0,144,1,0,0).get(it).toByte() }))
    val y = ByteArrayInputStream(x.tmp())
    val z = Array<Any>(3, {it + 5})
    z[0] = 3
    z[2] = Array(3, {it + 1})

//    //Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[x];}").serialize(x, z)
//
//    Compiler(Lex()).compile("struct test {\nint x;\nint y = 7;\nint z[x];}").deserialize(y).forEach {
//        println(it)
//        if (it is Array<*>) {
//            for (value in it) {
//                println(value)
//            }
//        }
//    }

//    for (tmp in getSerializer(Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[x];}"), z)) {
//        tmp.deserialize(y)
//        println(tmp.value)
//        val array = tmp.value
//        if (array is Array<*>) {
//            for (value in array) {
//                println(value)
//            }
//        }
//    }

    //println(getConstantProperty("{1,2,{{{4, 4, 5, 6.6}}}}"))


//    val str = Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[3] = {1,2,3};}")
//    val str2 = Compiler(Lex()).compile("struct test {\nint x;\nint y;\nint z[3] = {1,2,3};}")
//    str2.serialize(x, z)
//
//    str.deserialize(y)
//    for(it in str.array) {
//        println(it)
//        if (it is Array<*>) {
//            for (value in it) {
//                println(value)
//            }
//        }
//    }

    val testStr = "test Str"

    val s = Array<Any>(2, {
        if (it == 0) {
            testStr.length
        } else {
            Array(testStr.length, {testStr[it].toInt()})
        }
    })
    val str3 = Compiler(Lex()).compile("struct test {int len; int str[len];}")
    val str4 = Compiler(Lex()).compile("struct test {int len; int str[len];}")
    str4.serialize(x, s)

    str3.deserialize(y)
    val tmp = str3.array[1] as Array<Any>
    println(String(CharArray(tmp.size, {(tmp[it] as Int).toChar()})))
//    for(it in str3.array) {
//        println(it)
//        if (it is Array<*>) {
//            for (value in it) {
//                println(value)
//            }
//        }
//    }

//    val aaaa = Array(testStr.length, {testStr[it].toInt() })
//    println(String(CharArray(aaaa.size, { aaaa[it].toChar() })))
//
//    println((tmp[tmp.size - 1] as Int))
//    println((aaaa[aaaa.size - 1]))

}

//fun generateString() : String {
//    val tmp = Random()
//
//}