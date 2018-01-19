import com.kotlinTest.DoTest
import com.kotlinTest.doTests

/**
 * Created by SuperRainbowNinja on 8/01/2018.
 */

fun main(args: Array<String>) {
    doTests(
            DoTest(SimpleTest()),
            DoTest(100, StringTest())
    )
}

