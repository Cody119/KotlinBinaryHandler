import com.kotlinBinaryHandler.Compiler
import com.kotlinTest.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by SuperRainbowNinja on 19/01/2018.
 */

data class Test(var x: Int, var y: Int, var z: Int)

class Test1 : TestCase() {
    override fun runTestCase() {
        Compiler().compile("struct test{int x;int y;int z;}")
        Compiler().compile("struct test{int x=0;int y=1;int z=2;}")
        Compiler().compile("struct test{int x;int y=2;int z;}")


    }
}

class Test2 : TestCase() {
    override fun runTestCase() {
        val write = TestOutputStream()
        val read = write.getInputStream()

        Compiler().compile("struct test{int x=0;int y=1;int z=2;}")[0].create(Test::class).serialize(write, Test(1,1,1))
        val out = Test(-1,-1,-1)
        Compiler().compile("struct test{int x;int y;int z;}")[0].create(Test::class).deserialize(read, out)

        println(out)
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